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

import junit.framework.TestCase;

/**
 * @author Max Ross <max.ross@gmail.com>
 */
public class CloudCoverManagerTest extends TestCase {

  public void testBuildRunStatusURL() {
    assertEquals("blablabla/cloudcover.html#33", CloudCoverManager.buildRunStatusURL("blablabla", 33));
  }

  public void testBuildFanInKey() {
    for (int i = 0; i < 1000; i++) {
      assertEquals("23-0", CloudCoverManager.buildFanInKey(23, i));
    }
    for (int i = 1000; i < 2000; i++) {
      assertEquals("23-1", CloudCoverManager.buildFanInKey(23, i));
    }
  }
}
