package org.appobjects;

import java.util.ArrayDeque;
import java.util.Deque;

public class ObjectStoreService {

    private static ObjectStoreFactory factory = new ObjectStoreFactory();

    private static final ThreadLocal<Deque<ObjectStore>> STACK = new ThreadLocal<Deque<ObjectStore>>() {
        @Override
        protected Deque<ObjectStore> initialValue() {
            return new ArrayDeque<ObjectStore>();
        }
    };

    public ObjectStoreService(){

    }

    public static ObjectStore store(){
        Deque<ObjectStore> stack = STACK.get();
        if (stack.isEmpty())
            stack.add(factory.begin());
        return stack.getLast();
    }
}
