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
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.util.List;

/**
 * Defines the API through which the GWT UI communicates with the server.
 *
 * @author Max Ross <max.ross@gmail.com>
 */
@RemoteServiceRelativePath("cloudcover")
public interface CloudCoverService extends RemoteService {

  /**
   * @see com.google.appengine.testing.cloudcover.server.CloudCoverManager#createNewRun(String)
   */
  NewRunResult createNewRun(String suiteId);

  /**
   * @see com.google.appengine.testing.cloudcover.server.CloudCoverManager#getRunSummary(long, boolean)  
   */
  RunSummary getRunSummary(long runId, boolean loadTestIdData);

  /**
   * @see com.google.appengine.testing.cloudcover.server.CloudCoverManager#getTestById(long, String)
   */
  Test getTestById(long runId, String testId);

  /**
   * @see com.google.appengine.testing.cloudcover.server.CloudCoverManager#getFailure(long, String, String)
   */
  Failure getFailure(long runId, String testId, String failureId);


  /**
   * @see com.google.appengine.testing.cloudcover.server.CloudCoverManager#getAvailableSuiteIds()
   */
  List<String> getAvailableSuiteIds();
}
