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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.testing.cloudcover.client.model.Failure;
import com.google.appengine.testing.cloudcover.client.model.Run;
import com.google.appengine.testing.cloudcover.client.model.Test;
import com.google.appengine.testing.cloudcover.client.model.TestStatus;
import com.google.appengine.testing.cloudcover.spi.TestHarness;
import com.google.appengine.testing.cloudcover.spi.TestHarnessConfig;
import com.google.appengine.testing.cloudcover.spi.TestId;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Max Ross <max.ross@gmail.com>
 */
public class DatastoreTestHarnessDao implements TestHarnessDao {

  private final DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

  private final TestHarnessConfig config;

  public DatastoreTestHarnessDao(TestHarnessConfig config) {
    this.config = config;
  }

  public Run newRun() {
    Entity runEntity = new Entity(getRunEntityKind());
    TestHarness harness = config.getTestHarness();
    runEntity.setUnindexedProperty("testRunnerConfigClass", harness.getClass().getName());
    Date created = new Date();
    runEntity.setUnindexedProperty("created", created);
    // -1 is an indication that we don't know the number of tests or test entities yet
    runEntity.setUnindexedProperty("numTests", -1);
    runEntity.setUnindexedProperty("numTestEntities", -1);
    Key key = ds.put(runEntity);
    return new Run(key.getId(), harness.getClass().getName(), created, null);
  }

