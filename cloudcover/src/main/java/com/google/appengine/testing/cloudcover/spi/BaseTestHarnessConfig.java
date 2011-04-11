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
import com.google.appengine.api.labs.taskqueue.QueueFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Base implementation for {@link TestHarnessConfig}.
 *
 * @author Max Ross <max.ross@gmail.com>
 */
public abstract class BaseTestHarnessConfig implements TestHarnessConfig {

  /**
   * Returns the default {@link Queue}.  Subclass and override if you want to
   * use a different queue.
   */
  public Queue getQueue(long runId) {
    return QueueFactory.getDefaultQueue();
  }

  /**
   * Returns the default base URL for all queue actions.  Subclass and override
   * if you want to customize, but don't forget to adjust web.xml if you do!
   */
  public String getBaseQueueActionURL() {
    return "/cloudcover/queueAction/";
  }

  /**
   * Returns the default IsolationMechanism for Cloud Cover:
   * {@link IsolationMechanism#ONE_NAMESPACE_PER_TEST}.  Subclass and override
   * if you want to use a different IsolationMechanism.
   */
  public IsolationMechanism getIsolationMechanism() {
    return IsolationMechanism.ONE_NAMESPACE_PER_TEST;
  }

  /**
   * Returns the default TestRunListener, which does nothing.  Subclass and
   * override if you want to use a different TestRunListener.
   */
  public TestRunListener getTestRunListener() {
    return new TestRunListener() {
      public void onTestRunCompletion(String statusURL, long runId) {
        // by default we do nothing
      }
    };
  }

  /**
   * Returns the default list of available suite ids.  Subclass and override
   * if you want to make multiple suites available.
   */
  public List<String> getAvailableSuiteIds() {
    return Arrays.asList("default");
  }
}
