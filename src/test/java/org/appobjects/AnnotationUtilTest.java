package org.appobjects;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import org.appobjects.annotations.Child;
import org.appobjects.util.AnnotationUtil;
import org.appobjects.util.AnnotationUtil.AnnotatedField;
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
        f.setNewTestEntity(new TestData.TestEntity("Test City"));
        AnnotatedField field = AnnotationUtil.getFieldWithAnnotation(Child.class, f);
        assertNotNull(field);
        TestData.TestEntity testEntity = (TestData.TestEntity) field.getFieldValue();
        assertEquals("Test City", testEntity.getType());
    }

    @Test
    public void testUpdateAnnotatedField() {
        TestData.RootEntity f = new TestData.RootEntity();
        f.setNewTestEntity(new TestData.TestEntity("Test City"));
        AnnotatedField field = AnnotationUtil.getFieldWithAnnotation(Child.class, f);
        assertNotNull(field);
        TestData.TestEntity testEntity = (TestData.TestEntity) field.getFieldValue();
        testEntity.setType("New Test City");
        field.setFieldValue(testEntity);

        field = AnnotationUtil.getFieldWithAnnotation(Child.class, f);
        assertNotSame("Test City", ((TestData.TestEntity) field.getFieldValue()).getType());
        assertEquals("New Test City", ((TestData.TestEntity) field.getFieldValue()).getType());
    }
}
