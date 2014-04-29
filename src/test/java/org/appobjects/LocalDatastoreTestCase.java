package org.appobjects;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import org.junit.After;
import org.junit.Before;

public abstract class LocalDatastoreTestCase {

  private final LocalServiceTestHelper helper =
          new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig()
                  .setApplyAllHighRepJobPolicy());

  @Before
  public void setupDatastore() {
    helper.setUp();
  }

  @After
  public void tearDownDatastore() {
    helper.tearDown();
  }
}