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

import com.google.appengine.testing.cloudcover.client.presenter.TestDetailsPresenter;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Max Ross <max.ross@gmail.com>
 */
class TestDetailsView extends Composite implements TestDetailsPresenter.Display {
  private final Label testName;
  private final Label numTests;
  private final Label startTime;
  private final Label endTime;
  private final Label status;
  private final VerticalPanel failurePanel;
  private final HTML failureMsg;
  private final HTML failureData;
  private final VerticalPanel detailsPanel;

  TestDetailsView() {
    detailsPanel = new VerticalPanel();
    detailsPanel.setVisible(false);

    HorizontalPanel testNamePanel = new HorizontalPanel();
    testNamePanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    detailsPanel.add(testNamePanel);
    testNamePanel.add(new Label("Test: "));
    testName = new Label();
    testNamePanel.add(testName);

    HorizontalPanel statusPanel = new HorizontalPanel();
    detailsPanel.add(statusPanel);
    statusPanel.add(new Label("Status: "));
    status = new Label();
    statusPanel.add(status);

    HorizontalPanel numTestsPanel = new HorizontalPanel();
    detailsPanel.add(numTestsPanel);
    numTestsPanel.add(new Label("Num Tests: "));
    numTests = new Label();
    numTestsPanel.add(numTests);

    HorizontalPanel startTimePanel = new HorizontalPanel();
    detailsPanel.add(startTimePanel);
    startTimePanel.add(new Label("Start Time: "));
    startTime = new Label();
    startTimePanel.add(startTime);
    
    HorizontalPanel endTimePanel = new HorizontalPanel();
    detailsPanel.add(endTimePanel);
    endTimePanel.add(new Label("End Time: "));
    endTime = new Label();
    endTimePanel.add(endTime);

    failurePanel = new VerticalPanel();
    failurePanel.setVisible(true);
    failureMsg = new HTML();
    failurePanel.add(failureMsg);
    failureData = new HTML();
    failurePanel.add(failureData);
    detailsPanel.add(failurePanel);
    detailsPanel.setVisible(false);
    initWidget(detailsPanel);
  }

  public Label getNumTests() {
    return numTests;
  }

  public Label getStartTime() {
    return startTime;
  }

  public Label getEndTime() {
    return endTime;
  }

  public Label getStatus() {
    return status;
  }

  public VerticalPanel getFailurePanel() {
    return failurePanel;
  }

  public VerticalPanel getDetailsPanel() {
    return detailsPanel;
  }

  public HTML getFailureMsg() {
    return failureMsg;
  }

  public HTML getFailureData() {
    return failureData;
  }

  public Label getTestName() {
    return testName;
  }
}
