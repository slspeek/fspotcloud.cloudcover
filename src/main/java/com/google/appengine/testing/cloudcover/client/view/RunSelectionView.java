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

import com.google.appengine.testing.cloudcover.client.presenter.RunSelectionPresenter;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Max Ross <max.ross@gmail.com>
 */
public class RunSelectionView extends Composite implements RunSelectionPresenter.Display {

  private final Button newRunButton;
  private final ListBox runSelector;
  private final Label newRunStatus = new Label();

  public RunSelectionView() {
    VerticalPanel vPanel = new VerticalPanel();
    HorizontalPanel hPanel = new HorizontalPanel();
    hPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    hPanel.setSpacing(10);
    vPanel.add(hPanel);
    hPanel.add(new Label("Available Suites"));
    runSelector = new ListBox();
    runSelector.addItem("Loading...");
    runSelector.setVisibleItemCount(1);
    hPanel.add(runSelector);
    newRunButton = new Button("Start New Run");
    newRunButton.setVisible(true);
    hPanel.add(newRunButton);
    hPanel.add(newRunStatus);
    initWidget(vPanel);
  }

  public Button getNewRunButton() {
    return newRunButton;
  }

  public ListBox getRunSelector() {
    return runSelector;
  }

  public Label getNewRunStatus() {
    return newRunStatus;
  }
}
