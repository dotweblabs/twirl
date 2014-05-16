package org.appobjects.object;

import org.appobjects.LocalDatastoreTestCase;
import org.appobjects.TestData;
import org.junit.Test;

import static org.junit.Assert.*;

import static org.appobjects.ObjectStoreService.store;
/**
 * Created by kerby on 5/16/14.
 */
public class ObjecstoreServiceTest extends LocalDatastoreTestCase {
    @Test
    public void testStore(){
        TestData.RootEntity entity = TestData.createTestRootEnity();
        store().put(entity);
        TestData.RootEntity one = store().get(TestData.RootEntity.class, "TestRoot");
        
        assertNotNull(one);
    }
}
