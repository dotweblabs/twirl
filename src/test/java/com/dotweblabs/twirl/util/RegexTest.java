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

import com.dotweblabs.twirl.LocalDatastoreTestBase;
import com.dotweblabs.twirl.entity.RootEntity;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by kerby on 4/23/14.
 */
public class RegexTest extends LocalDatastoreTestBase {
    @Test
    public void testGetClassNameFromString(){
        String result = StringHelper.getClassNameFrom(RootEntity.class.getName());
        assertEquals("RootEntity" , result);
    }

    @Test
    public void testGetClassNameFromString_manual(){
        String result1 = StringHelper.getClassNameFrom("com.domain.app.RootEntity");
        String result2 = StringHelper.getClassNameFrom("com.domain.app.TestData$RootEntity");
        String result3 = StringHelper.getClassNameFrom("com.domain.app.TestData$TestNested$RootEntity");

        assertEquals("RootEntity" , result1);
        assertEquals("RootEntity" , result2);
        assertEquals("RootEntity" , result3);
    }

    @Test
    public void testGetClassNameFromObject(){
        String result = StringHelper.getClassNameOf(new RootEntity());
        assertEquals("RootEntity" , result);
    }


}
