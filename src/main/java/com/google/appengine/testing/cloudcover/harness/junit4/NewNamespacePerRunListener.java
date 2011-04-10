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
package com.google.appengine.testing.cloudcover.harness.junit4;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.testing.cloudcover.client.model.Test;

import org.junit.runner.Description;
import org.junit.runner.Result;

/**
 * @author Max Ross <max.ross@gmail.com>
 */
final class NewNamespacePerRunListener extends BaseRunListener {

  private final Test gaeTest;

  public NewNamespacePerRunListener(Test gaeTest) {
    this.gaeTest = gaeTest;
  }

  @Override
  public void testRunFinished(Result result) {
    NamespaceManager.set("");
    super.testRunFinished(result);
  }

  @Override
  public void testRunStarted(Description description) {
    super.testRunStarted(description);
    String namespace =
        String.format("%s_%s", gaeTest.getRun().getId(), description.getDisplayName());
    NamespaceManager.set(namespace);
  }
}

