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
package com.google.appengine.testing.cloudcover.client.presenter;

import com.google.appengine.testing.cloudcover.client.CloudCoverServiceAsync;
import com.google.appengine.testing.cloudcover.client.StyleHelper;
import com.google.appengine.testing.cloudcover.client.model.Failure;
import com.google.appengine.testing.cloudcover.client.model.Run;
import com.google.appengine.testing.cloudcover.client.model.Test;
import com.google.appengine.testing.cloudcover.client.model.TestStatus;
import com.google.appengine.testing.cloudcover.client.view.SubTestTreeItem;
import com.google.appengine.testing.cloudcover.client.view.TestTreeItem;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Max Ross <max.ross@gmail.com>
 */
public class TestTreePresenter {

  public interface Display {
    Tree getTree();
  }

  private final Display display;
  private final CloudCoverServiceAsync svc;
  private final TestDetailsPresenter testDetailsPresenter;
  private Map<String, TestTreeItem> allTestTreeItems = new HashMap<String, TestTreeItem>();

  public TestTreePresenter(CloudCoverServiceAsync svc, Display display,
                           TestDetailsPresenter testDetailsPresenter) {
    this.svc = svc;
    this.display = display;
    this.testDetailsPresenter = testDetailsPresenter;
  }

  public void clearTree() {
    display.getTree().removeItems();
  }

  /**
   * Only called once per Run
   */
  public void setAllTestIds(Run run, Map<String, Integer> testIdsToTestCounts) {
    Tree tree = display.getTree();
    allTestTreeItems.clear();
    tree.removeItems();
    List<String> sorted = new ArrayList<String>(testIdsToTestCounts.keySet());
    Collections.sort(sorted);
    TreeItem root = new TreeItem();
    root.setText(run.getTestRunnerConfigClass() + " (" + run.getNumTests() + ")");
    tree.addItem(root);
    tree.addSelectionHandler(new SelectionHandler<TreeItem>() {
      public void onSelection(SelectionEvent<TreeItem> treeItemSelectionEvent) {
        TreeItem item = treeItemSelectionEvent.getSelectedItem();
        if (item instanceof TestTreeItem) {
          loadTestDetails((TestTreeItem) item);
          testDetailsPresenter.setFailure(null);
        } else if (item instanceof SubTestTreeItem) {
          SubTestTreeItem stti = (SubTestTreeItem) item;
          loadTestDetails((TestTreeItem) stti.getParentItem());
          if (stti.getFailureId() != null) {
            loadTestFailureDetails((TestTreeItem) stti.getParentItem(), stti.getFailureId());
          } else {
            testDetailsPresenter.setFailure(null);
          }
        } else {
          testDetailsPresenter.setTest(null);
        }
      }
    });

    for (String testId : sorted) {
      // we won't have the actual test object until we
      // query for run status so for now just create a test with
      // the minimal info we have
      int testCount = testIdsToTestCounts.get(testId);
      TestTreeItem item = new TestTreeItem(new Test(testId, run, testCount));
      item.setText(testId + " (" + testCount + ")");
      allTestTreeItems.put(testId, item);
      root.addItem(item);
    }
    root.setState(true);
  }

  private void loadTestFailureDetails(TestTreeItem testTreeItem, String failureId) {
    Test test = testTreeItem.getTest();
    Failure theFailure = null;
    for (Failure f : test.getFailures()) {
      if (f.getId().equals(failureId)) {
        if (f.getFailureMsg() != null) {
          // we've already got the failure data
          theFailure = f;
        } else {
          // we found the right failure but we don't have the data.
          // stop looking and perform the fetch
        }
        break;
      }
    }
    if (theFailure == null) {
      AsyncCallback<Failure> callback = new AsyncCallback<Failure>() {
        public void onFailure(Throwable caught) {
          Window.alert(caught.toString());
        }

        public void onSuccess(Failure result) {
          testDetailsPresenter.setFailure(result);
        }
      };
      svc.getFailure(test.getRun().getId(), test.getName(), failureId, callback);
    } else {
      testDetailsPresenter.setFailure(theFailure);
    }
  }


  private void loadTestDetails(final TestTreeItem selected) {
    AsyncCallback<Test> callback = new AsyncCallback<Test>() {
      public void onFailure(Throwable caught) {
        Window.alert(caught.toString());
      }

      public void onSuccess(Test test) {
        // don't bother updating the ui if the test still hasn't finished,
        // we won't have any new info until it is complete
        if (test != null && test.getStatus() != TestStatus.IN_PROGRESS) {
          // new data!  make sure we refresh
          selected.setTest(test);
          Set<String> failureTestNames = new HashSet<String>();
          for (Failure f : test.getFailures()) {
            failureTestNames.add(f.getId());
          }
          setSubtests(selected, test.getSuccesses(), failureTestNames);
          testDetailsPresenter.setTest(test);
        }
      }
    };

    // don't bother refetching the test details if the test already
    // finished running
    if (selected.getTest().getStatus() == TestStatus.IN_PROGRESS) {
      // there might be new data
      svc.getTestById(selected.getRunId(), selected.getTestName(), callback);
    } else {
      // just refresh the details with the data we already have
      testDetailsPresenter.setTest(selected.getTest());
    }
  }

  /**
   * Called when the user selects a Test in the Tree
   */
   public void updateTreeItemStatus(Set<Test> tests, TestStatus status) {
    for (Test t : tests) {
      TestTreeItem item = allTestTreeItems.get(t.getName());
      item.setStatus(status);
    }
  }

  public void setSubtests(TestTreeItem testTreeItem, Set<String> success, Set<String> fail) {
    List<String> sorted = new ArrayList<String>(success);
    sorted.addAll(fail);
    Collections.sort(sorted);
    testTreeItem.removeItems();
    TestStatus overallTestStatus = TestStatus.SUCCESS;
    for (String subTest : sorted) {
      SubTestTreeItem subItem;
      if (success.contains(subTest)) {
        subItem = new SubTestTreeItem(null);
        StyleHelper.setStatus(TestStatus.SUCCESS, subItem);
      } else if (fail.contains(subTest)) {
        subItem = new SubTestTreeItem(subTest);
        StyleHelper.setStatus(TestStatus.FAILURE, subItem);
        overallTestStatus = TestStatus.FAILURE;
      } else {
        subItem = new SubTestTreeItem(subTest);
        StyleHelper.setStatus(TestStatus.TOO_SLOW, subItem);
        overallTestStatus = TestStatus.FAILURE;
      }
      subItem.setText(subTest);
      testTreeItem.addItem(subItem);
    }
    testTreeItem.setState(true);
    StyleHelper.setStatus(overallTestStatus, testTreeItem);
  }
}
