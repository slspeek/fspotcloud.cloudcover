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
import com.google.appengine.testing.cloudcover.client.model.Failure;
import com.google.appengine.testing.cloudcover.client.model.Test;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Max Ross <max.ross@gmail.com>
 */
public class TestDetailsPresenter extends Composite {

  private final Display display;

  public TestDetailsPresenter(Display display) {
    this.display = display;
  }

  public void setTest(Test test) {
    if (test != null) {
      display.getDetailsPanel().setVisible(true);
      display.getTestName().setText(test.getName());
      display.getStatus().setText(test.getStatus().name());
      StyleHelper.setStatus(test.getStatus(), display.getStatus());
      display.getNumTests().setText(String.valueOf(test.getNumTests()));
      DateTimeFormat df = DateTimeFormat.getMediumDateTimeFormat();
      display.getStartTime().setText(df.format(test.getStartTime()));
      String end = test.getEndTime() == null ? "" : df.format(test.getEndTime());
      display.getEndTime().setText(end);
    } else {
      display.getDetailsPanel().setVisible(false);
    }
  }

  public void setFailure(Failure result) {
    if (result != null) {
      display.getFailurePanel().setVisible(true);
      display.getFailureMsg().setHTML(result.getFailureMsg());
      display.getFailureData().setHTML(result.getFailureData());
    } else {
      display.getFailurePanel().setVisible(false);
    }
  }

  public interface Display {
    HasText getTestName();
    HasText getNumTests();
    HasText getStartTime();
    HasText getEndTime();
    Label getStatus();
    VerticalPanel getFailurePanel();
    VerticalPanel getDetailsPanel();
    HTML getFailureMsg();
    HTML getFailureData();
  }
}
