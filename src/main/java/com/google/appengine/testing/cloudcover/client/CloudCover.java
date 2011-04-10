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

package com.google.appengine.testing.cloudcover.client;

import com.google.appengine.testing.cloudcover.client.presenter.RunPresenter;
import com.google.appengine.testing.cloudcover.client.view.RunView;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * GWT entry point for the CloudCover UI
 *
 * @author Max Ross <max.ross@gmail.com>
 */
public class CloudCover implements EntryPoint {

  private static final String RUN_STATUS_ELEMENT_ID = "run-status";

  public void onModuleLoad() {
    CloudCoverServiceAsync svc = GWT.create(CloudCoverService.class);
    new RunPresenter(svc, new RunView(), RootPanel.get(RUN_STATUS_ELEMENT_ID));
  }
}
