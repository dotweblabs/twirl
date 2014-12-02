package com.textquo.twist.object;

import com.google.appengine.api.datastore.*;
import com.textquo.twist.LocalDatastoreTestCase;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 * Created by kerby on 5/2/14.
 */
public class EntityTest extends LocalDatastoreTestCase {

    DatastoreService _ds = DatastoreServiceFactory.getDatastoreService();

    @Test
    public void test(){
        Entity e = new Entity(KeyFactory.createKey("TestKind", "TestKey"));
        Key key = _ds.put(e);
        try {
            Entity test = _ds.get(key);
            assertNotNull(test);
        } catch (EntityNotFoundException e1) {
            e1.printStackTrace();
        }
    }
}
