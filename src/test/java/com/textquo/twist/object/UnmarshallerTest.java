package com.textquo.twist.object;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.textquo.twist.GaeObjectStore;
import com.textquo.twist.LocalDatastoreTestCase;
import com.textquo.twist.gae.GaeUnmarshaller;
import org.junit.Test;

import java.util.LinkedHashMap;
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

    @Test
    public void testCreateMapFromEntity(){
        Entity source = new Entity("TestKind");
        source.setProperty("TestProperty", "TestValue");
        source.setProperty("TestProperty2", "TestValue2");
        Map<String,Object> destination = new LinkedHashMap<>();

        unmarshaller.unmarshall(destination, source);

        assertEquals("TestValue", destination.get("TestProperty"));
        assertEquals("TestValue2", destination.get("TestProperty2"));
    }
}

