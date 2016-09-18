/**
 *
 * Copyright (c) 2016 Dotweblabs Web Technologies and others. All rights reserved.
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
 * |  |_.--.--.--|__.-----|  |
 * |   _|  |  |  |  |   --|  |_
 * |____|________|__|___| |____|
 * :: twirl :: Object Mapping ::
 *
 */
package com.dotweblabs.twirl.object;

import static org.boon.Lists.list;
import static org.junit.Assert.*;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gson.Gson;
import com.dotweblabs.twirl.LocalDatastoreTestBase;
import com.dotweblabs.twirl.ObjectStore;
import com.dotweblabs.twirl.TestData;
import com.dotweblabs.twirl.entity.*;
import com.dotweblabs.twirl.GaeObjectStore;
import com.google.appengine.api.datastore.Key;
import com.dotweblabs.twirl.types.Cursor;
import com.dotweblabs.twirl.types.ListResult;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.boon.Lists;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by kerby on 4/27/14.
 */
public class ObjectStoreTest extends LocalDatastoreTestBase {

    protected static Logger LOG = LogManager.getLogger(ObjectStoreTest.class.getName());

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
    public void testPut_withEnumField(){
        EntityEnum entityEnum = new EntityEnum();
        entityEnum.setTestEnum(EntityEnum.TestEnum.ONE);
        store.put(entityEnum);
        EntityEnum saved = store.get(EntityEnum.class, entityEnum.getId());
        assertEquals(EntityEnum.TestEnum.ONE, saved.getTestEnum());
    }

    @Test
    public void testPut_aliasedField(){
        EntityAlias entity = new EntityAlias();
        entity.setActiveFlag(true);
        store.put(entity);
        EntityAlias saved = store.get(EntityAlias.class, entity.getId());
        assertEquals(true, saved.isActive());
    }

