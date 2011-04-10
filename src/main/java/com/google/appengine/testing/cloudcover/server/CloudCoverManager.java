/*
 * Copyright (C) 2010 Google Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.appengine.testing.cloudcover.server;

import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.TaskAlreadyExistsException;
import com.google.appengine.api.labs.taskqueue.TaskOptions;
import com.google.appengine.testing.cloudcover.client.model.Failure;
import com.google.appengine.testing.cloudcover.client.model.FailureData;
import com.google.appengine.testing.cloudcover.client.model.NewRunResult;
import com.google.appengine.testing.cloudcover.client.model.Run;
import com.google.appengine.testing.cloudcover.client.model.RunSummary;
import com.google.appengine.testing.cloudcover.client.model.Test;
import com.google.appengine.testing.cloudcover.client.model.TestResult;
import com.google.appengine.testing.cloudcover.client.model.TestStatus;
import com.google.appengine.testing.cloudcover.spi.TestHarness;
import com.google.appengine.testing.cloudcover.spi.TestHarnessConfig;
import com.google.appengine.testing.cloudcover.spi.TestId;
import com.google.appengine.testing.cloudcover.spi.TestRun;
import com.google.apphosting.api.DeadlineExceededException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CloudCoverManager is the brains of the test harness.  It handles the high
 * level operations requested by the servlet and delegates to the {@link TestHarnessDao}
 * for all persistence operations.
 *
 * @author Max Ross <max.ross@gmail.com>
 */
public class CloudCoverManager {

  private static final int FAN_IN_PERIOD = 1000;

  private final TestHarnessConfig harnessConfig;
  private final TestHarnessDao dao;
  private final Logger logger = Logger.getLogger(getClass().getName());

  public CloudCoverManager(TestHarnessConfig harnessConfig, TestHarnessDao dao) {
    this.harnessConfig = harnessConfig;
    this.dao = dao;
  }

  /**
   * Creates a new run
   * @param suiteId The id of the suite for which we want to create a new Run
   * @return A NewRunResult
   */
  public NewRunResult createNewRun(String suiteId) {
    logger.fine("Creating new run.");
    try {
      Run run = dao.newRun();
      TestRun testRun = harnessConfig.newTestRun(suiteId);
      Map<String, Integer> testIdsToTestCounts = scheduleTestExecution(run.getId(), testRun);
      int numSubTests = 0;
      for (Integer val : testIdsToTestCounts.values()) {
        numSubTests += val;
      }
      run.setNumTests(numSubTests);
      run.setNumTestEntities(testIdsToTestCounts.size());
      dao.updateRun(run);
      // schedule a task to create the test id data
      scheduleTestIdDataAddition(run.getId(), testIdsToTestCounts);
      logger.fine("Created new run with id " + run.getId());
      return new NewRunResult(run, testIdsToTestCounts);
    } catch (DeadlineExceededException dee) {
      String msg = "Unable to create run due to deadline exceeded exception";
      logger.fine(msg);
      return new NewRunResult(msg);
    }
  }

  private void scheduleTestIdDataAddition(long runId, Map<String, Integer> testIdsToTestCounts) {
    logger.fine("Scheduling addition of test id data for run " + runId);
    Queue q = harnessConfig.getQueue(runId);
    q.add(buildTaskOptionsForTestIdData(runId, testIdsToTestCounts));
    logger.fine("Scheduled addition of test id data for run " + runId);
  }

  public void newTestIdData(long runId, Map<String, Integer> testIdsToTestCounts) {
    logger.fine("Creating test id data.");
    try {
      dao.addRunTestIdData(runId, testIdsToTestCounts);
    } finally {
      logger.fine("Wrote test id data for run " + runId);
    }
  }

