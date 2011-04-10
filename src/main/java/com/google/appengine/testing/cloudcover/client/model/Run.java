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
import java.util.Date;
import java.util.Map;

/**
 * @author Max Ross <max.ross@gmail.com>
 */
public class Run implements Serializable {

  private long id;
  private String testRunnerConfigClass;
  private Date created;
  // total number of tests belonging to the run
  private int numTests;
  // the number of test entities that will be associated with this run once
  // all tests are in progress
  private int numTestEntities;

  private Map<String, Integer> testIdsToTestCounts;

  private Run() {}
  
  public Run(long id, String testRunnerConfigClass, Date created, Map<String, Integer> testIdsToTestCounts) {
    this.id = id;
    this.testRunnerConfigClass = testRunnerConfigClass;
    this.created = created;
    this.testIdsToTestCounts = testIdsToTestCounts;
  }

  public long getId() {
    return id;
  }

  public int getNumTests() {
    return numTests;
  }

  public void setNumTests(int numTests) {
    this.numTests = numTests;
  }

  public String getTestRunnerConfigClass() {
    return testRunnerConfigClass;
  }

  public Date getCreated() {
    return created;
  }

  public int getNumTestEntities() {
    return numTestEntities;
  }

  public void setNumTestEntities(int numTestEntities) {
    this.numTestEntities = numTestEntities;
  }

  public Map<String, Integer> getTestIdsToTestCounts() {
    return testIdsToTestCounts;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Run run = (Run) o;

    if (id != run.id) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return (int) (id ^ (id >>> 32));
  }
}