    @Test
    public void testPut_aliasedField_NULL_reference(){
        EntityAlias entity = new EntityAlias();
        entity.setActive(true);
        entity.setActiveFlag(null);
        store.put(entity);
        EntityAlias saved = store.get(EntityAlias.class, entity.getId());
        assertEquals(true, saved.isActive());
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
    public void testPut_parent(){
        RootEntity rootObject = new RootEntity(); // one entity

        // String not set
        ChildEntity childObject = new ChildEntity("Test City");
        childObject.setParent(rootObject);

        rootObject.setId("TestUser");
        rootObject.setCount(25);
        rootObject.setNewChildEntity(childObject); // one entity, causes stackoverflow error

        Key key = store.put(rootObject); // FIXME not consistent, RootEntity is not on last item!

        rootObject = store.get(RootEntity.class, key);

        ChildEntity update = rootObject.getNewChildEntity();
        update.setType("Edited");

        rootObject.setNewChildEntity(update);
        rootObject.setNewChildEntity(update);
        rootObject.setNewChildEntity(update);

        assertEquals(2L, update.getId().longValue());

        store.put(rootObject);
        store.put(rootObject);

        rootObject.setNewChildEntity(update);
        Key newKey = store.put(rootObject);

        RootEntity saved = store.get(RootEntity.class, newKey);
        ChildEntity childEntity = saved.getNewChildEntity();

        assertEquals(2L, childEntity.getId().longValue());

        // Make sure ChildEntity is not duplicating on multiple parent puts
        List<RootEntity> entities = store.find(RootEntity.class).asList().getList();
        List<ChildEntity> childEntities = store.find(ChildEntity.class).asList().getList();

        assertEquals(1, entities.size());
        assertEquals(1, childEntities.size());

    }

    @Test
    public void testPutParentInTranscation(){
        RootEntity rootObject = new RootEntity(); // one entity

        // String not set
        ChildEntity childObject = new ChildEntity("Test City");
        childObject.setParent(rootObject);

        rootObject.setId("TestUser");
        rootObject.setCount(25);
        rootObject.setNewChildEntity(childObject); // one entity, causes stackoverflow error

        Key key = store.putInTransaction(rootObject);

        rootObject = store.get(RootEntity.class, key);

        assertNotNull(rootObject);

        ChildEntity update = rootObject.getNewChildEntity();
        update.setType("Edited");

        rootObject.setNewChildEntity(update);

        assertEquals(2L, update.getId().longValue());

        store.putInTransaction(rootObject);
        store.putInTransaction(rootObject);

        // Make sure ChildEntity is not duplicating on multiple parent puts
        List<RootEntity> entities = store.find(RootEntity.class).asList().getList();
        List<ChildEntity> childEntities = store.find(ChildEntity.class).asList().getList();

        assertEquals(1, entities.size());
        assertEquals(1, childEntities.size());

    }

    @Test
    public void testPut_noIdwithParentKey(){
        Key demoParentKey = KeyFactory.createKey("Guestbook", "demo");
        EntityNoId entity = new EntityNoId();
        entity.setContent("Sample content");
        entity.setParent(demoParentKey);
        Key key = store.put(entity);
        EntityNoId saved = store.get(EntityNoId.class, key);
        assertNotNull(key);
        assertNotNull(saved);
        assertEquals(demoParentKey, saved.getParent());
        assertEquals("Sample content", saved.getContent());
    }

    @Test//(expected = ObjectNotFoundException.class)
    public void testPut_noIdwithParentKeyAncestor(){
        Key demoParentKey = KeyFactory.createKey("Guestbook", "demo");
        EntityNoId entity = new EntityNoId();
        entity.setContent("Sample content");
        entity.setParent(demoParentKey);
        Key key = store.put(entity);
        List<EntityNoId> entities = store.find(EntityNoId.class, demoParentKey)
                .sortDescending("content")
                .asList().getList();
        EntityNoId saved = entities.get(0);
        assertNotNull(key);
        assertNotNull(saved);
        assertEquals(demoParentKey, saved.getParent());
        assertEquals("Sample content", saved.getContent());

        // Special test should not return the same items above
        // as it is from different ancestor
        Key otherParentKey = KeyFactory.createKey("Guestbook", "other");
        List<EntityNoId> otherEntitites = store.find(EntityNoId.class, otherParentKey)
                .sortDescending("content")
                .asList().getList();
        assertTrue(otherEntitites.isEmpty());

        EntityNoId otherEntity = new EntityNoId();
        otherEntity.setParent(otherParentKey);
        otherEntity.setContent("Other content");
        store.put(otherEntity);

        otherEntitites = store.find(EntityNoId.class, otherParentKey).asList().getList();
        assertTrue(!otherEntitites.isEmpty());
        assertEquals(1, otherEntitites.size());

        EntityNoId otherSaved = otherEntitites.get(0);
        assertNotNull(otherSaved);
        assertEquals("Other content", otherSaved.getContent());
    }

    @Test
    public void testPut_objectId(){
        EntityObjectId entity = new EntityObjectId();
        entity.setContent("Sample Content");
        store.put(entity);
        //System.out.println(entity.getId());
        EntityObjectId saved = store.get(EntityObjectId.class, entity.getId());
        assertNotNull(saved);
        assertEquals("Sample Content", saved.getContent());
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

    @Test//(expected=RuntimeException.class)
    public void testPut_innerMap(){
        Map<String,Object> map = new HashMap<String,Object>();
        Map<String,Object> inner = new HashMap<String,Object>();

        inner.put("innerField1", "innerField1");

        map.put("__key__", "testKey");
        map.put("__kind__", "testKind");
        map.put("testField", inner);

        Key key = store.put(map);
        Map saved = store.get(Map.class, key);
        Map savedInner = (Map) saved.get("testField");
        assertNotNull(saved);
        assertNotNull(savedInner);
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
    public void testPut_map_getAsPOJO(){
        Map<String,Object> map = new LinkedHashMap<>();
        map.put("__key__", "test");
        map.put("__kind__", "Post");
        map.put("created", "2014-12-11T14:31:43 -08:00");
        store.put(map);
        Post saved = store.get(Post.class, "test");
        assertNotNull(saved);
        assertNotNull(saved.getCreated());
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
    public void testPut_long_String(){
        String longString = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
        Post post = new Post();
        post.setUserId("testUserId");
        post.setMessage(longString);
        Key key = store.put(post);
        Post saved = store.get(Post.class, key);
        assertNotNull(saved);
        assertEquals("testUserId", saved.getUserId());
        assertEquals(longString, saved.getMessage());
    }

    @Test
    public void testPut_entityWithBytes() throws Exception {
        byte[] bytes = "Hello World".getBytes(Charset.forName("UTF-8"));
        EntityWithBytes entity = new EntityWithBytes();
        entity.setBytes(bytes);
        store.put(entity);
        Long id = entity.getId();
        EntityWithBytes saved = store.get(EntityWithBytes.class, id);
        assertNotNull(saved);
        assertEquals(bytes.length, saved.getBytes().length);
        assertEquals("Hello World", new String(saved.getBytes(), "UTF-8"));
    }

    @Test
    public void testPut_entityWithBlob() throws Exception {
        byte[] bytes = "Hello World".getBytes(Charset.forName("UTF-8"));
        Blob blob = new Blob(bytes);
        EntityWithBlob entity = new EntityWithBlob();
        entity.setBlob(blob);
        store.put(entity);
        Long id = entity.getId();
        EntityWithBlob saved = store.get(EntityWithBlob.class, id);
        assertNotNull(saved);
        assertEquals(bytes.length, saved.getBlob().getBytes().length);
        assertEquals("Hello World", new String(saved.getBlob().getBytes(), "UTF-8"));

    }

    @Test
    public void testPut_entityWithGeoPoint(){
        EntityGeoPoint entity = new EntityGeoPoint();
        entity.setGeoPoint(new GeoPt(1.2F, 3.4F));
        store.put(entity);
        EntityGeoPoint saved = store.get(EntityGeoPoint.class, entity.getId());
        assertEquals(1.2F, saved.getGeoPoint().getLatitude(), 0.1);
        assertEquals(3.4F, saved.getGeoPoint().getLongitude(), 0.1);
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
    public void testPut_List(){
        Post post = new Post();
        post.setCreated(new Date());
        post.setMessage("Test Message");
        post.setTags(list("tag1", "tag2", "tag3"));
        store.put(post);
        Post saved = store.get(Post.class, post.getId());
        assertNotNull(saved);
        assertTrue(!saved.getTags().isEmpty());
    }

    @Test
    public void testFind_List(){
        Post post = new Post();
        post.setCreated(new Date());
        post.setMessage("Test Message");
        post.setTags(list("tag1", "tag2", "tag3"));
        store.put(post);

        Post saved = store.get(Post.class, post.getId());
        assertNotNull(saved);

        List<Post> postsWithTag = store.find(Post.class).equal("tags", "tag1").asList().getList();
        assertNotNull(postsWithTag);
        assertTrue(!postsWithTag.isEmpty());
    }

    @Test
    public void testFindOrderByStringDate(){
        // December
        for(int i=1; i <= 31; i++){
            Map map = new LinkedHashMap();
            map.put("__key__", "december"+i);
            map.put("__kind__", "EntityWithDate");
            map.put("current", false);
            String zero = i < 10 ? "0" : "";
            map.put("created", "2014-12-" + zero + i + "T14:31:43 -08:00");
            Key key = store.put(map);
            assertEquals("december"+i, key.getName());
        }
        List<EntityWithDate> entities = store.find(EntityWithDate.class)
                .sortDescending("created")
                .asList()
                .getList();
        assertNotNull(entities);
        assertEquals(31, entities.size());
        // November
        for(int i=1; i <= 30; i++){
            Map map = new LinkedHashMap();
            map.put("__key__", "november"+i);
            map.put("__kind__", "EntityWithDate");
            map.put("current", true);
            String zero = i < 10 ? "0" : "";
            map.put("created", "2014-11-" + zero + i + "T14:31:43 -08:00");
            Key key = store.put(map);
            assertEquals("november"+i, key.getName());
        }
        List<EntityWithDate> all = store.find(EntityWithDate.class)
                .sortDescending("created")
                .asList()
                .getList();
        assertNotNull(all);
        assertEquals(61, all.size());
        assertEquals("december31", all.get(0).getId());
        assertEquals("november1", all.get(60).getId());

        List<EntityWithDate> allQueryWithEqual
                = store.find(EntityWithDate.class)
                .equal("current", true)
                .sortDescending("created")
                .asList()
                .getList();

        assertNotNull(allQueryWithEqual);
        assertEquals(30, allQueryWithEqual.size());
        assertEquals("november1", allQueryWithEqual.get(29).getId());

    }

    @Test
    public void tesdFindByPropertyKeysOnly(){

        Object testEntity = TestData.createTestRootEnity();

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
    public void testGetByLongId(){
        EntityLongId entity = new EntityLongId();
        entity.setContent("Test Content");
        store.put(entity);
        assertEquals(1L, entity.getId().longValue());

        EntityLongId stored = store.get(EntityLongId.class, 1L);
        assertNotNull(stored);
        assertEquals(1L, stored.getId().longValue());
        assertEquals("Test Content", stored.getContent());
    }

    @Test
    public void testDelete(){
        Object testEntity = TestData.createTestRootEnity();
        Key key = store.put(testEntity);
        RootEntity result = store.get(RootEntity.class, key);
        assertNotNull(result);
        store.delete(RootEntity.class, result.getKey());
        result = store.get(RootEntity.class, key);
        assertNull(result);
    }

    @Test
    public void testDeletebyId(){
//        Post testEntity = new Post();
//        testEntity.setUserId("user123");
//        Key key = store.put(testEntity);
//        Long id = testEntity.getId();
//        assertNotNull(id);
//        Post result = store.get(Post.class, id);
//        assertNotNull(result);
//        assertEquals("user123", result.getUserId());
//        store.delete(key);
//        result = store.get(Post.class, id);
//        assertNull(result);
    }

    @Test
    public void testFind(){
        store.put(new RootEntity("101", 5));
        store.put(new RootEntity("102", 4));
        store.put(new RootEntity("103", 3));
        store.put(new RootEntity("104", 2));
        store.put(new RootEntity("105", 1));

        List<RootEntity> one = Lists.list(store.find(RootEntity.class)
                .equal("__key__", "101")
                .now());

        List<RootEntity> four = Lists.list(store.find(RootEntity.class)
                .greaterThan("__key__", "101")
                .now());

        List<RootEntity> all = Lists.list(store.find(RootEntity.class)
                .greaterThanOrEqual("__key__", "101")
                .sortAscending("__key__")
                .sortDescending("count")
                .now());

        List<RootEntity> all_order = Lists.list(store.find(RootEntity.class)
                .greaterThanOrEqual("__key__", "101")
                .now());

        List<RootEntity> all_reversed = Lists.list(store.find(RootEntity.class)
                .greaterThanOrEqual("__key__", "101")
                .sortAscending("__key__")
                .sortAscending("count").now());

        List<RootEntity> all_limited = Lists.list(store.find(RootEntity.class)
                .greaterThanOrEqual("__key__", "101")
                .sortAscending("__key__")
                .sortAscending("count").limit(1).now());

        List<RootEntity> all_count = Lists.list(store.find(RootEntity.class)
                .greaterThanOrEqual("count", 1)
                .now());

        assertEquals(1, one.size());
        assertEquals(4, four.size());
        assertEquals(5, all.size());
        assertEquals("101", all_order.get(0).getKey());
        assertEquals("105", all_order.get(4).getKey());
        assertEquals(5, all_reversed.size());
        assertEquals(1, all_limited.size());
        assertEquals("105", all_reversed.get(4).getKey());
        assertEquals("101", all_reversed.get(0).getKey());
        assertEquals(5, all_count.size());

        assertEquals("101", one.get(0).getKey());
        assertEquals(5, one.get(0).getCount());
    }

    @Test
    public void testFindFirst(){
        store.put(new RootEntity("101", 5));
        RootEntity first = store.find(RootEntity.class).first();
        assertNotNull(first);
        assertEquals("101", first.getKey());
    }

    @Test
    public void testFindMultiFilter(){
        Date start = new Date();
        store.put(new RootEntity("101", 5, true));
        store.put(new RootEntity("102", 4, true));
        store.put(new RootEntity("103", 3, true));
        store.put(new RootEntity("104", 2, true));
        store.put(new RootEntity("105", 1, false));
        Date end = new Date();

        List<RootEntity> two = Lists.list(store.find(RootEntity.class)
                .greaterThanOrEqual("created", start)
                .lessThanOrEqual("created", end)
                .sortDescending("created")
                .sortDescending("count")
                .equal("status", true)
                .now());

        assertEquals(4, two.size());
        assertEquals("104", two.get(0).getKey());
    }

    @Test
    public void testFind_withSort(){
        store.put(new RootEntity("101", 5));
        store.put(new RootEntity("102", 4));
        store.put(new RootEntity("103", 3));
        store.put(new RootEntity("104", 2));
        store.put(new RootEntity("105", 1));

        List<RootEntity> all = Lists.list(store.find(RootEntity.class)
                .sortAscending("__key__")
                .sortAscending("count").now());

        assertNotNull(all);
        assertEquals(5, all.size());
        assertEquals("101", all.get(0).getKey());
    }

    @Test
    public void testFindKind(){
        CustomKind first = new CustomKind("Count", 5L);
        Map fields = new LinkedHashMap();
        fields.put("field1", 1);
        first.setFields(fields);
        store.put(first);
        store.put(new CustomKind("Count", 4L));
        store.put(new CustomKind("Count", 3L));
        store.put(new CustomKind("Count", 2L));
        store.put(new CustomKind("Count", 1L));
        Iterator<CustomKind> it = store.find(CustomKind.class, "Count").limit(100).now();
        assertTrue(it.hasNext());
        CustomKind saved = ((CustomKind) it.next());
        long value = saved.getValue();
        assertEquals(5L, value);
        assertEquals("Count", saved.getKind());
        assertNotNull(saved.getFields());
    }

    @Test
    public void testFindOne(){

    }

    @Test
    public void testFind_asList(){
        store.put(new RootEntity("101", 5));
        store.put(new RootEntity("102", 4));
        store.put(new RootEntity("103", 3));
        store.put(new RootEntity("104", 2));
        store.put(new RootEntity("105", 1));

        List<RootEntity> entities = store.find(RootEntity.class).asList().getList();
        List<RootEntity> limited = store.find(RootEntity.class).limit(3).asList().getList();

        assertTrue(!entities.isEmpty());
        assertTrue(!limited.isEmpty());
        assertEquals(5, entities.size());
        assertEquals(3, limited.size());
    }

    @Test
    public void testFind_asIterable(){
        store.put(new RootEntity("101", 5));
        store.put(new RootEntity("102", 4));
        store.put(new RootEntity("103", 3));
        store.put(new RootEntity("104", 2));
        store.put(new RootEntity("105", 1));

        Iterable<RootEntity> e = store.find(RootEntity.class).asIterable();
        Iterable<RootEntity> l = store.find(RootEntity.class).limit(3).asIterable();

        List<RootEntity> entities = new LinkedList<RootEntity>();
        List<RootEntity> limited = new LinkedList<RootEntity>();

        for (RootEntity entity : e) {
            entities.add(entity);
        }

        for (RootEntity entity : l){
            limited.add(entity);
        }

        assertTrue(!entities.isEmpty());
        assertTrue(!limited.isEmpty());
        assertEquals(5, entities.size());
        assertEquals(3, limited.size());
    }

    @Test
    public void testFind_asList_withCursor(){
        store.put(new RootEntity("101", 5));
        store.put(new RootEntity("102", 4));
        store.put(new RootEntity("103", 3));
        store.put(new RootEntity("104", 2));
        store.put(new RootEntity("105", 1));

        String startWebSafeCursor = "";

        String oldCursorString = "";

        ListResult<RootEntity> entities = store.find(RootEntity.class)
                .greaterThanOrEqual("count", 1)
                .limit(2)
                .withCursor(startWebSafeCursor)
                .sortAscending("count")
                .asList();

        Cursor nextCursor = entities.getCursor();
        String newCursorString = nextCursor.getWebSafeString();

        LOG.info("Old Cursor=" + oldCursorString + "\n"
                + "New Cursor=" + newCursorString);
        assertFalse(newCursorString.equals(oldCursorString));
        assertFalse(entities.getList().isEmpty());
        assertEquals(2, entities.getList().size());
        assertEquals("105", entities.getList().get(0).getKey());
        assertNotNull(nextCursor);

        oldCursorString = nextCursor.getWebSafeString();
        entities = store.find(RootEntity.class)
                .greaterThanOrEqual("count", 1)
                .limit(2)
                .sortAscending("count")
                .withCursor(nextCursor)
                .asList();

        nextCursor = entities.getCursor();
        newCursorString = nextCursor.getWebSafeString();

        LOG.info("Old Cursor=" + oldCursorString + "\n"
                + "New Cursor=" + newCursorString);
        assertFalse(newCursorString.equals(oldCursorString));
        assertFalse(entities.getList().isEmpty());
        assertEquals(2, entities.getList().size());
        assertEquals("103", entities.getList().get(0).getKey());

        oldCursorString = nextCursor.getWebSafeString();
        entities = store.find(RootEntity.class)
                .greaterThanOrEqual("count", 1)
                .limit(2)
                .sortAscending("count")
                .withCursor(nextCursor)
                .asList();
        nextCursor = entities.getCursor();
        newCursorString = nextCursor.getWebSafeString();

        assertNotSame(newCursorString, oldCursorString);

        assertFalse(entities.getList().isEmpty());
        assertEquals(1, entities.getList().size());
        assertEquals("101", entities.getList().get(0).getKey());

        oldCursorString = nextCursor.getWebSafeString();
        entities = store.find(RootEntity.class)
                .greaterThanOrEqual("count", 1)
                .limit(2)
                .sortAscending("count")
                .withCursor(nextCursor)
                .asList();
        nextCursor = entities.getCursor();
        newCursorString = nextCursor.getWebSafeString();

        assertNotSame(newCursorString, oldCursorString);
        assertTrue(entities.getList().isEmpty());

    }


    @Test
    public void testFindSkipKeys(){
        store.put(new SimpleEntity("101", 10));
        store.put(new SimpleEntity("102", 9));
        store.put(new SimpleEntity("103", 8));
        store.put(new SimpleEntity("104", 7));
        store.put(new SimpleEntity("105", 6));
        store.put(new SimpleEntity("106", 5));
        store.put(new SimpleEntity("107", 4));
        store.put(new SimpleEntity("108", 3));
        store.put(new SimpleEntity("109", 2));
        store.put(new SimpleEntity("110", 1));

        ListResult<SimpleEntity> entities = store.find(SimpleEntity.class)
                .notEqual("__key__", Arrays.asList("102", 104)) // skip
                .limit(5)
                .sortAscending("__key__")
                .sortAscending("count")
                .withCursor("")
                .asList();
        assertEquals(5, entities.getList().size());
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
                = Lists.list(store.update(RootEntity.class)
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


    @Test
    public void testPut_GetJSONEntityWithMap(){
        JSONEntityWithMap withMap = new JSONEntityWithMap();
        withMap.setField("testKey", "testValue");
        withMap.setField("testKey2", "testValue2");
        Key key = store.put(withMap);
        JSONEntityWithMap result = store.get(JSONEntityWithMap.class, key);
        assertEquals("testValue2", result.getField("testKey2"));
        LOG.info("Result: " + new Gson().toJson(result));
    }

}
