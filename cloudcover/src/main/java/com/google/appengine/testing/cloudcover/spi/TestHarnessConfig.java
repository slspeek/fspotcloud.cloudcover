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
package com.google.appengine.testing.cloudcover.spi;

import com.google.appengine.api.labs.taskqueue.Queue;

import java.util.List;

/**
 * Config options for the {@link TestHarness}. 
 *
 * @author Max Ross <max.ross@gmail.com>
 */
public interface TestHarnessConfig {

  /**
   * @return the names of the suites that this config knows how to run.
   */
  List<String> getAvailableSuiteIds();

  /**
   * @param runId the unique id of the run that will use the queue.
   * @return the Queue to use for all async tasks (test execution, completion
   * notification, etc)
   */
  Queue getQueue(long runId);

  /**
   * @return the harness that knows how to run tests
   */
  TestHarness getTestHarness();

  /**
   * @param suiteId identifies the suite for which a new Testrun should be
   * created.  should be a value in the list returned by
   * {@link #getAvailableSuiteIds()}.
   * @return a new TestRun for the provided suite id.
   */
  TestRun newTestRun(String suiteId);

  /**
   * @return the base URL for all queue actions.  Must correspond to the
   * servlet mapping in web.xml
   */
  String getBaseQueueActionURL();

  /**
   * @return the IsolationMechanism that will be used to isolate tests from one
   * another.
   */
  IsolationMechanism getIsolationMechanism();

  /**
   * @return a listener that receives notifications of TestRun events.
   */
  TestRunListener getTestRunListener();
}
