package org.appobjects.object;

import static org.junit.Assert.*;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import org.appobjects.Marshaller;
import org.appobjects.TestData;
import org.appobjects.TestData.RootEntity;
import org.appobjects.gae.GaeMarshaller;
import org.appobjects.LocalDatastoreTestCase;
import org.junit.Test;

import java.util.IdentityHashMap;

/**
 * Created by kerby on 4/23/14.
 */
public class MarshallerTest extends LocalDatastoreTestCase {

    Marshaller testMarshaller =  new GaeMarshaller();

    @Test
    public void testMarshallChildEmbedded(){

        RootEntity rootObject = new RootEntity(); // one Entity

        TestData.ChildEntity childObject = new TestData.ChildEntity("Test City");
        TestData.ChildEntity embeddedObject = new TestData.ChildEntity("Old Test City");

        rootObject.setKey("TestUser");
        rootObject.setCount(25);
        rootObject.setNewChildEntity(childObject); // one Entity
        rootObject.setOldChildEntity(embeddedObject); // not included, @Embedded

        IdentityHashMap<Object,Entity> stack = testMarshaller.marshall(null, rootObject);

        Entity rootObjectEntity = stack.get(rootObject);
        Entity childObjectEntity = stack.get(childObject);

        Object expectedEmbeddedEntity = rootObjectEntity.getProperty("oldTestEntity");

        Key childKey = (Key) rootObjectEntity.getProperty("newTestEntity");
        Key parentKey = childKey.getParent();

        assertEquals(2, stack.size());
        assertNotNull(parentKey);
        assertEquals(EmbeddedEntity.class, expectedEmbeddedEntity.getClass());
        assertTrue(childKey instanceof Key);
        assertEquals(rootObjectEntity.getKey().getId(), parentKey.getId());
        assertEquals(rootObjectEntity.getKey().getName(), parentKey.getName());

    }


}
