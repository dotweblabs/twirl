package org.appobjects.util;

import org.appobjects.LocalDatastoreTestCase;
import org.appobjects.TestData;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by kerby on 4/23/14.
 */
public class RegexTest extends LocalDatastoreTestCase {
    @Test
    public void testGetClassNameFromString(){
        String result = StringHelper.getClassNameFrom(TestData.RootEntity.class.getName());
        assertEquals("TestData$RootEntity" , result);
    }
    @Test
    public void testGetClassNameFromObject(){
        String result = StringHelper.getClassNameOf(new TestData.RootEntity());
        assertEquals("TestData$RootEntity" , result);
    }
}
