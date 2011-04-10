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

import com.google.appengine.testing.cloudcover.client.presenter.TestTreePresenter;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Tree;

/**
 * @author Max Ross <max.ross@gmail.com>
 */
class TestTreeView extends Composite implements TestTreePresenter.Display {
  private final Tree tree;

  TestTreeView() {
    tree = new Tree();
    initWidget(tree);
  }

  public Tree getTree() {
    return tree;
  }
}
