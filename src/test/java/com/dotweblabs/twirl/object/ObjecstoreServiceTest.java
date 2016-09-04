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

import com.dotweblabs.twirl.LocalDatastoreTestBase;
import com.dotweblabs.twirl.TestData;
import com.dotweblabs.twirl.entity.RootEntity;
import org.junit.Test;

import static org.junit.Assert.*;

import static com.dotweblabs.twirl.ObjectStoreService.store;
/**
 * Created by kerby on 5/16/14.
 */
public class ObjecstoreServiceTest extends LocalDatastoreTestBase {
    @Test
    public void testStore(){
        RootEntity entity = TestData.createTestRootEnity();
        store().put(entity);
        RootEntity one = store().get(RootEntity.class, "TestRoot");
        
        assertNotNull(one);
    }
}
