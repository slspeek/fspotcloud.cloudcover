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

import com.google.appengine.testing.cloudcover.client.model.TestStatus;
import com.google.gwt.user.client.ui.UIObject;

/**
 * Style-related utilities.
 *
 * @author Max Ross <max.ross@gmail.com>
 */
public final class StyleHelper {
  private StyleHelper() { }
  
  public static void setStatus(TestStatus testStatus, UIObject obj) {
    switch (testStatus) {
      case SUCCESS:
        obj.setStylePrimaryName("test-passed");
        break;
      case IN_PROGRESS:
        obj.setStylePrimaryName("test-in-progress");
        break;
      case FAILURE:
        obj.setStylePrimaryName("test-failed");
        break;
      case TOO_SLOW:
        obj.setStylePrimaryName("test-too-slow");
        break;
      case NOT_STARTED:
        obj.setStylePrimaryName("test-not-started");
        break;
    }
  }
}
