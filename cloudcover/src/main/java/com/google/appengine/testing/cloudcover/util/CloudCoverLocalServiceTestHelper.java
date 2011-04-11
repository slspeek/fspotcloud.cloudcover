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
package com.google.appengine.testing.cloudcover.util;

import com.google.appengine.tools.development.testing.LocalServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.apphosting.api.ApiProxy;

/**
 * Drop-in replacement for {@link LocalServiceTestHelper} that turns into a
 * no-op when it sees that the execution environment has already been
 * initialized (in the dev appserver or prod for example).
 * Use instances of this class instead of
 * {@link LocalServiceTestHelper} if you want to run your tests in the cloud.
 * <br>
 * Tests that access or mutate the installed {@link ApiProxy.Delegate} via
 * {@link ApiProxy#setDelegate(com.google.apphosting.api.ApiProxy.Delegate)}
 * and {@link com.google.apphosting.api.ApiProxy#getDelegate()} should instead
 * use {@link #setDelegate(com.google.apphosting.api.ApiProxy.Delegate)} and
 * {@link #getDelegate()} since the versions of these methods defined in this
 * class use a {@link ThreadLocalDelegate} under the hood to keep custom
 * {@link ApiProxy.Delegate} implementations from leaking to other tests
 * running concurrently.
 *
 * @author Max Ross <max.ross@gmail.com>
 */
public class CloudCoverLocalServiceTestHelper {

  /**
   * This is a {@link LocalServiceTestHelper} but we may not have that class
   * available at runtime.
   */
  private final Object helper;

  // initialized lazily because we it requires the current Delegate
  // as a ctor arg and that might not be available when the class loads
  private static ThreadLocalDelegate THREAD_LOCAL_DELEGATE;

  /**
   * @see LocalServiceTestHelper#LocalServiceTestHelper(com.google.appengine.tools.development.testing.LocalServiceTestConfig...)
   */
  public CloudCoverLocalServiceTestHelper(LocalServiceTestConfig... configs) {
    if (ApiProxy.getCurrentEnvironment() == null) {
      helper = new LocalServiceTestHelper(configs);
    } else {
      helper = null;
    }
  }

  /**
   * @see LocalServiceTestHelper#setUp()
   */
  public void setUp() {
    if (helper != null) {
      ((LocalServiceTestHelper) helper).setUp();
    } else {
      synchronized (getClass()) {
        // install a delegate that supports thread-local customizations
        // if not already installed
        if (!(ApiProxy.getDelegate() instanceof ThreadLocalDelegate)) {
          THREAD_LOCAL_DELEGATE = new ThreadLocalDelegate(ApiProxy.getDelegate());
          ApiProxy.setDelegate(THREAD_LOCAL_DELEGATE);
        }
      }
    }
  }

  /**
   * @see LocalServiceTestHelper#tearDown()
   */
  public void tearDown() {
    if (helper != null) {
      ((LocalServiceTestHelper) helper).tearDown();
    }
    synchronized (getClass()) {
      if (THREAD_LOCAL_DELEGATE != null) {
        // clear out any delegates that were set during the test
        THREAD_LOCAL_DELEGATE.clearDelegateForThread();
      }
    }
  }

  /**
   * Installs the provided {@link ApiProxy.Delegate}.  If the currently
   * {@link ApiProxy.Delegate} is a {@link ThreadLocalDelegate} then the
   * provided {@link ApiProxy.Delegate} is installed only for the current
   * thread.  Otherwise the provided {@link ApiProxy.Delegate} is installed
   * globally.
   */
  public static synchronized void setDelegate(ApiProxy.Delegate delegate) {
    if (THREAD_LOCAL_DELEGATE == null) {
      ApiProxy.setDelegate(delegate);
    } else {
      CloudCoverLocalServiceTestHelper.THREAD_LOCAL_DELEGATE.setDelegateForThread(delegate);
    }
  }

  /**
   * Provides access to the currently installed {@link ApiProxy.Delegate}.
   * This method should be used in place of
   * {@link com.google.apphosting.api.ApiProxy#getDelegate()}
   */
  public static synchronized ApiProxy.Delegate getDelegate() {
    if (THREAD_LOCAL_DELEGATE == null) {
      return ApiProxy.getDelegate();
    }
    return THREAD_LOCAL_DELEGATE.getDelegate();
  }
}