  public void updateRun(Run run) {
    try {
      Entity runEntity = ds.get(KeyFactory.createKey(getRunEntityKind(), run.getId()));
      // num tests and numTestEntities are the only mutable fields
      runEntity.setUnindexedProperty("numTests", run.getNumTests());
      runEntity.setUnindexedProperty("numTestEntities", run.getNumTestEntities());
      ds.put(runEntity);
    } catch (EntityNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private Key buildTestIdDataKey(long runId) {
    Key runKey = buildRunKey(runId);
    return KeyFactory.createKey(runKey, getTestIdDataKind(), 1);
  }

  public Run getRunById(long runId, boolean loadTestIdData) {
    Map<String, Integer> testIdsToTestCounts = null;
    if (loadTestIdData) {
      testIdsToTestCounts = getTestIdData(runId);
    }
    try {
      return entityToRun(ds.get(buildRunKey(runId)), testIdsToTestCounts);
    } catch (EntityNotFoundException e) {
      return null;
    }
  }

  public Test newTest(Run run, TestId testId) throws TestAlreadyExistsException {
    Transaction txn = ds.beginTransaction();
    try {
      Test existingTest = getTestById(run.getId(), testId.getTestId());
      if (existingTest == null) {
        Test test = new Test(testId.getTestId(), run, testId.getNumSubtests());
        ds.put(testToEntity(test));
        txn.commit();
        return test;
      } else {
        // the test was already created - this will happen when an exception
        // is thrown during test cleanup
        throw new TestAlreadyExistsException(existingTest);
      }
    } finally {
      if (txn.isActive()) {
        txn.rollback();
      }
    }
  }

  public void updateTest(Test test) {
    Transaction txn = ds.beginTransaction();
    try {
      if (!test.getFailures().isEmpty()) {
        List<Entity> failureEntities = failuresToEntities(test);
        ds.put(txn, failureEntities);
      }
      ds.put(txn, testToEntity(test));
      txn.commit();
    } finally {
      if (txn.isActive()) {
        txn.rollback();
      }
    }
  }

  private Query createTestsForRunQuery(long runId) {
    // we're relying on the Run id being prepended to the testId for this
    // query.  Not the most straightforward way to get this information
    // but we can't issue any queries the require indexes to be built and
    // filtering by key range can be done entirely with built-in indexes.
    Query failureForRunQuery = new Query(getTestEntityKind());
    failureForRunQuery.addFilter(
        Entity.KEY_RESERVED_PROPERTY,
        Query.FilterOperator.GREATER_THAN,
        KeyFactory.createKey(getTestEntityKind(), "Run " + runId));
    failureForRunQuery.addFilter(
        Entity.KEY_RESERVED_PROPERTY,
        Query.FilterOperator.LESS_THAN,
        KeyFactory.createKey(getTestEntityKind(), "Run " + (runId + 1)));
    return failureForRunQuery;
  }

  public List<Test> getTestsForRun(long runId) {
    PreparedQuery pq = ds.prepare(createTestsForRunQuery(runId));
    List<Test> result = new ArrayList<Test>();
    for (Entity e : pq.asIterable(FetchOptions.Builder.withPrefetchSize(500))) {
      result.add(entityToTest(e));
    }
    return result;
  }

  public boolean runIsComplete(long runId) {
    Query query = createTestsForRunQuery(runId);
    int count = 0;
    for (Entity testEntity : ds.prepare(query).asIterable()) {
      Test test = entityToTest(testEntity);
      count++;
      // any test that hasn't completed means the the run is not complete
      if (test.getStatus() == TestStatus.IN_PROGRESS) {
        return false;
      }
    }
    // no in-progres tests but there may be tests that haven't started at all
    // we'll need to issue another query to see if that's the case
    return count == getRunById(runId, false).getNumTestEntities();
  }

  private List<Entity> failuresToEntities(Test test) {
    List<Entity> entities = new ArrayList<Entity>();
    for (Failure f : test.getFailures()) {
      entities.add(failureToEntity(test, f));
    }
    return entities;
  }

  private Key buildFailureKey(Test test, Failure f) {
    Key testKey = buildTestKey(test);
    return buildFailureKey(testKey, f.getId());
  }

  private Key buildFailureKey(Key testKey, String failureId) {
    return KeyFactory.createKey(testKey, getFailureEntityKind(), failureId);
  }

  private Entity failureToEntity(Test test, Failure f) {
    Key failureKey = buildFailureKey(test, f);
    Entity e = new Entity(failureKey);
    String failureMsg = f.getFailureMsg().length() > 500 ?
                        f.getFailureMsg().substring(0, 500) : f.getFailureMsg();
    e.setUnindexedProperty("failureMsgShort", failureMsg);
    e.setUnindexedProperty("failureMsgFull", new Text(f.getFailureMsg()));
    String failureData = f.getFailureData().length() > 500 ?
                        f.getFailureData().substring(0, 500) : f.getFailureData();
    e.setUnindexedProperty("failureDataShort", failureData);
    e.setUnindexedProperty("failureDataFull", new Text(f.getFailureData()));
    e.setUnindexedProperty("runKey", buildRunKey(test.getRun().getId()));
    return e;
  }

  private Key buildTestKey(Test test) {
    return buildTestKey(test.getRun().getId(), test.getName());
  }

  private Key buildTestKey(long runId, String testId) {
    return KeyFactory.createKey(getTestEntityKind(), "Run " + runId + ":" + testId);
  }

  private Key buildRunKey(long runId) {
    return KeyFactory.createKey(getRunEntityKind(), runId);
  }

  private static Run entityToRun(Entity e, Map<String, Integer> testIdsToTestCounts) {
    Date created = (Date) e.getProperty("created");
    Run run = new Run(e.getKey().getId(),
                      (String) e.getProperty("testRunnerConfigClass"), created, testIdsToTestCounts);
    run.setNumTests(((Long) e.getProperty("numTests")).intValue());
    run.setNumTestEntities(((Long) e.getProperty("numTestEntities")).intValue());
    return run;
  }

  private Test entityToTest(Entity e) {
    String testName = (String) e.getProperty("testName");
    Key runKey = (Key) e.getProperty("runKey");
    long numTests = (Long) e.getProperty("numTests");
    Run run = new Run(runKey.getId(), null, null, null);
    Test t = new Test(testName, run, numTests);
    t.setStartTime((Date) e.getProperty("startTime"));
    t.setEndTime((Date) e.getProperty("endTime"));
    t.setStatus(TestStatus.valueOf((String) e.getProperty("status")));
    List<Key> failureKeys = (List<Key>) e.getProperty("failureKeys");
    if (failureKeys != null) {
      for (Key failureKey : failureKeys) {
        Failure f = new Failure(failureKey.getName(), null, null);
        t.getFailures().add(f);
      }
    }

    List<String> successNames = (List<String>) e.getProperty("successes");
    if (successNames != null) {
      t.getSuccesses().addAll(successNames);
    }
    return t;
  }

  private Entity testToEntity(Test t) {
    Entity testEntity = new Entity(buildTestKey(t));
    testEntity.setUnindexedProperty("testName", t.getName());
    testEntity.setUnindexedProperty("startTime", t.getStartTime());
    if (t.getEndTime() == null) {
      testEntity.removeProperty("endTime");
      testEntity.removeProperty("durationInMs");
    } else {
      testEntity.setUnindexedProperty("endTime", t.getEndTime());
      testEntity.setUnindexedProperty("durationInMs", t.getEndTime().getTime() - t.getStartTime().getTime());
    }
    testEntity.setUnindexedProperty("status", t.getStatus().name());
    testEntity.setUnindexedProperty("numTests", t.getNumTests());
    if (t.getFailures().isEmpty()) {
      testEntity.removeProperty("failureKeys");
    } else {
      List<Key> failureKeys = new ArrayList<Key>();
      for (Failure f : t.getFailures()) {
        failureKeys.add(buildFailureKey(t, f));
      }
      testEntity.setUnindexedProperty("failureKeys", failureKeys);
    }
    if (t.getSuccesses().isEmpty()) {
      testEntity.removeProperty("successes");
    } else {
      List<Key> failureKeys = new ArrayList<Key>();
      for (Failure f : t.getFailures()) {
        failureKeys.add(buildFailureKey(t, f));
      }
      testEntity.setUnindexedProperty("successes", t.getSuccesses());
    }
    testEntity.setUnindexedProperty("runKey", buildRunKey(t.getRun().getId()));
    return testEntity;
  }

  public boolean createCompletionRecordIfNotAlreadyPresent(long runId) {
    Key key = KeyFactory.createKey(getCompletionNotificationEntityKind(), Long.valueOf(runId).toString());
    // We'll do a fetch by Key in a txn so we can guarantee that we only write
    // a single completion record
    Transaction txn = ds.beginTransaction();
    try {
      ds.get(key);
      // Entity already exists otherwise there would have been an exception so
      // return false to indicate that nothing needs to be done.
      return false;
    } catch (EntityNotFoundException enfe) {
      // Entity doesn't already exist so create it.
      // The Entity doesn't have any data, we just use it as a lock that
      // prevents more than one completion notification from being sent.
      Entity entity = new Entity(key);
      ds.put(entity);
      try {
        txn.commit();
      } catch (ConcurrentModificationException cme) {
        // Somebody create the entity during our txn.
        // That's fine, it just means we return false;
        return false;
      }
      return true;
    } finally {
      if (txn.isActive()) {
        txn.rollback();
      }
    }
  }

  public Failure getFailure(long runId, String testId, String failureId) {
    Key failureKey = buildFailureKey(buildTestKey(runId, testId), failureId);
    try {
      Entity e = ds.get(failureKey);
      return entityToFailure(e);
    } catch (EntityNotFoundException e1) {
      return null;
    }
  }

  private Failure entityToFailure(Entity e) {
    String failureMsg = ((Text) e.getProperty("failureMsgFull")).getValue();
    String failureData = ((Text) e.getProperty("failureDataFull")).getValue();
    return new Failure(e.getKey().getName(), failureMsg, failureData);
  }

  public Test getTestById(long runId, String testId) {
    Key testKey = buildTestKey(runId, testId);
    try {
      Entity e = ds.get(testKey);
      return entityToTest(e);
    } catch (EntityNotFoundException e1) {
      // test hasn't started yet
      return null;
    }
  }

  private Map<String, Integer> getTestIdData(long runId) {
    Map<String, Integer> testIdsToTestCounts = null;
    try {
      Entity testIdData = ds.get(buildTestIdDataKey(runId));
      testIdsToTestCounts = entityToTestIdData(testIdData);
    } catch (EntityNotFoundException e) {
      // not available yet, no big deal
    }
    return testIdsToTestCounts;
  }

  Map<String, Integer> entityToTestIdData(Entity testIdData) {
    Map<String, Integer> testIdsToTestCounts = new HashMap<String, Integer>();
    int curIndex = 0;
    while (testIdData.hasProperty("data" + curIndex)) {
      List<Object> data = (List<Object>) testIdData.getProperty("data" + curIndex);
      curIndex++;
      String testId = null;
      for (Object obj : data) {
        if (testId == null) {
          testId = (String) obj;
        } else {
          testIdsToTestCounts.put(testId, ((Long) obj).intValue());
          testId = null;
        }
      }
    }
    return testIdsToTestCounts;
  }

  public void addRunTestIdData(long runId, Map<String, Integer> testIdsToTestCounts) {
    ds.put(testIdDataToEntity(runId, testIdsToTestCounts));
  }

  Entity testIdDataToEntity(long runId, Map<String, Integer> testIdsToTestCounts) {
    Key key = buildTestIdDataKey(runId);
    Entity entity = new Entity(key);
    int curIndex = 0;
    List<Object> curData = new ArrayList<Object>();
    for (Map.Entry<String, Integer> entry : testIdsToTestCounts.entrySet()) {
      curData.add(entry.getKey());
      curData.add(entry.getValue());
      if (curData.size() == 500) {
        entity.setProperty("data" + curIndex, curData);
        curData = new ArrayList<Object>();
        curIndex++;
      }
    }
    if (!curData.isEmpty()) {
      entity.setProperty("data" + curIndex, curData);
    }
    return entity;
  }

  protected String getTestIdDataKind() {
    return "CloudCoverRunTestIdData";
  }

  protected String getRunEntityKind() {
    return "CloudCoverRun";
  }

  protected String getTestEntityKind() {
    return "CloudCoverTest";
  }

  protected String getFailureEntityKind() {
    return "CloudCoverFailure";
  }

  protected String getCompletionNotificationEntityKind() {
    return "CloudCoverCompletionNotification";
  }
}
