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

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Max Ross <max.ross@gmail.com>
 */
public class DatastoreTestHarnessDAOTest extends TestCase {
  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
      new LocalDatastoreServiceTestConfig()
  );

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    helper.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    helper.tearDown();
    super.tearDown();
  }

  public void testEntityToTestIdData() {
    DatastoreTestHarnessDao dao = new DatastoreTestHarnessDao(null);
    Entity entity = new Entity("yar");
    assertTrue(dao.entityToTestIdData(entity).isEmpty());

    entity.setProperty("data0", Arrays.asList("1", 1L));
    Map<String, Integer> result = dao.entityToTestIdData(entity);
    assertEquals(1, result.size());
    assertEquals(Integer.valueOf(1), result.get("1"));

    entity.setProperty("data0", Arrays.asList("1", 1L, "2", 2L));
    result = dao.entityToTestIdData(entity);
    assertEquals(2, result.size());
    assertEquals(Integer.valueOf(1), result.get("1"));
    assertEquals(Integer.valueOf(2), result.get("2"));

    entity.setProperty("data1", Arrays.asList("3", 3L, "4", 4L));
    result = dao.entityToTestIdData(entity);
    assertEquals(4, result.size());
    assertEquals(Integer.valueOf(1), result.get("1"));
    assertEquals(Integer.valueOf(2), result.get("2"));
    assertEquals(Integer.valueOf(3), result.get("3"));
    assertEquals(Integer.valueOf(4), result.get("4"));
  }

  public void testTestIdDataToEntity() {
    DatastoreTestHarnessDao dao = new DatastoreTestHarnessDao(null);
    Map<String, Integer> map = new HashMap<String, Integer>();

    Entity e = dao.testIdDataToEntity(23, map);
    assertEquals(1, e.getKey().getId());
    assertEquals(23, e.getParent().getId());
    assertEquals(0, e.getProperties().size());

    for (int i = 0; i < 249; i++) {
      map.put("id" + i, i);
    }
    e = dao.testIdDataToEntity(23, map);
    assertEquals(1, e.getProperties().size());
    List<Object> data = (List<Object>) e.getProperty("data0");
    assertEquals(498, data.size());

    map.put("id249", 249);
    e = dao.testIdDataToEntity(23, map);
    assertEquals(1, e.getProperties().size());
    data = (List<Object>) e.getProperty("data0");
    assertEquals(500, data.size());

    map.put("id250", 250);
    e = dao.testIdDataToEntity(23, map);
    assertEquals(2, e.getProperties().size());
    data = (List<Object>) e.getProperty("data0");
    assertEquals(500, data.size());
    data = (List<Object>) e.getProperty("data1");
    assertEquals(2, data.size());
  }
}
