package com.hunchee.twist.util;

import java.util.Iterator;

/**
 * Created by kerby on 9/30/2015.
 */
public class CollectionUtil {
    public static <E> Iterable<E> iterable(final Iterator<E> iterator) {
        if (iterator == null) {
            throw new NullPointerException();
        }
        return new Iterable<E>() {
            public Iterator<E> iterator() {
                return iterator;
            }
        };
    }
}
