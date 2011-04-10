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

import com.google.appengine.testing.cloudcover.client.StyleHelper;
import com.google.appengine.testing.cloudcover.client.model.Test;
import com.google.appengine.testing.cloudcover.client.model.TestStatus;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.TreeItem;

/**
 * @author Max Ross <max.ross@gmail.com>
 */
public class TestTreeItem extends TreeItem implements HasText {

  private Test test;

  private TestTreeItem() { }

  public TestTreeItem(Test test) {
    this.test = test;
    setText(test.getName());
  }

  public void setStatus(TestStatus testStatus) {
    StyleHelper.setStatus(testStatus, this);
  }

  public long getRunId() {
    return test.getRun().getId();
  }

  public String getTestName() {
    return test.getName();
  }

  public void setTest(Test test) {
    this.test = test;
  }

  public Test getTest() {
    return test;
  }
}
