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

import com.google.appengine.api.datastore.Key;
import com.dotweblabs.twirl.LocalDatastoreTestBase;
import com.dotweblabs.twirl.GaeObjectStore;
import com.dotweblabs.twirl.ObjectStore;
import com.dotweblabs.twirl.entity.JSONEntity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class JSONObjectStoreTest extends LocalDatastoreTestBase {

    ObjectStore store = new GaeObjectStore();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testPut_notRegistered(){
        JSONEntity entity = new JSONEntity();
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
        JSONEntity entity = new JSONEntity();
        entity.setKind("TestKind");
        entity.setId("TestId");
        entity.setContent("Sample Content");
        entity.getFields().put("TestField1", "Test Value 1");
        entity.getFields().put("TestField2", "Test Value 2");
        entity.getFields().put("TestField3", "Test Value 3");

        Map innerField = new LinkedHashMap<>();
        entity.getFields().put("innerField", innerField);

        Key key = store.put(entity);
        assertNotNull(key);
        assertEquals("TestId", key.getName());

        JSONEntity saved = store.get(JSONEntity.class, key);
        assertNotNull(saved);
        assertTrue(!saved.getFields().isEmpty());
        // Check for leaks
        assertFalse(saved.getFields().containsKey("id"));
        assertFalse(saved.getFields().containsKey("kind"));
        assertFalse(saved.getFields().containsKey("content"));
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
