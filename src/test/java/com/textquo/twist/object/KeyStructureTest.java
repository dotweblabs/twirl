package com.textquo.twist.object;

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
