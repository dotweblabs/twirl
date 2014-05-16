package org.appobjects;

/**
 * Factory class
 */
public class ObjectStoreFactory {
    public ObjectStore begin(){
        return new GaeObjectStore();
    }
}
