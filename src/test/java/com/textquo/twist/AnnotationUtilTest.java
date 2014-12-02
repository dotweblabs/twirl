package com.textquo.twist;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.textquo.twist.annotations.Child;
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
