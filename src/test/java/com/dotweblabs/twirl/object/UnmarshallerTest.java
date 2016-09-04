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

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.dotweblabs.twirl.LocalDatastoreTestBase;
import com.dotweblabs.twirl.GaeObjectStore;
import com.dotweblabs.twirl.entity.Post;
import com.dotweblabs.twirl.gae.GaeUnmarshaller;
import org.junit.Test;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import static org.junit.Assert.*;

/**
 * Created by kerby on 4/28/14.
 */
public class UnmarshallerTest extends LocalDatastoreTestBase {


    GaeUnmarshaller unmarshaller;
    GaeObjectStore store;

    @Override
    public void setupDatastore() {
        super.setupDatastore();
        store = new GaeObjectStore();
        unmarshaller = (GaeUnmarshaller) store.unmarshaller();
    }

    @Test
    public void testCreateMapFromEmbeddedEntity(){
        EmbeddedEntity ee = new EmbeddedEntity();
        ee.setProperty("TestProperty", "TestValue");
        Object result = unmarshaller.getMapOrList(ee);
        assertTrue(result instanceof Map);
        assertEquals("TestValue", ((Map) result).get("TestProperty"));
    }

    @Test
    public void testCreateMapFromEmbeddedEntityWithKey(){
        EmbeddedEntity ee = new EmbeddedEntity();
        ee.setKey(KeyStructure.createKey("TestKind", "TestKey"));
        ee.setProperty("TestProperty", "TestValue");
        Object result = unmarshaller.getMapOrList(ee);
        assertTrue(result instanceof Map);
        assertEquals("TestValue", ((Map) result).get("TestProperty"));
    }

    @Test
    public void testCreateMapFromEntity(){
        Entity source = new Entity("TestKind");
        source.setProperty("TestProperty", "TestValue");
        source.setProperty("TestProperty2", "TestValue2");
        Map<String,Object> destination = new LinkedHashMap<>();

        unmarshaller.unmarshall(destination, source);

        assertEquals("TestValue", destination.get("TestProperty"));
        assertEquals("TestValue2", destination.get("TestProperty2"));
    }

    @Test
    public void testUnmarshallDate(){
        Entity source = new Entity("Post");
        source.setProperty("created", "2014-12-11T14:31:43 -08:00");

        Post destination = new Post();

        unmarshaller.unmarshall(destination, source);

        assertNotNull(destination.getCreated());
        assertTrue(destination.getCreated() instanceof Date);
    }
}

