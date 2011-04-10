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
package com.google.appengine.testing.cloudcover;

import com.google.appengine.testing.cloudcover.harness.junit3.JUnit3TestHarnessTest;
import com.google.appengine.testing.cloudcover.server.CloudCoverManagerTest;
import com.google.appengine.testing.cloudcover.server.DatastoreTestHarnessDAOTest;

import junit.framework.TestSuite;

/**
 * @author Max Ross <max.ross@gmail.com>
 */
public class AllTests {
  public static TestSuite suite() throws Exception {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(CloudCoverManagerTest.class);
    suite.addTestSuite(DatastoreTestHarnessDAOTest.class);
    suite.addTestSuite(JUnit3TestHarnessTest.class);
    return suite;
  }
}