  /**
   * Fetches a RunSummary for a Run with optional test id data
   *
   * @param runId The unique id of the Run to load
   * @param loadTestIdData Whether or not to include load test in the RunSummary
   * @return The RunSummary for the Run uniquely identified by the given runId,
   * or {@code null} if no Run could be found.
   */
  public RunSummary getRunSummary(long runId, boolean loadTestIdData) {
    logger.fine("Getting status for run " + runId);
    Run run = dao.getRunById(runId, loadTestIdData);
    if (run == null) {
      return null;
    }
    Set<Test> failed = new HashSet<Test>();
    Set<Test> inProgress = new HashSet<Test>();
    Set<Test> passed = new HashSet<Test>();
    Set<Test> tooSlow = new HashSet<Test>();
    List<Test> testsForRun = dao.getTestsForRun(runId);
    for (Test t : testsForRun) {
      if (t.getStatus() == TestStatus.SUCCESS) {
        passed.add(t);
      } else if (t.getStatus() == TestStatus.FAILURE) {
        failed.add(t);
      } else if (t.getStatus() == TestStatus.IN_PROGRESS) {
        inProgress.add(t);
      } else if (t.getStatus() == TestStatus.TOO_SLOW) {
        tooSlow.add(t);
      }
    }
    logger.fine("Retrieved status for run " + runId);
    return new RunSummary(run, passed, failed, tooSlow, inProgress);
  }

  private Map<String, Integer> scheduleTestExecution(long runId, TestRun testRun) {
    logger.fine("Scheduling execution for run " + runId);
    Map<String, Integer> testIdsToTestCounts = new HashMap<String, Integer>();
    Queue q = harnessConfig.getQueue(runId);
    List<TestId> currentBatch = new ArrayList<TestId>();
    for (TestId testId : testRun.getTestIds(runId)) {
      if (testIdsToTestCounts.put(testId.getTestId(), testId.getNumSubtests()) != null) {
        throw new RuntimeException("More than one test with id " + testId);
      }
      currentBatch.add(testId);
      // max size for batch add is 100
      if (currentBatch.size() == 100) {
        q.add(buildTaskOptionsForTestRun(runId, currentBatch));
        currentBatch.clear();
      }
    }
    if (!currentBatch.isEmpty()) {
      q.add(buildTaskOptionsForTestRun(runId, currentBatch));
    }
    logger.fine("Scheduled execution of " + testIdsToTestCounts.size() + " tests for run " + runId);
    return testIdsToTestCounts;
  }

  private List<TaskOptions> buildTaskOptionsForTestRun(long runId, List<TestId> testIds) {
    List<TaskOptions> opts = new ArrayList<TaskOptions>();
    for (TestId testId : testIds) {
      opts.add(TaskOptions.Builder.method(TaskOptions.Method.POST)
        .url(harnessConfig.getBaseQueueActionURL() +
             "runtest/" + runId + "/" + testId.getTestId() + "/" + testId.getNumSubtests()));
    }
    return opts;
  }

  private TaskOptions buildTaskOptionsForTestIdData(long runId, Map<String, Integer> testIdsToTestCounts) {
    TaskOptions opts = TaskOptions.Builder.method(TaskOptions.Method.POST)
      .url(harnessConfig.getBaseQueueActionURL() + "testIdData/" + runId);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject(testIdsToTestCounts);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    opts.payload(baos.toByteArray(), "application/x-java-serialized-object");
    return opts;
  }

  private TaskOptions buildTaskOptionsForRunCompletionNotification(long runId) {
    String fanInKey = buildFanInKey(runId, System.currentTimeMillis());
    // need to wait until the fan-in period has passed to ensure we don't
    // perform the check too early
    return TaskOptions.Builder.method(TaskOptions.Method.POST)
        .url(harnessConfig.getBaseQueueActionURL() + "completionNotification/" + runId)
        .taskName(fanInKey)
        .countdownMillis(FAN_IN_PERIOD * 2);

  }

  static String buildFanInKey(long runId, long now) {
    // ensures we perform at most one completion check per second
    return Long.toString(runId) + "-" + Long.toString(now / FAN_IN_PERIOD);
  }

  public Failure getFailure(long runId, String testId, String failureId) {
    return dao.getFailure(runId, testId, failureId);
  }

