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
public class Failure implements Serializable {

  // id is only unique within the test that owns it
  private String id;
  private String failureMsg;
  private String failureData;

  private Failure() { }

  public Failure(String id, String failureMsg, String failureData) {
    this.id = id;
    this.failureMsg = failureMsg;
    this.failureData = failureData;
  }

  public String getId() {
    return id;
  }

  public String getFailureMsg() {
    return failureMsg;
  }

  public String getFailureData() {
    return failureData;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Failure failure = (Failure) o;

    if (id != null ? !id.equals(failure.id) : failure.id != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }
}
