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

import static org.junit.Assert.*;

import com.google.appengine.api.datastore.*;
import com.dotweblabs.twirl.LocalDatastoreTestBase;
import com.dotweblabs.twirl.common.AutoGenerateStringIdException;
import com.dotweblabs.twirl.entity.ChildEntity;
import com.dotweblabs.twirl.entity.RootEntity;
import com.dotweblabs.twirl.Marshaller;
import com.dotweblabs.twirl.entity.*;
import com.dotweblabs.twirl.gae.GaeMarshaller;
import com.dotweblabs.twirl.entity.ChildChildEntity;
import com.dotweblabs.twirl.entity.JSONEntity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

import static org.boon.Lists.list;


/**
 * Created by kerby on 4/23/14.
 */
public class MarshallerTest extends LocalDatastoreTestBase {

    Marshaller testMarshaller =  new GaeMarshaller();

    @Test
    public void testCreateMapFromPOJO(){

    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testMarshall_withoutStringKey_shouldThrowError(){
        RootEntity rootObject = new RootEntity(); // one entity
        thrown.expect(AutoGenerateStringIdException.class);
        IdentityHashMap<Object,Entity> stack = testMarshaller.marshall(null, rootObject);
    }

    @Test
    public void testMarshall_Child_first(){
        ChildEntity child = new ChildEntity();
        RootEntity parent = new RootEntity();
        parent.setId("ParentKey");
        child.setParent(parent);
        IdentityHashMap<Object,Entity> stack = testMarshaller.marshall(null, child);
        Entity childEntity = stack.get(child);
        assertNotNull(stack);
        assertEquals(2, stack.size());
        assertEquals("ParentKey", childEntity.getParent().getName());

    }

    @Test
    public void testMarshall_Child(){
        RootEntity rootObject = new RootEntity(); // one entity
        ChildEntity childObject = new ChildEntity("Test City");
        rootObject.setId("TestUser");
        rootObject.setCount(25);
        childObject.setParent(rootObject);
        rootObject.setNewChildEntity(childObject); // one entity

        IdentityHashMap<Object,Entity> stack = testMarshaller.marshall(null, rootObject);

        Entity rootObjectEntity = stack.get(rootObject);
        Entity childObjectEntity = stack.get(childObject);

        Key childKey = (Key) rootObjectEntity.getProperty("newChildEntity");
        Key parentKey = childKey.getParent();

        assertEquals(2, stack.size());
        assertNotNull(parentKey);
        assertTrue(childKey instanceof Key);
        assertEquals(rootObjectEntity.getKey().getId(), parentKey.getId());
        assertEquals(rootObjectEntity.getKey().getName(), parentKey.getName());
    }

    @Test
    public void testMarshall_Embedded(){
        RootEntity rootObject = new RootEntity(); // one entity
        ChildEntity childObject = new ChildEntity("Test City");
        ChildEntity embeddedObject = new ChildEntity("Old Test City");
        embeddedObject.setId(1L);
        embeddedObject.setType("EmbeddedType");
        rootObject.setId("TestUser");
        rootObject.setCount(25);
        childObject.setParent(rootObject);
        rootObject.setNewChildEntity(childObject); // one entity
        rootObject.setEmbeddedEntity(embeddedObject); // not included, @Embedded

        IdentityHashMap<Object,Entity> stack = testMarshaller.marshall(null, rootObject);

        Entity rootObjectEntity = stack.get(rootObject);
        Entity childObjectEntity = stack.get(childObject);

        EmbeddedEntity ee = (EmbeddedEntity) rootObjectEntity.getProperty("embeddedEntity");
        Key childKey = (Key) rootObjectEntity.getProperty("newChildEntity");
        Key parentKey = childKey.getParent();

        assertEquals(2, stack.size());
        assertNotNull(parentKey);
        assertEquals(EmbeddedEntity.class, ee.getClass());
        assertTrue(childKey instanceof Key);
        assertEquals(rootObjectEntity.getKey().getId(), parentKey.getId());
        assertEquals(rootObjectEntity.getKey().getName(), parentKey.getName());

        assertEquals(1L, ee.getProperties().get("id"));
        assertEquals("EmbeddedType", ee.getProperties().get("type"));

    }

    @Test
    public void testMarshall_ChildChild(){
        RootEntity rootObject = new RootEntity();
        ChildChildEntity cc = new ChildChildEntity();
        ChildEntity child = new ChildEntity();
        child.setType("ChildType");
        child.setParent(rootObject);
        cc.setChild(child);

        IdentityHashMap<Object,Entity> stack = testMarshaller.marshall(null, cc);

        assertNotNull(stack);
        assertTrue(!stack.isEmpty());
    }

    @Test
    public void testMarshall_JSONEntity() {
        JSONEntity json = new JSONEntity();

        json.setKind("MyDocument");
        json.setId("abcdef12345");
        json.getFields().put("age", 28);
        json.getFields().put("name", "Name");

        IdentityHashMap<Object,Entity> stack = testMarshaller.marshall(null, json);
        Entity e = stack.get(json);

        assertNotNull(stack);
        assertTrue(!stack.isEmpty());

        assertEquals("MyDocument", e.getKind());
    }

    @Test
    public void test(){
        Entity parent = new Entity("Parent");
// set parent properties...
        List<EmbeddedEntity> children = new ArrayList<EmbeddedEntity>();
        for (int i = 0; i < 5; i++) {
            EmbeddedEntity child = new EmbeddedEntity();
            // set child properties...
            children.add(child);
        }
        parent.setUnindexedProperty("children", children);
        parent.setUnindexedProperty("children2", list("tag1", "tag2", "tag3"));

        DatastoreServiceFactory.getDatastoreService().put(parent);

        try {
            Entity e = DatastoreServiceFactory.getDatastoreService().get(parent.getKey());
            e.getKey();
            Object children2 = e.getProperty("children2");
            assertTrue(children2 instanceof List);
        } catch (EntityNotFoundException e1) {
            e1.printStackTrace();
        }
    }


    @Test
    public void test_Marshall_List(){
        Post post = new Post();
        post.setTags(list("tag1", "tag2", "tag3"));

        IdentityHashMap<Object,Entity> stack = testMarshaller.marshall(null, post);
        Entity e = stack.get(post);
        Object tags = e.getProperty("tags");

        assertNotNull(e);
        assertTrue(tags instanceof List);
        assertEquals("tag1", ((List) tags).get(0));
        assertEquals("tag2", ((List) tags).get(1));
        assertEquals("tag3", ((List) tags).get(2));
    }

    @Test(expected = RuntimeException.class)
    public void test_Marshall_List_with_POJO(){
        RootEntity entity = new RootEntity();
        entity.setId("test_id");

        entity.setChildren(list(new ChildEntity(), new ChildEntity(), new ChildEntity()));

        IdentityHashMap<Object,Entity> stack = testMarshaller.marshall(null, entity);
        Entity e = stack.get(entity);
    }


}
