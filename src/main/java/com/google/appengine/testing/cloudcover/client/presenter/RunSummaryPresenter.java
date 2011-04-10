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
package com.google.appengine.testing.cloudcover.client.presenter;

import com.google.appengine.testing.cloudcover.client.StyleHelper;
import com.google.appengine.testing.cloudcover.client.model.Run;
import com.google.appengine.testing.cloudcover.client.model.RunSummary;
import com.google.appengine.testing.cloudcover.client.model.Test;
import com.google.appengine.testing.cloudcover.client.model.TestStatus;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;

/**
 * @author max.ross@gmail.com (Max Ross)
 */
public class RunSummaryPresenter extends Composite {
  public interface Display {
    HasText getSummary();
    Label getNumInProgress();
    Label getNumPassed();
    Label getNumFailed();
    Label getNumTooSlow();
    Label getNumNotStarted();
    void setVisible(boolean visible);
  }

  private final Display display;
  private RunSummary runSummary;

  RunSummaryPresenter(Display display) {
    this.display = display;
  }

  void hideRunSummary() {
    display.setVisible(false);
    runSummary = null;
  }

  /**
   * returns {@code true} if the run is finished
   */
  public boolean setRunSummary(RunSummary runSummary) {
    this.runSummary = runSummary;
    boolean result = update();
    display.setVisible(true);
    return result;
  }

  private boolean update() {
    int numFinished = 0;
    int numPassed = 0;
    int numFailed = 0;
    int numInProgress = 0;
    int numTooSlow = 0;

    for (Test t : runSummary.getPassed()) {
      // you don't show up in the passed list unless all tests passed
      // so increment numPassed by the number of tests
      numPassed += t.getNumTests();
      numFinished += t.getNumTests();
    }

    for (Test t : runSummary.getFailed()) {
      int numFailuresForTest = t.getFailures().size();
      // the presence of the Test in the failed list means at least one
      // subtest failed.  make sure we account for subtests that passed
      numPassed += t.getNumTests() - numFailuresForTest;
      numFailed += numFailuresForTest;
      numFinished += t.getNumTests();
    }

    for (Test t : runSummary.getTooSlow()) {
      numTooSlow += t.getNumTests();
      numFinished += t.getNumTests();
    }

    for (Test t : runSummary.getInProgress()) {
      numInProgress += t.getNumTests();
    }

    int numNotStarted = runSummary.getRun().getNumTests() - (numFinished + numInProgress);
    int pctComplete = Float.valueOf((numFinished / (float) runSummary.getNumTests()) * 100).intValue();
    Status status = getStatus(numFinished, numInProgress, runSummary.getRun());
    update(
        "Run " + runSummary.getRun().getId() + ": " + status + ", " + "Completed " +
         numFinished + "/" + runSummary.getNumTests() + " (" + pctComplete + "%)",
         String.valueOf(numInProgress),
         String.valueOf(numNotStarted),
         String.valueOf(numPassed),
         String.valueOf(numFailed),
         String.valueOf(numTooSlow));
    return status == Status.FINISHED;
  }

  private void update(String summary, String inProgress, String notStarted, String passed,
                      String failed, String tooSlow) {
    display.getSummary().setText(summary);
    display.getNumInProgress().setText(inProgress);
    StyleHelper.setStatus(TestStatus.IN_PROGRESS, display.getNumInProgress());
    display.getNumPassed().setText(passed);
    StyleHelper.setStatus(TestStatus.NOT_STARTED, display.getNumNotStarted());
    display.getNumNotStarted().setText(notStarted);
    StyleHelper.setStatus(TestStatus.SUCCESS, display.getNumPassed());
    display.getNumTooSlow().setText(tooSlow);
    StyleHelper.setStatus(TestStatus.TOO_SLOW, display.getNumTooSlow());
    display.getNumFailed().setText(failed);
    StyleHelper.setStatus(TestStatus.FAILURE, display.getNumFailed());
  }

  private enum Status { NOT_STARTED, RUNNING, FINISHED }

  private Status getStatus(int numFinished, int numInProgress, Run run) {
    if (numFinished == 0 && numInProgress == 0) {
      return Status.NOT_STARTED;
    } else if (numFinished == run.getNumTests()) {
      return Status.FINISHED;
    } else {
      return Status.RUNNING;
    }
  }

}
