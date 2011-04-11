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
package com.google.appengine.testing.cloudcover.server;

import com.google.appengine.testing.cloudcover.client.model.Failure;
import com.google.appengine.testing.cloudcover.client.model.Run;
import com.google.appengine.testing.cloudcover.client.model.Test;
import com.google.appengine.testing.cloudcover.spi.TestId;

import java.util.List;
import java.util.Map;

/**
 * @author Max Ross <max.ross@gmail.com>
 */
public interface TestHarnessDao {

  /**
   * Creates a new Run in the datastore
   */
  Run newRun();

  /**
   * Updates an existing Run in the datastore
   */
  void updateRun(Run run);

  /**
   * Loads a Run from the datastore with optional test id data
   *
   * @param runId The unique id of the Run to load
   * @param loadTestIdData Whether or not to load test id data along with the
   * Run
   * @return The Run uniquely identified by the given runId, or {@code null} if
   * no Run could be found.
   */
  Run getRunById(long runId, boolean loadTestIdData);

  /**
   * Creates a new Test in the datastore
   *
   * @param run The Run to which the Test belongs
   * @param testId The unique id of the Test to create
   * @return The newly created Test
   * @throws TestAlreadyExistsException If a Test with the provided id already
   * exists
   */
  Test newTest(Run run, TestId testId) throws TestAlreadyExistsException;

  /**
   * Updates an existing Test in the datastore
   */
  void updateTest(Test test);

  /**
   * Return all Tests associated with the Run uniquely identified by the given
   * runId.
   */
  List<Test> getTestsForRun(long runId);

  /**
   * @return {@code true} if the completion record was created, {@code false}
   * otherwise.
   */
  boolean createCompletionRecordIfNotAlreadyPresent(long runId);

  /**
   * Returns the Failure uniquely identified by the given runId, testId,
   * and failureId, or {@code null} if no such Failure exists.
   */
  Failure getFailure(long runId, String testId, String failureId);

  /**
   * Returns the Test uniquely identified by the given runId and testId, or
   * {@code null} if no such Test exists.
   */
  Test getTestById(long runId, String testId);

  /**
   * Returns {@code true} if the run is finished, {@code false} otherwise.
   */
  boolean runIsComplete(long runId);

  /**
   * Writes test id data to the datastore. 
   */
  void addRunTestIdData(long runId, Map<String, Integer> testIdsToTestCounts);
}
