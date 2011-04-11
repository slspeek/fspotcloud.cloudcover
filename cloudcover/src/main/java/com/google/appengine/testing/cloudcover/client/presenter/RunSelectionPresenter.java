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
import com.google.appengine.testing.cloudcover.client.model.NewRunResult;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.ListBox;

import java.util.List;

/**
 * @author Max Ross <max.ross@gmail.com>
 */
public class RunSelectionPresenter extends Composite {
  public interface Display {
    Button getNewRunButton();
    ListBox getRunSelector();
    HasText getNewRunStatus();
  }

  private final Display display;

  RunSelectionPresenter(CloudCoverServiceAsync svc, Display display, RunPresenter runPresenter,
                        RunSummaryPresenter runSummaryPresenter, TestTreePresenter testTreePresenter,
                        TestDetailsPresenter testDetailsPresenter) {
    this.display = display;
    loadSuiteIds(svc);
    initializeNewRunButton(runPresenter, runSummaryPresenter, testTreePresenter, testDetailsPresenter, svc);
  }

  private void initializeNewRunButton(final RunPresenter runPresenter,
                                      final RunSummaryPresenter runSummaryPresenter,
                                      final TestTreePresenter testTreePresenter,
                                      final TestDetailsPresenter testDetailsPresenter,
                                      final CloudCoverServiceAsync svc) {
    display.getNewRunButton().addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        runPresenter.setRunError(null);
        AsyncCallback<NewRunResult> callback = new AsyncCallback<NewRunResult>() {
          public void onFailure(Throwable caught) {
            Window.alert(caught.toString());
          }

          public void onSuccess(NewRunResult result) {
            if (result.getError() != null) {
              runPresenter.setRunError(result.getError());
            } else {
              display.getNewRunStatus().setText("");
              runPresenter.setRunData(result.getRun(), result.getTestIdsToTestCounts());
            }
          }
        };
        runSummaryPresenter.hideRunSummary();
        testTreePresenter.clearTree();
        testDetailsPresenter.setTest(null);
        ListBox selector = display.getRunSelector();
        svc.createNewRun(selector.getItemText(selector.getSelectedIndex()), callback);
        display.getNewRunStatus().setText("Creating new test run...");
      }
    });
  }

  private void loadSuiteIds(CloudCoverServiceAsync svc) {
    AsyncCallback<List<String>> callback = new AsyncCallback<List<String>>() {
      public void onFailure(Throwable caught) {
        Window.alert(caught.toString());
      }

      public void onSuccess(List<String> suiteIds) {
        // clear out the "loading" item
        display.getRunSelector().clear();
        for (String suiteId : suiteIds) {
          display.getRunSelector().addItem(suiteId);
        }
      }
    };
    svc.getAvailableSuiteIds(callback);
  }

}
