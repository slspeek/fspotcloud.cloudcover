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
package com.google.appengine.testing.cloudcover.harness.junit4;

import com.google.appengine.testing.cloudcover.client.model.FailureData;
import com.google.appengine.testing.cloudcover.client.model.Test;
import com.google.appengine.testing.cloudcover.client.model.TestResult;
import com.google.appengine.testing.cloudcover.client.model.TestStatus;
import com.google.appengine.testing.cloudcover.spi.IsolationMechanism;
import com.google.appengine.testing.cloudcover.spi.TestHarness;
import com.google.appengine.testing.cloudcover.spi.TestHarnessConfig;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * A JUnit4 {@link TestHarness}. The id of each {@link Test} is a class name,
 * so all tests belonging to that class are run.
 *
 * @author Max Ross <max.ross@gmail.com>
 */
public class JUnit4TestHarness implements TestHarness {

  public TestResult runTest(TestHarnessConfig config, Test test) {
    JUnitCore u = newJUnitCore(config, test);
    try {
      Class<?> cls = Class.forName(test.getName());
      Result res = u.run(cls);
      return translateResult(test.getRun().getId(), test.getName(), res.getRunCount(), res);
    } catch (RuntimeException rte) {
      throw rte;
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  protected JUnitCore newJUnitCore(TestHarnessConfig config, Test test) {
    JUnitCore u = new JUnitCore();
    if (config.getIsolationMechanism() == IsolationMechanism.ONE_NAMESPACE_PER_TEST) {
      u.addListener(new NewNamespacePerRunListener(test));
    } else if (config.getIsolationMechanism() == IsolationMechanism.WIPE_STORAGE_AFTER_EACH_TEST) {
      u.addListener(new DatastoreWipingRunListener());
    }
    return u;
  }

  private TestResult translateResult(long runId, String testId, int numTests,
                                     Result result) {
    TestStatus testStatus = TestStatus.SUCCESS;
    List<FailureData> failureData = new ArrayList<FailureData>();
    // JUnit 4: doesn't distinguish between 'failure' and 'error'
    if (result.getFailureCount() != 0) {
      testStatus = TestStatus.FAILURE;
      List<Failure> failureList = result.getFailures();
      for (Failure fail : failureList) {
        failureData.add(new FailureData(fail.toString(), fail.getMessage(), fail.getTrace()));
      }
    }
    // TODO(maxr): fill in successful test names
    return new TestResult(runId, testId, testStatus, numTests, failureData, new HashSet<String>());
  }

  private static final String FIVE_SPACES = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

  private String testFailureToString(Failure failure) {
    return failure.getMessage().replace("\n", "<br>") + "<br>"
           + FIVE_SPACES
           + failure.getTrace().replace("\n", "<br>" + FIVE_SPACES);
  }
}
