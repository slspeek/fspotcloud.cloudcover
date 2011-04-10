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
package com.google.appengine.testing.cloudcover.harness.junit3;

import com.google.appengine.testing.cloudcover.client.model.FailureData;
import com.google.appengine.testing.cloudcover.client.model.Test;
import com.google.appengine.testing.cloudcover.client.model.TestResult;
import com.google.appengine.testing.cloudcover.client.model.TestStatus;
import com.google.appengine.testing.cloudcover.harness.junitx.JUnitStackTraceRewriter;
import com.google.appengine.testing.cloudcover.spi.IsolationMechanism;
import com.google.appengine.testing.cloudcover.spi.TestHarness;
import com.google.appengine.testing.cloudcover.spi.TestHarnessConfig;

import junit.framework.TestCase;
import junit.framework.TestFailure;
import junit.framework.TestSuite;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A JUnit3 {@link TestHarness}.  The id of each {@link Test} is a class name,
 * so all tests belonging to that class are run.
 *
 * @author Max Ross <max.ross@gmail.com>
 */
public class JUnit3TestHarness implements TestHarness {

  public TestResult runTest(TestHarnessConfig config, Test test) {
    TestNameCollector collector = new TestNameCollector();
    DeadlineAwareTestResult result = newJUnitTestResult(config, test, collector);
    try {
      Class<?> cls = Class.forName(test.getName());
      TestSuite testSuite;
      try {
        Method m = cls.getMethod("suite");
        testSuite = (TestSuite) m.invoke(null);
      } catch (NoSuchMethodException nsme) {
        testSuite = new TestSuite(cls);
      }
      testSuite.run(result);
      return translateResult(test.getRun().getId(), test.getName(), testSuite.countTestCases(),
                             result, collector.getTestNames());
    } catch (RuntimeException rte) {
      throw rte;
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  protected DeadlineAwareTestResult newJUnitTestResult(TestHarnessConfig config, Test test,
                                                          TestNameCollector collector) {
    DeadlineAwareTestResult testResult = new DeadlineAwareTestResult();

    if (config.getIsolationMechanism() == IsolationMechanism.ONE_NAMESPACE_PER_TEST) {
      testResult.addListener(new NewNamespacePerTestListener(test));
    } else if (config.getIsolationMechanism() == IsolationMechanism.WIPE_STORAGE_AFTER_EACH_TEST){
      testResult.addListener(new DatastoreWipingTestListener());
    }
    testResult.addListener(collector);
    return testResult;
  }

  static String getShortName(junit.framework.Test t) {
    String shortName;
    if (t instanceof TestCase) {
      shortName = ((TestCase) t).getName();
    } else if (t instanceof TestSuite) {
      shortName = ((TestSuite) t).getName();
    } else {
      shortName = t.toString();
    }
    return shortName;
  }

  private TestResult translateResult(long runId, String testId, int numTests,
                                     DeadlineAwareTestResult result, Set<String> allTestNames) {
    Set<String> successfulTests = new HashSet<String>(allTestNames);
    TestStatus testStatus = TestStatus.SUCCESS;
    List<FailureData> failureData = new ArrayList<FailureData>();
    if (result.errorCount() != 0) {
      testStatus = TestStatus.FAILURE;
      @SuppressWarnings("unchecked")
      Enumeration<TestFailure> errorEnum = result.errors();
      while (errorEnum.hasMoreElements()) {
        TestFailure tf = errorEnum.nextElement();
        String shortName = getShortName(tf.failedTest());
        successfulTests.remove(shortName);
        failureData.add(testFailureToFalureData(shortName, tf));
      }
    }
    if (result.failureCount() != 0) {
      testStatus = TestStatus.FAILURE;
      @SuppressWarnings("unchecked")
      Enumeration<TestFailure> failureEnum = result.failures();
      while (failureEnum.hasMoreElements()) {
        TestFailure tf = failureEnum.nextElement();
        String shortName = getShortName(tf.failedTest());
        successfulTests.remove(shortName);
        failureData.add(testFailureToFalureData(shortName, tf));
      }
    }
    if (result.isTooSlow()) {
      testStatus = TestStatus.TOO_SLOW;
    }
    return new TestResult(runId, testId, testStatus, numTests, failureData, successfulTests);
  }

  FailureData testFailureToFalureData(String shortName, TestFailure tf) {
    String msg = tf.exceptionMessage();
    if (msg == null) {
      msg = "";
    }
    String trace = tf.trace();
    if (trace == null) {
      trace = "";
    }
    return new FailureData(shortName, msg, JUnitStackTraceRewriter.rewrite(trace));
  }
}
