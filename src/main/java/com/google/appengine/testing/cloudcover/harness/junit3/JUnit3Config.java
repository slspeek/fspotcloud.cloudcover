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
package com.google.appengine.testing.cloudcover.harness.junit3;

import com.google.appengine.testing.cloudcover.spi.BaseTestHarnessConfig;
import com.google.appengine.testing.cloudcover.spi.TestHarness;

/**
 * Base config for JUnit 3.
 *
 * @author Max Ross <max.ross@gmail.com>
 */
public abstract class JUnit3Config extends BaseTestHarnessConfig {

  public TestHarness getTestHarness() {
    return new JUnit3TestHarness();
  }
}
