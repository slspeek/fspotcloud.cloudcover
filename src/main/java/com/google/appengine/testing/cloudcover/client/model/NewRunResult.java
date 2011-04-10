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
package com.google.appengine.testing.cloudcover.client.model;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Max Ross <max.ross@gmail.com>
 */
public class NewRunResult implements Serializable {

  private Run run;
  private Map<String, Integer> testIdsToTestCounts;
  private String error;

  private NewRunResult() {}

  public NewRunResult(Run run, Map<String, Integer> testIdsToTestCounts) {
    this.run = run;
    this.testIdsToTestCounts = testIdsToTestCounts;
  }

  public NewRunResult(String error) {
    this.error = error;
  }

  public Run getRun() {
    return run;
  }

  public Map<String, Integer> getTestIdsToTestCounts() {
    return testIdsToTestCounts;
  }

  public String getError() {
    return error;
  }
}
