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
package com.textquo.twist;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.textquo.twist.annotations.Cached;
import com.textquo.twist.annotations.Child;
import com.textquo.twist.annotations.Entity;
import com.textquo.twist.util.AnnotationUtil;
import com.textquo.twist.util.AnnotationUtil.AnnotatedField;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

public class AnnotationUtilTest {
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
	public void test() {
        TestData.RootEntity f = new TestData.RootEntity();
        List<AnnotationUtil.AnnotatedField> fields = AnnotationUtil.getFieldsWithAnnotation(Child.class, f);
        assertTrue(!fields.isEmpty());

	}

    @Test
    public void test_Entity_annotation(){
        TestData.RootEntityWithAnno e = new TestData.RootEntityWithAnno();
        TestData.Post e1 = new TestData.Post();
        assertTrue(AnnotationUtil.isClassAnnotated(Entity.class, e));
        assertTrue(AnnotationUtil.isClassAnnotated(Cached.class, e1));
    }

    @Test
    public void testGetAnnotatedField() {
        TestData.RootEntity f = new TestData.RootEntity();
        f.setNewChildEntity(new TestData.ChildEntity("Test City"));
        AnnotatedField field = AnnotationUtil.getFieldWithAnnotation(Child.class, f);
        assertNotNull(field);
        TestData.ChildEntity childEntity = (TestData.ChildEntity) field.getFieldValue();
        assertEquals("Test City", childEntity.getType());
    }

    @Test
    public void testUpdateAnnotatedField() {
        TestData.RootEntity f = new TestData.RootEntity();
        f.setNewChildEntity(new TestData.ChildEntity("Test City"));
        AnnotatedField field = AnnotationUtil.getFieldWithAnnotation(Child.class, f);
        assertNotNull(field);
        TestData.ChildEntity childEntity = (TestData.ChildEntity) field.getFieldValue();
        childEntity.setType("New Test City");
        field.setFieldValue(childEntity);

        field = AnnotationUtil.getFieldWithAnnotation(Child.class, f);
        assertNotSame("Test City", ((TestData.ChildEntity) field.getFieldValue()).getType());
        assertEquals("New Test City", ((TestData.ChildEntity) field.getFieldValue()).getType());
    }
}
