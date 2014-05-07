package org.appobjects.object;

import com.google.appengine.api.datastore.EmbeddedEntity;
import org.appobjects.GaeObjectStore;
import org.appobjects.LocalDatastoreTestCase;
import org.appobjects.gae.GaeUnmarshaller;
import org.junit.Test;

import java.util.Map;
import static org.junit.Assert.*;

/**
 * Created by kerby on 4/28/14.
 */
public class UnmarshallerTest extends LocalDatastoreTestCase {


    GaeUnmarshaller unmarshaller;
    GaeObjectStore store;

    @Override
    public void setupDatastore() {
        super.setupDatastore();
        store = new GaeObjectStore();
        unmarshaller = (GaeUnmarshaller) store.unmarshaller();
    }

    @Test
    public void testCreateMapFromEmbeddedEntity(){
        EmbeddedEntity ee = new EmbeddedEntity();
        ee.setProperty("TestProperty", "TestValue");
        Object result = unmarshaller.getMapOrList(ee);
        assertTrue(result instanceof Map);
        assertEquals("TestValue", ((Map) result).get("TestProperty"));
    }

    @Test
    public void testCreateMapFromEmbeddedEntityWithKey(){
        EmbeddedEntity ee = new EmbeddedEntity();
        ee.setKey(KeyStructure.createKey("TestKind", "TestKey"));
        ee.setProperty("TestProperty", "TestValue");
        Object result = unmarshaller.getMapOrList(ee);
        assertTrue(result instanceof Map);
        assertEquals("TestValue", ((Map) result).get("TestProperty"));
    }
}

