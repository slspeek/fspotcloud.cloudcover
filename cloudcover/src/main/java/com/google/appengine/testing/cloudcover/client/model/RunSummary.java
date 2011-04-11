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
import java.util.Set;

/**
 * @author Max Ross <max.ross@gmail.com>
 */
public class RunSummary implements Serializable {
  private Run run;
  private Set<Test> passed;
  private Set<Test> failed;
  private Set<Test> tooSlow;
  private Set<Test> inProgress;

  private RunSummary() {}
  
  public RunSummary(Run run, Set<Test> passed, Set<Test> failed, Set<Test> tooSlow, Set<Test> inProgress) {
    this.run = run;
    this.passed = passed;
    this.failed = failed;
    this.tooSlow = tooSlow;
    this.inProgress = inProgress;
  }

  public Run getRun() {
    return run;
  }

  public Set<Test> getFailed() {
    return failed;
  }

  public Set<Test> getInProgress() {
    return inProgress;
  }

  public long getNumTests() {
    return run.getNumTests();
  }

  public Set<Test> getPassed() {
    return passed;
  }

  public Set<Test> getTooSlow() {
    return tooSlow;
  }

}
