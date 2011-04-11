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

import com.google.appengine.testing.cloudcover.client.presenter.RunPresenter;
import com.google.appengine.testing.cloudcover.client.presenter.RunSelectionPresenter;
import com.google.appengine.testing.cloudcover.client.presenter.RunSummaryPresenter;
import com.google.appengine.testing.cloudcover.client.presenter.TestDetailsPresenter;
import com.google.appengine.testing.cloudcover.client.presenter.TestTreePresenter;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HorizontalSplitPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Max Ross <max.ross@gmail.com>
 */
public class RunView extends Composite implements RunPresenter.Display {
  private final TestDetailsView testDetailsView;
  private final RunSummaryView runSummaryView;
  private final RunSelectionView runSelectionView;
  private final TestTreeView testTreeView;
  private final Label errorMsg = new Label();

  public RunView() {
    HorizontalSplitPanel split = new HorizontalSplitPanel();
    split.setSplitPosition("50%");
    testTreeView = new TestTreeView();
    split.add(testTreeView);
    testDetailsView = new TestDetailsView();
    split.add(testDetailsView);

    runSelectionView = new RunSelectionView();
    runSummaryView = new RunSummaryView();

    FlowPanel mainPanel = new FlowPanel();
    mainPanel.add(runSelectionView);
    mainPanel.add(runSummaryView);
    mainPanel.add(split);
    initWidget(mainPanel);
  }

  public Widget asWidget() {
    return this;
  }

  public RunSummaryPresenter.Display getRunSummaryView() {
    return runSummaryView;
  }

  public TestTreePresenter.Display getTestTreeView() {
    return testTreeView;
  }

  public TestDetailsPresenter.Display getTestDetailsView() {
    return testDetailsView;
  }

  public RunSelectionPresenter.Display getRunSelectionView() {
    return runSelectionView;
  }

  public HasText getErrorMsg() {
    return errorMsg;
  }
}
