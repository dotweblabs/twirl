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
package com.dotweblabs.twirl.util;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

import com.dotweblabs.twirl.LocalDatastoreTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Tests for the {@link EntityUtil} class.
 *
 */
public class EntityUtilTest extends LocalDatastoreTestBase {
  
  private DatastoreService ds;

  @Before
  public void setupDatastore()  {
    super.setupDatastore();
    ds = DatastoreServiceFactory.getDatastoreService();
  }
  @After
  public void tearDownDatastore()  {
    ds = null;
    super.tearDownDatastore();
  }

  @Test
  public void testInSameEntityGroup() {
    Entity ancestor = new Entity("foo");
    ds.put(ancestor);
    
    Entity e1 = new Entity("bar", ancestor.getKey());
    ds.put(e1);
    
    Entity e2 = new Entity("baz", "Bob", e1.getKey());
    Entity e3 = new Entity("foo");
    ds.put(new ArrayList<Entity>(Arrays.asList(e2, e3)));
    
    assertTrue(EntityUtil.inSameEntityGroup(ancestor, e1, e2));
    assertFalse(EntityUtil.inSameEntityGroup(ancestor, e1, e2, e3));
  }
  
}
