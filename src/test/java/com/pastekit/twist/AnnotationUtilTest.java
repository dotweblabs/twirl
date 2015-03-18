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
package com.pastekit.twist;

import com.pastekit.twist.annotations.Child;
import com.pastekit.twist.entity.ChildEntity;
import com.pastekit.twist.entity.RootEntity;
import com.pastekit.twist.entity.RootEntityWithAnno;
import com.pastekit.twist.util.AnnotationUtil;
import com.pastekit.twist.annotations.Cached;
import com.pastekit.twist.annotations.Child;
import com.pastekit.twist.annotations.Entity;
import com.pastekit.twist.entity.ChildEntity;
import com.pastekit.twist.entity.Post;
import com.pastekit.twist.entity.RootEntity;
import com.pastekit.twist.entity.RootEntityWithAnno;
import com.pastekit.twist.util.AnnotationUtil;
import com.pastekit.twist.util.AnnotationUtil.AnnotatedField;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

public class AnnotationUtilTest extends LocalDatastoreTestBase{
	
	@Test
	public void test() {
        RootEntity f = new RootEntity();
        List<AnnotationUtil.AnnotatedField> fields = AnnotationUtil.getFieldsWithAnnotation(Child.class, f);
        assertTrue(!fields.isEmpty());

	}

    @Test
    public void test_Entity_annotation(){
        RootEntityWithAnno e = new RootEntityWithAnno();
        Post e1 = new Post();
        assertTrue(AnnotationUtil.isClassAnnotated(Entity.class, e));
        assertTrue(AnnotationUtil.isClassAnnotated(Cached.class, e1));
    }

    @Test
    public void testGetAnnotatedField() {
        RootEntity f = new RootEntity();
        f.setNewChildEntity(new ChildEntity("Test City"));
        AnnotationUtil.AnnotatedField field = AnnotationUtil.getFieldWithAnnotation(Child.class, f);
        assertNotNull(field);
        ChildEntity childEntity = (ChildEntity) field.getFieldValue();
        assertEquals("Test City", childEntity.getType());
    }

    @Test
    public void testUpdateAnnotatedField() {
        RootEntity f = new RootEntity();
        f.setNewChildEntity(new ChildEntity("Test City"));
        AnnotationUtil.AnnotatedField field = AnnotationUtil.getFieldWithAnnotation(Child.class, f);
        assertNotNull(field);
        ChildEntity childEntity = (ChildEntity) field.getFieldValue();
        childEntity.setType("New Test City");
        field.setFieldValue(childEntity);

        field = AnnotationUtil.getFieldWithAnnotation(Child.class, f);
        assertNotSame("Test City", ((ChildEntity) field.getFieldValue()).getType());
        assertEquals("New Test City", ((ChildEntity) field.getFieldValue()).getType());
    }
}
