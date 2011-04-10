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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Max Ross <max.ross@gmail.com>
 */
public class TestResult implements Serializable {

  private long runId;
  private String testId;
  private TestStatus testStatus;
  private int numTests;
  private List<FailureData> failureData = new ArrayList<FailureData>();
  private Set<String> successfulTestNames = new HashSet<String>();

  private TestResult() {}
  
  public TestResult(long runId, String testId, TestStatus testStatus, int numTests,
                    List<FailureData> failureData, Set<String> successfulTestNames) {
    this.runId = runId;
    this.testId = testId;
    this.testStatus = testStatus;
    this.numTests = numTests;
    this.failureData.addAll(failureData);
    this.successfulTestNames.addAll(successfulTestNames);
  }

  public TestStatus getStatus() {
    return testStatus;
  }

  public List<FailureData> getFailureData() {
    return failureData;
  }

  public int getNumTests() {
    return numTests;
  }

  public long getRunId() {
    return runId;
  }

  public String getTestId() {
    return testId;
  }

  public Set<String> getSuccessfulTestNames() {
    return successfulTestNames;
  }
}
