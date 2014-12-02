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

import com.google.appengine.api.datastore.Key;
import com.google.common.collect.Lists;
import com.textquo.twist.GaeObjectStore;
import com.textquo.twist.LocalDatastoreTestCase;
import com.textquo.twist.ObjectStore;
import com.textquo.twist.TestData;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
