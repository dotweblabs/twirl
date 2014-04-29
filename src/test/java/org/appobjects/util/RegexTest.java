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
        String result = StringHelper.getClassNameOf(new TestData.RootEntity());
        assertEquals("RootEntity" , result);
    }


}
