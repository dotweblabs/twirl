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

import static org.junit.Assert.*;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.textquo.twist.Marshaller;
import com.textquo.twist.TestData;
import com.textquo.twist.TestData.RootEntity;
import com.textquo.twist.TestData.ChildEntity;
import com.textquo.twist.common.AutoGenerateStringIdException;
import com.textquo.twist.gae.GaeMarshaller;
import com.textquo.twist.LocalDatastoreTestCase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.IdentityHashMap;

/**
 * Created by kerby on 4/23/14.
 */
public class MarshallerTest extends LocalDatastoreTestCase {

    Marshaller testMarshaller =  new GaeMarshaller();

    @Test
    public void testCreateMapFromPOJO(){

    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testMarshall_withoutStringKey_shouldThrowError(){
        RootEntity rootObject = new RootEntity(); // one Entity
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
        RootEntity rootObject = new RootEntity(); // one Entity
        ChildEntity childObject = new TestData.ChildEntity("Test City");
        rootObject.setId("TestUser");
        rootObject.setCount(25);
        childObject.setParent(rootObject);
        rootObject.setNewChildEntity(childObject); // one Entity

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
        RootEntity rootObject = new RootEntity(); // one Entity
        ChildEntity childObject = new TestData.ChildEntity("Test City");
        ChildEntity embeddedObject = new TestData.ChildEntity("Old Test City");
        embeddedObject.setId(1L);
        embeddedObject.setType("EmbeddedType");
        rootObject.setId("TestUser");
        rootObject.setCount(25);
        childObject.setParent(rootObject);
        rootObject.setNewChildEntity(childObject); // one Entity
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
        TestData.ChildChildEntity cc = new TestData.ChildChildEntity();
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
        TestData.JSONEntity json = new TestData.JSONEntity();

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




}
