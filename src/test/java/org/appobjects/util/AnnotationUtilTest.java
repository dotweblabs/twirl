package org.appobjects.util;

import org.appobjects.LocalDatastoreTestCase;
import org.appobjects.TestData;
import org.appobjects.annotations.Entity;
import org.junit.Test;

import java.lang.annotation.Annotation;
import static org.junit.Assert.*;

/**
 * Created by kerby on 4/23/14.
 */
public class AnnotationUtilTest extends LocalDatastoreTestCase {
    @Test
    public void test(){
        Annotation a = AnnotationUtil.getClassAnnotation(Entity.class, new TestData.RootEntityWithNoAnno());
        assertNotNull(a);
        assertTrue(a instanceof Entity);
        Entity e = (Entity) a;
        assertEquals("", e.name());
    }

    @Test
    public void testWithCustomName(){
        Annotation a = AnnotationUtil.getClassAnnotation(Entity.class, new TestData.RootEntityWithAnno());
        assertNotNull(a);
        assertTrue(a instanceof Entity);
        Entity e = (Entity) a;
        assertEquals("CustomEntityName", e.name());
    }
}
