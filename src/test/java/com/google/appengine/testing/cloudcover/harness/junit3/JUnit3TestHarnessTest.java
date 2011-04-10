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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestFailure;
import junit.framework.TestResult;

/**
 * @author Max Ross <max.ross@gmail.com>
 */
public class JUnit3TestHarnessTest extends TestCase {

  public void testStackTraceConversion() {
    Test failed = new Test() {
      public int countTestCases() {
        return 0;
      }

      public void run(TestResult result) {
      }
    };
    RuntimeException rte = new RuntimeException("boom");
    TestFailure tf = new TestFailure(failed, rte);
    JUnit3TestHarness harness = new JUnit3TestHarness();
    FailureData fd = harness.testFailureToFalureData("short name", tf);
    assertTrue(fd.getData(),
               fd.getData().startsWith("java.lang.RuntimeException: boom<br>&nbsp;&nbsp;&nbsp;&nbsp;"));
  }
}
