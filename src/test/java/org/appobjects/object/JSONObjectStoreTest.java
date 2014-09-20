package org.appobjects.object;

import com.google.appengine.api.datastore.Key;
import com.google.common.collect.Lists;
import org.appobjects.GaeObjectStore;
import org.appobjects.LocalDatastoreTestCase;
import org.appobjects.ObjectStore;
import org.appobjects.TestData;
import org.appobjects.TestData.RootEntity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class JSONObjectStoreTest extends LocalDatastoreTestCase {

    ObjectStore store = new GaeObjectStore();

//    {
//        GaeObjectStore.register(RootEntity.class);
//        GaeObjectStore.register(TestData.ChildEntity.class);
//    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testPut_notRegistered(){
        TestData.JSONEntity entity = new TestData.JSONEntity();
        entity.setKind("TestKind");
        entity.setId("TestId");
        entity.getFields().put("TestField1", "Test Value 1");
        entity.getFields().put("TestField2", "Test Value 2");
        entity.getFields().put("TestField3", "Test Value 3");

        Key key = store.put(entity);

        assertNotNull(key);
        assertEquals("TestId", key.getName());
    }

    @Test
    public void testPut_json(){

    }

    @Test
    public void testGetByKey(){
        TestData.JSONEntity entity = new TestData.JSONEntity();
        entity.setKind("TestKind");
        entity.setId("TestId");
        entity.getFields().put("TestField1", "Test Value 1");
        entity.getFields().put("TestField2", "Test Value 2");
        entity.getFields().put("TestField3", "Test Value 3");

        Key key = store.put(entity);


        assertNotNull(key);
        assertEquals("TestId", key.getName());
    }

    @Test
    public void testGetById(){

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
