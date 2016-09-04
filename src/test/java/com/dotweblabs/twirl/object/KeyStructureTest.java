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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class KeyStructureTest {
    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig()
                .setDefaultHighRepJobPolicyUnappliedJobPercentage(0)); 	   	

    @Before
    public void setUp() {
        helper.setUp();
    }

    @After
    public void tearDown() {
        helper.tearDown();
    }	
	
	@Test
	public void testKindKey() {
        Key testKey = KeyStructure.createKey("TestKind", "TestKey");
        assertEquals("TestKind", testKey.getKind());
        assertEquals("TestKey", testKey.getName());
	}

    @Test
    public void testParent() {
        Key parent = KeyStructure.createKey("ParentKind", "ParentKey");
        Key child = KeyStructure.createKey(parent, "ChildKind", "ChildKey");

        assertEquals("ParentKind", parent.getKind());
        assertEquals("ParentKey", parent.getName());
        assertEquals("ChildKind", child.getKind());
        assertEquals("ChildKey", child.getName());
        assertEquals(parent.getName(), child.getParent().getName());
    }

    @Test
    public void testLongKeys(){
        Key parent = KeyStructure.createKey("ParentKind", 1L);
        assertNotNull(parent);
        assertEquals(1L, parent.getId());
        parent.getName();
    }

    @Test
    public void testAutoLongKeys(){
        Key parent = KeyStructure.createKey("TestKind", KeyStructure.autoLongId("TestKind"));
        assertNotNull(parent);

    }
	
}
