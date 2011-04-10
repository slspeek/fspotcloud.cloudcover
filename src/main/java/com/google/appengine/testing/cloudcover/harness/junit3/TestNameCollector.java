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

import junit.framework.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * A {@link junit.framework.TestListener} that wipes all data written by the test when the
 * test finishes running.
 *
 * @author Max Ross <max.ross@gmail.com>
 */
public class TestNameCollector extends BaseTestListener {
  private final Set<String> testNames = new HashSet<String>();
  @Override
  public void startTest(Test test) {
    super.startTest(test);
    testNames.add(JUnit3TestHarness.getShortName(test));
  }

  public Set<String> getTestNames() {
    return testNames;
  }
}