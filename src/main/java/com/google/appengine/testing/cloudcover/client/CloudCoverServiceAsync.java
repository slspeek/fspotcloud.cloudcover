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

import com.google.appengine.testing.cloudcover.client.model.Failure;
import com.google.appengine.testing.cloudcover.client.model.NewRunResult;
import com.google.appengine.testing.cloudcover.client.model.RunSummary;
import com.google.appengine.testing.cloudcover.client.model.Test;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

/**
 * Async version of {@link CloudCoverService}.
 *
 * @author Max Ross <max.ross@gmail.com>
 */
public interface CloudCoverServiceAsync {
  void createNewRun(String suiteId, AsyncCallback<NewRunResult> callback);
  void getRunSummary(long runId, boolean loadTestIdData, AsyncCallback<RunSummary> callback);
  void getFailure(long runId, String testId, String failureId, AsyncCallback<Failure> callback);
  void getTestById(long runId, String testId, AsyncCallback<Test> callback);
  void getAvailableSuiteIds(AsyncCallback<List<String>> callback);
}
