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

package com.google.appengine.testing.cloudcover.client.view;

import com.google.appengine.testing.cloudcover.client.presenter.RunSummaryPresenter;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Max Ross <max.ross@gmail.com>
 */
class RunSummaryView extends Composite implements RunSummaryPresenter.Display {
  private final VerticalPanel vPanel = new VerticalPanel();
  private final Label summary = new Label();
  private final Label numInProgress = new Label();
  private final Label numPassed = new Label();
  private final Label numFailed = new Label();
  private final Label numTooSlow = new Label();
  private final Label numNotStarted = new Label();

  RunSummaryView() {
    HorizontalPanel summaryPanel1 = new HorizontalPanel();
    vPanel.add(summaryPanel1);
    summaryPanel1.setSpacing(10);
    summaryPanel1.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

    summaryPanel1.add(summary);
    HorizontalPanel summaryPanel2 = new HorizontalPanel();
    vPanel.add(summaryPanel2);
    summaryPanel2.setSpacing(10);
    summaryPanel2.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    summaryPanel2.add(new Label("Passed: "));
    summaryPanel2.add(numPassed);
    summaryPanel2.add(new Label("Too Slow: "));
    summaryPanel2.add(numTooSlow);
    summaryPanel2.add(new Label("Failed: "));
    summaryPanel2.add(numFailed);
    summaryPanel2.add(new Label("In Progress: "));
    summaryPanel2.add(numInProgress);
    summaryPanel2.add(new Label("Not Started: "));
    summaryPanel2.add(numNotStarted);

    // don't show the summary panel until we have a run to display
    vPanel.setVisible(false);
    initWidget(vPanel);
  }

  public HasText getSummary() {
    return summary;
  }

  public Label getNumInProgress() {
    return numInProgress;
  }

  public Label getNumPassed() {
    return numPassed;
  }

  public Label getNumFailed() {
    return numFailed;
  }

  public Label getNumTooSlow() {
    return numTooSlow;
  }

  public Label getNumNotStarted() {
    return numNotStarted;
  }
}
