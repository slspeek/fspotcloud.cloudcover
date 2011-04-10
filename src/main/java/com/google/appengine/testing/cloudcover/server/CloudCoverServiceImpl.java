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

import com.google.appengine.testing.cloudcover.client.CloudCoverService;
import com.google.appengine.testing.cloudcover.client.model.Failure;
import com.google.appengine.testing.cloudcover.client.model.NewRunResult;
import com.google.appengine.testing.cloudcover.client.model.RunSummary;
import com.google.appengine.testing.cloudcover.client.model.Test;
import com.google.appengine.testing.cloudcover.spi.TestHarnessConfig;
import com.google.appengine.testing.cloudcover.spi.TestId;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Max Ross <max.ross@gmail.com>
 */
public class CloudCoverServiceImpl extends RemoteServiceServlet implements CloudCoverService {

  private static final String CONFIG_CLASS_PROPERTY = "cloudcover.config.class";
  private static final String DAO_CLASS_PROPERTY = "cloudcover.dao.class";

  private final Logger logger = Logger.getLogger(getClass().getName());

  private CloudCoverManager cloudCoverManager;

  @Override
  public void init(ServletConfig servletConfig) throws ServletException {
    super.init(servletConfig);
    cloudCoverManager = createTestHarnessManager(servletConfig);
  }

  private TestHarnessConfig createTestHarnessConfig(ServletConfig servletConfig)
      throws ServletException {
    String configClass = servletConfig.getInitParameter(CONFIG_CLASS_PROPERTY);
    if (configClass == null) {
      throw new ServletException(CONFIG_CLASS_PROPERTY + " is a required init parameter");
    }

    try {
      return (TestHarnessConfig) Class.forName(configClass).newInstance();
    } catch (Exception e) {
      throw new ServletException(e);
    }
  }

  private CloudCoverManager createTestHarnessManager(ServletConfig servletConfig) throws ServletException {
    TestHarnessConfig harnessConfig = createTestHarnessConfig(servletConfig);
    String daoClass = servletConfig.getInitParameter(DAO_CLASS_PROPERTY);
    TestHarnessDao dao;
    if (daoClass == null) {
      dao = new DatastoreTestHarnessDao(harnessConfig);
    } else {
      try {
        dao = (TestHarnessDao) Class.forName(daoClass).newInstance();
      } catch (Exception e) {
        throw new ServletException(e);
      }
    }
    return new CloudCoverManager(harnessConfig, dao);
  }

  public NewRunResult createNewRun(String suiteId) {
    long start = System.currentTimeMillis();
    try {
      return cloudCoverManager.createNewRun(suiteId);
    } finally {
      long duration = System.currentTimeMillis() - start;
      logger.fine("Created a run for suite " + suiteId + " in " + duration + "ms.");
    }
  }

  public RunSummary getRunSummary(long runId, boolean loadTestIdData) {
    long start = System.currentTimeMillis();
    try {
      return cloudCoverManager.getRunSummary(runId, loadTestIdData);
    } finally {
      long duration = System.currentTimeMillis() - start;
      logger.fine("Fetched status for run " + runId + " in " + duration + "ms.");
    }
  }

  public Failure getFailure(long runId, String testId, String failureId) {
    long start = System.currentTimeMillis();
    try {
      return cloudCoverManager.getFailure(runId, testId, failureId);
    } finally {
      long duration = System.currentTimeMillis() - start;
      logger.fine("Fetched failure " + failureId + " for test " + testId + " in " + duration + "ms.");
    }
  }

  public Test getTestById(long runId, String testId) {
    long start = System.currentTimeMillis();
    try {
      return cloudCoverManager.getTestById(runId, testId);
    } finally {
      long duration = System.currentTimeMillis() - start;
      logger.fine("Fetched test " + testId + " in " + duration + "ms.");
    }
  }

  public List<String> getAvailableSuiteIds() {
    long start = System.currentTimeMillis();
    try {
      return cloudCoverManager.getAvailableSuiteIds();
    } finally {
      long duration = System.currentTimeMillis() - start;
      logger.fine("Fetched available suite ids in " + duration + "ms.");            
    }
  }

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    if (req.getMethod().toUpperCase().equals("POST") &&
        req.getRequestURI().startsWith(
            cloudCoverManager.getHarnessConfig().getBaseQueueActionURL())) {
      handleQueueAction(req, resp);
    } else {
      super.service(req, resp);
    }
  }

  private void handleQueueAction(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    resp.setContentType("text/html");
    String[] components = req.getRequestURI().substring(1).split("/");
    if (components.length < 4) {
      throw new ServletException("invalid url");
    }
    if (components[2].equals("runtest")) {
      if (components.length < 6) {
        throw new ServletException("test to run not available");
      }
      long runId = Long.parseLong(components[3]);
      String testId = components[4];
      int numTests = Integer.valueOf(components[5]);
      cloudCoverManager.runTest(runId, new TestId(testId, numTests));
    } else if (components[2].equals("completionNotification")) {
      long runId = Long.parseLong(components[3]);
      cloudCoverManager.doCompletionCheck(
          runId, extractServerURL(req.getRequestURL().toString(), req.getRequestURI()));
    } else if (components[2].equals("testIdData")) {
      long runId = Long.parseLong(components[3]);
      cloudCoverManager.newTestIdData(runId, extractTestIdData(req));
    }
  }

  private Map<String, Integer> extractTestIdData(HttpServletRequest req) throws IOException {
    ObjectInputStream ois = new ObjectInputStream(req.getInputStream());
    try {
      return (Map<String, Integer>) ois.readObject();
    } catch (ClassNotFoundException e) {
      throw new IOException(e);
    }
  }

  static String extractServerURL(String requestURL, String requestURI) {
    return requestURL.substring(0, requestURL.indexOf(requestURI));
  }

}
