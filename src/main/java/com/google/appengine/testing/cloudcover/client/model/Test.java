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
import java.util.HashSet;
import java.util.Set;

/**
 * @author Max Ross <max.ross@gmail.com>
 */
public class Test implements Serializable {

  private String name;
  private Run run;
  private Date startTime = new Date();
  private Date endTime;
  private TestStatus testStatus = TestStatus.IN_PROGRESS;
  private long numTests;
  private Set<String> successes = new HashSet<String>();
  private Set<Failure> failures = new HashSet<Failure>();

  private Test() {}
  
  public Test(String name, Run run, long numTests) {
    this.name = name;
    this.run = run;
    this.numTests = numTests;
  }

  public String getName() {
    return name;
  }

  public Date getStartTime() {
    return startTime;
  }

  public Date getEndTime() {
    return endTime;
  }

  public void setEndTime(Date endTime) {
    this.endTime = endTime;
  }

  public TestStatus getStatus() {
    return testStatus;
  }

  public void setStatus(TestStatus testStatus) {
    this.testStatus = testStatus;
  }

  public Run getRun() {
    return run;
  }

  public long getNumTests() {
    return numTests;
  }

  public Set<Failure> getFailures() {
    return failures;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Test test = (Test) o;

    if (!name.equals(test.name)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  public Set<String> getSuccesses() {
    return successes;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }
}
