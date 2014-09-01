package org.appobjects.object;

import static org.boon.Boon.puts;
import static org.boon.Lists.list;
import static org.junit.Assert.*;

import com.google.common.collect.Lists;
import org.appobjects.GaeObjectStore;
import com.google.appengine.api.datastore.Key;
import org.appobjects.ObjectStore;
import org.appobjects.TestData;
import org.appobjects.TestData.RootEntity;
import org.appobjects.LocalDatastoreTestCase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Iterator;
import java.util.List;

/**
 * Created by kerby on 4/27/14.
 */
public class ObjectStoreTest extends LocalDatastoreTestCase {

    ObjectStore store = new GaeObjectStore();

//    {
//        GaeObjectStore.register(RootEntity.class);
//        GaeObjectStore.register(TestData.ChildEntity.class);
//    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testPut_notRegistered(){

        RootEntity rootObject = new RootEntity(); // one Entity

        // String not set
        //TestData.ChildEntity childObject = new TestData.ChildEntity("Test City");
        //TestData.ChildEntity embeddedObject = new TestData.ChildEntity("Old Test City");

        rootObject.setId("TestParent");
        rootObject.setCount(25);
        //rootObject.setNewChildEntity(childObject); // one Entity
        //rootObject.setEmbeddedEntity(embeddedObject); // not included, @Embedded

        Key key = store.put(rootObject);

        assertNotNull(key);
        assertEquals("TestParent", key.getName());
    }

    @Test
    public void testPut_child(){
        RootEntity rootObject = new RootEntity(); // one Entity

        // String not set
        TestData.ChildEntity childObject = new TestData.ChildEntity("Test City");
        childObject.setParent(rootObject);
        rootObject.setId("TestUser");
        rootObject.setCount(25);
        //rootObject.setNewChildEntity(childObject); // one Entity, causes stackoverflow error

        Key key = store.put(childObject); // FIXME not consistent, RootEntity is not on last item!

        assertNotNull(key);
        assertEquals("TestUser", key.getParent().getName());
        assertEquals(rootObject.getKey(), key.getParent().getName());

    }

    @Test
    public void testGetByKey(){

        Object testEntity = TestData.createTestRootEnity();
        Key key = store.put(testEntity);
        RootEntity result = store.get(RootEntity.class, key);

        assertNotNull(key);
        assertEquals("TestRoot", key.getName());
        assertNotNull(result);

        assertNotNull(result.getNewChildEntity());
        assertNotNull(result.getEmbeddedEntity());
        assertEquals("TestRoot", result.getKey());
        assertEquals("TestChild", result.getNewChildEntity().getType());
        assertEquals("TestEmbedded", result.getEmbeddedEntity().getType());
    }

    @Test
    public void testGetById(){

        Object testEntity = TestData.createTestRootEnity();
        Key key = store.put(testEntity);
        RootEntity result = store.get(RootEntity.class, "TestRoot");

        assertNotNull(key);
        assertEquals("TestRoot", key.getName());
        assertNotNull(result);

        assertNotNull(result.getNewChildEntity());
        assertNotNull(result.getEmbeddedEntity());
        assertEquals("TestRoot", result.getKey());
        assertEquals("TestChild", result.getNewChildEntity().getType());
        assertEquals("TestEmbedded", result.getEmbeddedEntity().getType());
    }

    @Test
    public void testDelete(){
//        Object testEntity = TestData.createTestRootEnity();
//        Key key = store.put(testEntity);
//        RootEntity result = store.get(RootEntity.class, key);
//        assertNotNull(result);
//        store.delete(RootEntity.class, result.getKey());
//        result = store.get(RootEntity.class, key);
//        assertNull(result);
    }

    @Test
    public void testFind(){
        store.put(new RootEntity("101", 5));
        store.put(new RootEntity("102", 4));
        store.put(new RootEntity("103", 3));
        store.put(new RootEntity("104", 2));
        store.put(new RootEntity("105", 1));

        List<RootEntity> one = Lists.newArrayList(store.find(RootEntity.class).equal("__key__", "101").now());
        List<RootEntity> four = Lists.newArrayList(store.find(RootEntity.class).greaterThan("__key__", "101").now());
        List<RootEntity> all = Lists.newArrayList(store.find(RootEntity.class).greaterThanOrEqual("__key__", "101").now());

        List<RootEntity> all_reversed = Lists.newArrayList(store.find(RootEntity.class).greaterThanOrEqual("__key__", "101")
                .sortAscending("count").now());
        List<RootEntity> all_limited = Lists.newArrayList(store.find(RootEntity.class).greaterThanOrEqual("__key__", "101")
                .sortAscending("count").limit(1).now());

        List<RootEntity> all_count = Lists.newArrayList(store.find(RootEntity.class).greaterThanOrEqual("count", 1).now());
        List<RootEntity> two = Lists.newArrayList(store.find(RootEntity.class).greaterThanOrEqual("count", 1).greaterThanOrEqual("__key__", "104").now());

        assertEquals(1, one.size());
        assertEquals(4, four.size());
        assertEquals(5, all.size());
        assertEquals(5, all_reversed.size());
        assertEquals(1, all_limited.size());
        assertEquals("105", all_reversed.get(4).getKey());
        assertEquals("101", all_reversed.get(0).getKey());
        assertEquals(5, all_count.size());
        assertEquals(2, two.size());

        assertEquals("101", one.get(0).getKey());
        assertEquals(5, one.get(0).getCount());

    }

    @Test
    public void testFindOne(){

    }

    @Test
    public void testUpdate(){
        store.put(new RootEntity("101", 5));
        store.put(new RootEntity("102", 4));
        store.put(new RootEntity("103", 3));
        store.put(new RootEntity("104", 2));
        store.put(new RootEntity("105", 1));

        RootEntity update101 = new RootEntity("101", 105);
        RootEntity update102 = new RootEntity("102", 104);
        RootEntity update103 = new RootEntity("103", 103);
        RootEntity update104 = new RootEntity("104", 102);
        RootEntity update105 = new RootEntity("105", 101);

        List<RootEntity> update_all
                = Lists.newArrayList(store.update(RootEntity.class)
                    .greaterThanOrEqual("__key__", "101")
                    .with(update101)
                    .now());

        assertEquals(5, update_all.size());
        assertEquals(105, update_all.get(0).getCount());
        assertEquals(105, update_all.get(1).getCount());
        assertEquals(105, update_all.get(2).getCount());
        assertEquals(105, update_all.get(3).getCount());
        assertEquals(105, update_all.get(4).getCount());
    }


}
