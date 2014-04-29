package org.appobjects.object;

import static org.junit.Assert.*;

import org.appobjects.GaeObjectStore;
import com.google.appengine.api.datastore.Key;
import org.appobjects.TestData;
import org.appobjects.TestData.RootEntity;
import org.appobjects.TestData.TestEntity;
import org.appobjects.common.AutoGenerateStringIdException;
import org.appobjects.LocalDatastoreTestCase;
import org.junit.Test;

/**
 * Created by kerby on 4/27/14.
 */
public class ObjectStoreTest extends LocalDatastoreTestCase {

    GaeObjectStore store = new GaeObjectStore();

    @Test(expected = AutoGenerateStringIdException.class)
    public void testPut_shouldThrowException(){
        RootEntity rootObject = new RootEntity(); // one Entity

        // String not set
        TestEntity childObject = new TestEntity("Test City");
        TestEntity embeddedObject = new TestEntity("Old Test City");

        rootObject.setKey("TestUser");
        rootObject.setCount(25);
        rootObject.setNewTestEntity(childObject); // one Entity
        rootObject.setOldTestEntity(embeddedObject); // not included, @Embedded

        try {
            Key key = store.put(rootObject);
        } catch(AutoGenerateStringIdException e){
            assertEquals(e.getMessage(), "Cannot auto-generate String @Id"); // dangerous test!
            throw e;
        }
    }

    @Test
    public void testGet(){
//       Key key = store.put(TestData.createTestRootEnity());
//       assertNotNull(key);
//       RootEntity result = store.get(RootEntity.class, key);
//       assertNotNull(result);
    }


}
