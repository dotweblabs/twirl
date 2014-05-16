package org.appobjects;

/**
 * Created by kerby on 5/16/14.
 */
public class ObjectStoreFactory {
    public ObjectStore begin(){
        return new GaeObjectStore();
    }
}
