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
package com.google.appengine.testing.cloudcover.client.model;

import java.io.Serializable;

/**
 * @author Max Ross <max.ross@gmail.com>
 */
public class FailureData implements Serializable {

  private String shortName;
  private String failureMsg;
  private String data;

  private FailureData() {}
  
  public FailureData(String shortName, String failureMsg, String data) {
    if (shortName == null) {
      throw new NullPointerException("shortName cannot be null");
    }
    if (failureMsg == null) {
      throw new NullPointerException("failureMsg cannot be null");
    }
    if (data == null) {
      throw new NullPointerException("data cannot be null");
    }
    this.shortName = shortName;
    this.failureMsg = failureMsg;
    this.data = data;
  }

  public String getShortName() {
    return shortName;
  }

  public String getFailureMsg() {
    return failureMsg;
  }

  public String getData() {
    return data;
  }
}
