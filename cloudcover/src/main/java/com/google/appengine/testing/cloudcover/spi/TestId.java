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

import java.io.Serializable;

/**
 * @author Max Ross <max.ross@gmail.com>
 */
public final class TestId implements Serializable {
  private String testId;
  private int numSubtests;

  private TestId() {}

  public TestId(String testId, int numSubtests) {
    this.testId = testId;
    this.numSubtests = numSubtests;
  }

  public String getTestId() {
    return testId;
  }

  public int getNumSubtests() {
    return numSubtests;
  }

  @Override
  public String toString() {
    return testId;
  }
}
