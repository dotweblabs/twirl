/**
 *
 * Copyright (c) 2014 Kerby Martino and others. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  __            __       __
 * |  |_.--.--.--|__.-----|  |_
 * |   _|  |  |  |  |__ --|   _|
 * |____|________|__|_____|____|
 * :: Twist :: Object Mapping ::
 *
 */
package com.textquo.twist.object;

import static org.boon.Lists.list;
import static org.junit.Assert.*;

import com.google.common.collect.Lists;
import com.textquo.twist.GaeObjectStore;
import com.google.appengine.api.datastore.Key;
import com.textquo.twist.ObjectStore;
import com.textquo.twist.LocalDatastoreTestBase;
import com.textquo.twist.TestData;
import com.textquo.twist.entity.ChildEntity;
import com.textquo.twist.entity.Post;
import com.textquo.twist.entity.RootEntity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.textquo.twist.TestData.*;
/**
 * Created by kerby on 4/27/14.
 */
public class ObjectStoreTest extends LocalDatastoreTestBase {

    ObjectStore store = new GaeObjectStore();

//    {
//        GaeObjectStore.register(RootEntity.class);
//        GaeObjectStore.register(ChildEntity.class);
//    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testPut_noIdGiven(){
        ChildEntity entity = new ChildEntity();
        Key key = store.put(entity);
        assertNotNull(key);
        assertEquals(1L, key.getId());
    }

    @Test
    public void testPut_notRegistered(){

        RootEntity rootObject = new RootEntity(); // one entity

        // String not set
        //ChildEntity childObject = new ChildEntity("Test City");
        //ChildEntity embeddedObject = new ChildEntity("Old Test City");

        rootObject.setId("TestParent");
        rootObject.setCount(25);
        //rootObject.setNewChildEntity(childObject); // one entity
        //rootObject.setEmbeddedEntity(embeddedObject); // not included, @Embedded

        Key key = store.put(rootObject);

        assertNotNull(key);
        assertEquals("TestParent", key.getName());
    }

    @Test
    public void testPut_child(){
        RootEntity rootObject = new RootEntity(); // one entity

        // String not set
        ChildEntity childObject = new ChildEntity("Test City");
        childObject.setParent(rootObject);
        rootObject.setId("TestUser");
        rootObject.setCount(25);
        //rootObject.setNewChildEntity(childObject); // one entity, causes stackoverflow error

        Key key = store.put(childObject); // FIXME not consistent, RootEntity is not on last item!

        assertNotNull(key);
        assertEquals("TestUser", key.getParent().getName());
        assertEquals(rootObject.getKey(), key.getParent().getName());

    }

    @Test
    public void testPut_map(){
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("__key__", "testKey");
        map.put("__kind__", "testKind");
        map.put("testField", "testField");

        Key key = store.put(map);

        assertNotNull(key);
        assertNull(key.getParent());
        assertEquals("testKey", key.getName());
        assertEquals("testKind", key.getKind());

        Map result = store.get(Map.class, "testKind", "testKey");

        assertNotNull(result);
        assertEquals("testKey", map.get(GaeObjectStore.KEY_RESERVED_PROPERTY));
        assertEquals("testKind", map.get(GaeObjectStore.KIND_RESERVED_PROPERTY));
        assertEquals("testField", map.get("testField"));
    }

    @Test(expected=RuntimeException.class)
    public void testPut_innerMap(){
        Map<String,Object> map = new HashMap<String,Object>();
        Map<String,Object> inner = new HashMap<String,Object>();

        inner.put("innerField1", "innerField1");

        map.put("__key__", "testKey");
        map.put("__kind__", "testKind");
        map.put("testField", inner);

        Key key = store.put(map);
    }

    @Test
    public void testPut_map_noKey(){
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("__kind__", "testKind");
        Key key = store.put(map);

        assertNotNull(key);
        assertNull(key.getParent());
        assertNull(key.getName());
        assertEquals("testKind", key.getKind());
    }

    @Test
    public void testPut_map_noKeyNoKind(){
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("testField", "testField");
        Key key = store.put(map);

        assertNotNull(key);
        assertNull(key.getParent());
        assertNull(key.getName());
        assertEquals(HashMap.class.getSimpleName(), key.getKind());
    }

    @Test
    public void testPut_cached(){
        Post post = new Post();
        post.setUserId("testUserId");
        post.setMessage("Test Message");
        Key key = store.put(post);
        Post saved = store.get(Post.class, key);

        assertNotNull(saved);
        assertEquals("testUserId", saved.getUserId());
        assertEquals("Test Message", saved.getMessage());
    }

    @Test
    public void testGetByKey(){

        Object testEntity = createTestRootEnity();
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
    public void tesdFindByPropertyKeysOnly(){

        Object testEntity = createTestRootEnity();

        Key key = store.put(testEntity);
        Iterator<RootEntity> result
                = store.find(RootEntity.class).equal("count", 25).keysOnly().now();

        assertNotNull(key);
        assertEquals("TestRoot", key.getName());
        assertNotNull(result);
        assertTrue(result.hasNext());

        RootEntity entity = result.next();

        assertEquals("TestRoot", entity.getKey());

        // Should be nulls because query is "keys only"
        assertNull(entity.getNewChildEntity());
        assertNull(entity.getEmbeddedEntity());
    }

    @Test
    public void testGetById(){

        Object testEntity = createTestRootEnity();
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
//        Object testEntity = createTestRootEnity();
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
