package org.appobjects.object;

import static org.junit.Assert.*;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import org.appobjects.Marshaller;
import org.appobjects.TestData;
import org.appobjects.TestData.RootEntity;
import org.appobjects.TestData.ChildEntity;
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
    public void testCreateMapFromPOJO(){

    }

    @Test
    public void testMarshallChild(){
        RootEntity rootObject = new RootEntity(); // one Entity
        ChildEntity childObject = new TestData.ChildEntity("Test City");
        rootObject.setKey("TestUser");
        rootObject.setCount(25);
        childObject.setParent(rootObject);
        rootObject.setNewChildEntity(childObject); // one Entity

        IdentityHashMap<Object,Entity> stack = testMarshaller.marshall(null, rootObject);

        Entity rootObjectEntity = stack.get(rootObject);
        Entity childObjectEntity = stack.get(childObject);

        Key childKey = (Key) rootObjectEntity.getProperty("newChildEntity");
        Key parentKey = childKey.getParent();

        assertEquals(2, stack.size());
        assertNotNull(parentKey);
        assertTrue(childKey instanceof Key);
        assertEquals(rootObjectEntity.getKey().getId(), parentKey.getId());
        assertEquals(rootObjectEntity.getKey().getName(), parentKey.getName());
    }

    @Test
    public void testMarshallEmbedded(){
        RootEntity rootObject = new RootEntity(); // one Entity
        ChildEntity childObject = new TestData.ChildEntity("Test City");
        ChildEntity embeddedObject = new TestData.ChildEntity("Old Test City");
        embeddedObject.setId(1L);
        embeddedObject.setType("EmbeddedType");
        rootObject.setKey("TestUser");
        rootObject.setCount(25);
        childObject.setParent(rootObject);
        rootObject.setNewChildEntity(childObject); // one Entity
        rootObject.setOldChildEntity(embeddedObject); // not included, @Embedded

        IdentityHashMap<Object,Entity> stack = testMarshaller.marshall(null, rootObject);

        Entity rootObjectEntity = stack.get(rootObject);
        Entity childObjectEntity = stack.get(childObject);

        EmbeddedEntity ee = (EmbeddedEntity) rootObjectEntity.getProperty("oldChildEntity");
        Key childKey = (Key) rootObjectEntity.getProperty("newChildEntity");
        Key parentKey = childKey.getParent();

        assertEquals(2, stack.size());
        assertNotNull(parentKey);
        assertEquals(EmbeddedEntity.class, ee.getClass());
        assertTrue(childKey instanceof Key);
        assertEquals(rootObjectEntity.getKey().getId(), parentKey.getId());
        assertEquals(rootObjectEntity.getKey().getName(), parentKey.getName());

        assertEquals(1L, ee.getProperties().get("id"));
        assertEquals("EmbeddedType", ee.getProperties().get("type"));

    }




}
