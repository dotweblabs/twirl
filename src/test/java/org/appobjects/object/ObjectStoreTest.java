package org.appobjects.object;

import static org.junit.Assert.*;

import org.appobjects.GaeObjectStore;
import com.google.appengine.api.datastore.Key;
import org.appobjects.TestData;
import org.appobjects.TestData.RootEntity;
import org.appobjects.common.AutoGenerateStringIdException;
import org.appobjects.LocalDatastoreTestCase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Created by kerby on 4/27/14.
 */
public class ObjectStoreTest extends LocalDatastoreTestCase {

    GaeObjectStore store = new GaeObjectStore();

    {
        GaeObjectStore.register(RootEntity.class);
        GaeObjectStore.register(TestData.ChildEntity.class);
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testPut_shouldThrowException(){

        exception.expect(AutoGenerateStringIdException.class);

        RootEntity rootObject = new RootEntity(); // one Entity

        // String not set
        TestData.ChildEntity childObject = new TestData.ChildEntity("Test City");
        TestData.ChildEntity embeddedObject = new TestData.ChildEntity("Old Test City");

        rootObject.setKey("TestUser");
        rootObject.setCount(25);
        rootObject.setNewChildEntity(childObject); // one Entity
        rootObject.setOldChildEntity(embeddedObject); // not included, @Embedded
        Key key = store.put(rootObject);

    }

    @Test
    public void testPut_child(){
        RootEntity rootObject = new RootEntity(); // one Entity

        // String not set
        TestData.ChildEntity childObject = new TestData.ChildEntity("Test City");
        childObject.setParent(rootObject);

        rootObject.setKey("TestUser");
        rootObject.setCount(25);
        //rootObject.setNewChildEntity(childObject); // one Entity, causes stackoverflow error

        Key key = store.put(childObject); // FIXME not consistent, RootEntity is not on last item!

        assertNotNull(key);
        assertEquals("SomeUniqueId1", key.getName());
        assertEquals(rootObject.getKey(), key.getParent().getName());

    }

    @Test
    public void testGet(){
//       Object testEntity = TestData.createTestRootEnity();
//       Key key = store.put(testEntity);
//       assertNotNull(key);
//       RootEntity result = store.get(RootEntity.class, key);
//       assertNotNull(result);
    }

    @Test
    public void testDelete(){

    }

    @Test
    public void testFind(){

    }

    @Test
    public void testFindOne(){

    }

    @Test
    public void testUpdate(){

    }


}
