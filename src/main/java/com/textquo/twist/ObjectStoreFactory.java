package com.textquo.twist;

/**
 * Factory class
 */
public class ObjectStoreFactory {
    public ObjectStore begin(){
        return new GaeObjectStore();
    }
}
