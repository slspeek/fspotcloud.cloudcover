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

import com.google.appengine.testing.cloudcover.client.CloudCoverServiceAsync;
import com.google.appengine.testing.cloudcover.client.model.Run;
import com.google.appengine.testing.cloudcover.client.model.RunSummary;
import com.google.appengine.testing.cloudcover.client.model.TestStatus;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

import java.util.Map;

/**
 * @author max.ross@gmail.com (Max Ross)
 */
public class RunPresenter {
  public interface Display {
    Widget asWidget();

    RunSummaryPresenter.Display getRunSummaryView();
    RunSelectionPresenter.Display getRunSelectionView();

    TestTreePresenter.Display getTestTreeView();

    TestDetailsPresenter.Display getTestDetailsView();

    HasText getErrorMsg();
  }

  private final CloudCoverServiceAsync svc;
  private final RunSummaryPresenter runSummaryPresenter;
  private final RunSelectionPresenter runSelectionPresenter;
  private final TestTreePresenter testTreePresenter;

  private final Display display;
  private Long runId;
  private boolean isFinished;
  private String error;
  private final Timer refresh = new Timer() {
    @Override
    public void run() {
      update();
    }
  };

  public RunPresenter(CloudCoverServiceAsync svc, Display display,
      final HasWidgets container) {
    this.svc = svc;
    this.display = display;

    runSummaryPresenter = new RunSummaryPresenter(display.getRunSummaryView());
    TestDetailsPresenter testDetailsPresenter =
        new TestDetailsPresenter(display.getTestDetailsView());
    testTreePresenter = new TestTreePresenter(svc, display.getTestTreeView(), testDetailsPresenter);
    runSelectionPresenter = new RunSelectionPresenter(
        svc, display.getRunSelectionView(), this, runSummaryPresenter, testTreePresenter,
        testDetailsPresenter);

    container.clear();
    container.add(display.asWidget());
    String hash = Window.Location.getHash();
    int hashIndex = hash.indexOf("#");
    if (hashIndex != -1) {
      String afterHash = hash.substring(hashIndex + 1);
      StringBuilder sb = new StringBuilder();
      // iChat is adding a trailing slash to the url
      // strip it off
      for (int i = 0; i < afterHash.length(); i++) {
        if (Character.isDigit(afterHash.charAt(i))) {
          sb.append(afterHash.charAt(i));
        } else {
          break;
        }
      }
      runId = Long.valueOf(sb.toString());
      AsyncCallback<RunSummary> callback = new AsyncCallback<RunSummary>() {
        public void onFailure(Throwable caught) {
          // TODO(maxr): display the error quietly
        }

        public void onSuccess(RunSummary summary) {
          setRunData(summary.getRun(), summary.getRun().getTestIdsToTestCounts());
          isFinished = updateRunStatus(summary);
        }
      };

      svc.getRunSummary(runId, true, callback);
    }
  }

  public void setRunData(Run run, Map<String, Integer> testIdsToTestCounts) {
    refresh.cancel();
    isFinished = false;
    runId = run.getId();
    testTreePresenter.setAllTestIds(run, testIdsToTestCounts);
    update();
    // refresh the run summary page every 3 seconds
    refresh.scheduleRepeating(3000);
  }

  public void setRunError(String error) {
    this.error = error;
    update();
  }

  private void update() {
    AsyncCallback<RunSummary> callback = new AsyncCallback<RunSummary>() {
      public void onFailure(Throwable caught) {
        // TODO(maxr): display the error quietly
      }

      public void onSuccess(RunSummary summary) {
        isFinished = updateRunStatus(summary);
      }
    };

    if (runId != null && !isFinished) {
      svc.getRunSummary(runId, false, callback);
    }

    display.getErrorMsg().setText(error);
  }

  private boolean updateRunStatus(RunSummary runSummary) {
    testTreePresenter.updateTreeItemStatus(runSummary.getInProgress(), TestStatus.IN_PROGRESS);
    testTreePresenter.updateTreeItemStatus(runSummary.getPassed(), TestStatus.SUCCESS);
    testTreePresenter.updateTreeItemStatus(runSummary.getFailed(), TestStatus.FAILURE);
    testTreePresenter.updateTreeItemStatus(runSummary.getTooSlow(), TestStatus.TOO_SLOW);
    return runSummaryPresenter.setRunSummary(runSummary);
  }
}
