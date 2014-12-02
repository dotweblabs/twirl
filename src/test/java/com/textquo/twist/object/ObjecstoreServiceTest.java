package com.textquo.twist.object;

import com.textquo.twist.LocalDatastoreTestCase;
import com.textquo.twist.TestData;
import org.junit.Test;

import static org.junit.Assert.*;

import static com.textquo.twist.ObjectStoreService.store;
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
