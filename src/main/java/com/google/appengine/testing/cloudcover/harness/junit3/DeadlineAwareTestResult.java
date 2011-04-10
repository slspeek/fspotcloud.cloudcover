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

import com.google.apphosting.api.DeadlineExceededException;

import junit.framework.TestResult;

import java.util.logging.Logger;

/**
 * An extension to {@link TestResult} with special handling for
 * {@link DeadlineExceededException}.  When we encouter this exception
 * we don't want to run any more tests because we'll encounter
 * {@code HardDeadlineExceededException shortly thereafter and our cleanup}
 * won't run.
 *
 * @author Max Ross <max.ross@gmail.com>
 */
class DeadlineAwareTestResult extends TestResult {

  private static final Logger logger = Logger.getLogger(DeadlineAwareTestResult.class.getName());

  private boolean tooSlow = false;

  @Override
  public void addError(junit.framework.Test test, Throwable t) {
    super.addError(test, t);
    if (t instanceof DeadlineExceededException) {
      logger.fine("Test " + t + " took too long, remaining sub tests will not be run.");
      tooSlow = true;
      stop();
    }
  }

  boolean isTooSlow() {
    return tooSlow;
  }
}