  public void runTest(long runId, TestId testId) {
    logger.fine("Running test " + testId + " in run " + runId);
    Run run = dao.getRunById(runId, false);
    Test test;
    try {
      test = dao.newTest(run, testId);
    } catch (TestAlreadyExistsException taee) {
      // message must have been delivered more than once, which is fine.
      // this will happen when we encounter an exception while cleaning
      // up from a test run
      logger.log(Level.WARNING, "Received duplicate request to run test " + testId);
      test = taee.getTest();
      if (test.getStatus() == TestStatus.IN_PROGRESS) {
        // this isn't accurate - it might have failed for some other reason
        // we should really change "TOO_SLOW" TO "HARNESS ERROR" or something
        test.setStatus(TestStatus.TOO_SLOW);
        test.setEndTime(new Date());
        dao.updateTest(test);
      }
      return;
    }
    TestHarness harness = harnessConfig.getTestHarness();
    TestResult result = null;
    Throwable thrown = null;
    try {
      result = harness.runTest(harnessConfig, test);
      logger.fine("Test " + testId + " in run " + runId + " completed with status " + result.getStatus());
    } catch (RuntimeException t) {
      thrown = t;
    } finally {
      // We're doing this handling in the finally block because if we hit a
      // deadline exception in a catch block we'll get interrupted.  This way
      // we don't have to worry about a null result.
      cleanupAfterRunTest(testId, runId, run, test, result, thrown);
    }
  }

  private void cleanupAfterRunTest(TestId testId, long runId, Run run, Test test, TestResult result,
                                   Throwable thrown) {
    if (result == null) {
      test.setStatus(TestStatus.FAILURE);
      if (thrown != null) {
        String msg = "Test " + testId + " in run " + runId + " threw an exception of type "
                       + thrown.getClass().getName();
        result = new TestResult(
            runId, testId.getTestId(), TestStatus.FAILURE, -1,
            Collections.singletonList(new FailureData("Harness Error", msg, thrown.toString())),
            Collections.<String>emptySet());
        logger.log(Level.SEVERE, msg, thrown);
      } else {
        String msg = "Test " + testId + " in run " + runId + " has an error without an exception.";
        result = new TestResult(
            runId, testId.getTestId(), TestStatus.FAILURE, -1,
            Collections.singletonList(new FailureData("Harness Error", msg, "")),
            Collections.<String>emptySet());
        logger.log(Level.SEVERE, msg);
      }
    }
    addResultToTest(test, result);
    test.setEndTime(new Date());
    dao.updateTest(test);

    // We want to return as quickly as possible to avoid deadline errors
    // so we'll schedule a new task to take care of any completion
    // notifications that may be necessary
    scheduleCompletionNotification(runId);
  }

  private void scheduleCompletionNotification(long runId) {
    logger.fine("Scheduling completion notification for run " + runId);
    Queue q = harnessConfig.getQueue(runId);
    TaskOptions opts = buildTaskOptionsForRunCompletionNotification(runId);
    try {
      q.add(opts);
      logger.fine("Scheduled completion notification for run " + runId);
    } catch (TaskAlreadyExistsException taee) {
      // that's ok, it just means we already have a check scheduled
    }
  }

  private void addResultToTest(Test test, TestResult result) {
    test.setStatus(result.getStatus());
    test.getSuccesses().addAll(result.getSuccessfulTestNames());
    for (FailureData data : result.getFailureData()) {
      test.getFailures().add(new Failure(data.getShortName(), data.getFailureMsg(), data.getData()));
    }
  }

  public void doCompletionCheck(long runId, String serverURL) {
    if (dao.runIsComplete(runId) && dao.createCompletionRecordIfNotAlreadyPresent(runId)) {
      String runStatusURL = buildRunStatusURL(serverURL, runId);
      harnessConfig.getTestRunListener().onTestRunCompletion(runStatusURL, runId);
    } else {
      // either the run is not yet complete or somebody else already created
      // the completion record.  in either case just return without doing any
      // notification
    }
  }

  static String buildRunStatusURL(String serverURL, long runId) {
    return serverURL + "/cloudcover.html#" + runId;
  }

  /**
   * Returns {@code null} if the test has not yet been started.
   */
  public Test getTestById(long runId, String testId) {
    return dao.getTestById(runId, testId);
  }

  public List<String> getAvailableSuiteIds() {
    return harnessConfig.getAvailableSuiteIds();
  }

  public TestHarnessConfig getHarnessConfig() {
    return harnessConfig;
  }
}
