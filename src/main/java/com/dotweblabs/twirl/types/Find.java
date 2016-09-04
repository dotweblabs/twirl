/**
 *
 * Copyright (c) 2016 Dotweblabs Web Technologies and others. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  __            __       __
 * |  |_.--.--.--|__.-----|  |
 * |   _|  |  |  |  |   --|  |_
 * |____|________|__|___| |____|
 * :: twirl :: Object Mapping ::
 *
 */

package com.dotweblabs.twirl.types;

import com.google.appengine.api.datastore.*;
import com.dotweblabs.twirl.object.QueryStore;
import com.dotweblabs.twirl.GaeObjectStore;
import com.dotweblabs.twirl.util.CollectionUtil;
import com.dotweblabs.twirl.util.Pair;
import org.boon.collections.MultiMap;
import org.boon.collections.MultiMapImpl;

import java.util.*;

/**
 * Builds GAE filters and sorts
 */
public class Find<V> {

    protected MultiMap<String, Pair<Query.FilterOperator, Object>> filters;
    protected Map<String, Query.SortDirection> sorts;
    protected List<String> projections;
    protected Integer skip;
    protected Integer max;
    protected boolean keysOnly = false;
    protected Key _ancestor;
    protected Cursor cursor;

    private final GaeObjectStore objectStore;
    private final QueryStore _store;
    private final Class<V> _clazz;
    private final String _kind;

    public Find(GaeObjectStore store, Class<V> clazz, String kind, Key ancestor){
        this(store, clazz, kind);
        _ancestor = ancestor;
    }

    public Find(GaeObjectStore store, Class<V> clazz, String kind){
        filters = new MultiMapImpl<String, Pair<Query.FilterOperator, Object>>();
        sorts = new LinkedHashMap<String, Query.SortDirection>();
        projections = new LinkedList<String>();
        objectStore = store;
        _store = new QueryStore(store.getDatastoreService(), null);
        _clazz = clazz;
        _kind = kind;
    }

    public Find greaterThan(String key, Object value){
        filters.put(key, new Pair<Query.FilterOperator, Object>(Query.FilterOperator.GREATER_THAN, value));
        return this;
    }

    public Find greaterThanOrEqual(String key, Object value){
        filters.put(key, new Pair<Query.FilterOperator, Object>(Query.FilterOperator.GREATER_THAN_OR_EQUAL, value));
        return this;
    }

    public Find lessThan(String key, Object value){
        filters.put(key, new Pair<Query.FilterOperator, Object>(Query.FilterOperator.LESS_THAN, value));
        return this;
    }

    public Find lessThanOrEqual(String key, Object value){
        filters.put(key, new Pair<Query.FilterOperator, Object>(Query.FilterOperator.LESS_THAN_OR_EQUAL, value));
        return this;
    }

    public Find in(String key, Object value){
        filters.put(key, new Pair<Query.FilterOperator, Object>(Query.FilterOperator.IN, value));
        return this;
    }

    public Find equal(String key, Object value){
        filters.put(key, new Pair<Query.FilterOperator, Object>(Query.FilterOperator.EQUAL, value));
        return this;
    }

    public Find notEqual(String key, Object value){
        if(value instanceof List){
            for(Object o : (List)value){
                filters.put(key, new Pair<Query.FilterOperator, Object>(Query.FilterOperator.NOT_EQUAL, o));
            }
        } else {
            filters.put(key, new Pair<Query.FilterOperator, Object>(Query.FilterOperator.NOT_EQUAL, value));
        }
        return this;
    }

    public Find notEqual(String key, Object... value){
        return notEqual(key, value);
    }

    public Find sortAscending(String key){
        sorts.put(key, Query.SortDirection.ASCENDING);
        return this;
    }

    public Find sortDescending(String key){
        sorts.put(key, Query.SortDirection.DESCENDING);
        return this;
    }

    public Find skip(int skip){
        this.skip = skip;
        return this;
    }

    public Find limit(int limit){
        this.max = limit;
        return this;
    }

    public Find keysOnly(){
        this.keysOnly = true;
        return this;
    }

    public Find projection(String field){
        throw new RuntimeException("Not yet implemented");
    }

    public Find projection(String[]fields){
        throw new RuntimeException("Not yet implemented");
    }

    public Find projection(Iterable<String> fields){
        throw new RuntimeException("Not yet implemented");
    }

    public Find withCursor(Cursor cursor){
        this.cursor = cursor;
        return this;
    }

    public Find withCursor(String webSafeCursor){
        this.cursor = new Cursor(webSafeCursor);
        return this;
    }

    public Find withCursor(){
        this.cursor = new Cursor();
        return this;
    }

    public Iterator<V> now() {
        if (filters == null){
            filters = new MultiMapImpl<String, Pair<Query.FilterOperator, Object>>();
        }
        /**
         * Map of fields and its matching filter operator and compare valueType
         */
        Iterator<V> it = null;
        // TODO: How to pass this back to the caller?
        Cursor nextCursor = null;
        try {
            if (sorts == null){
                sorts = new HashMap<String, Query.SortDirection>();
            }
            if(cursor != null){
                final QueryResultIterator<Entity> eit
                        = (QueryResultIterator<Entity>) _store.querySortedLike(
                        _ancestor, _kind, filters, sorts,
                        max, skip, cursor,
                        keysOnly, false);
                nextCursor = new Cursor(eit.getCursor().toWebSafeString());
                it = new Iterator<V>() {
                    public void remove() {
                        eit.remove();
                    }
                    public V next() {
                        Entity e = eit.next();
                        V instance = null;
                        if(_clazz.equals(Map.class)){
                            instance = (V) new LinkedHashMap<>();
                        } else {
                            instance = createInstance(_clazz);
                        }
                        objectStore.unmarshaller().unmarshall(instance, e);
                        return instance;
                    }
                    public boolean hasNext() {
                        return eit.hasNext();
                    }
                };
            } else {
                final Iterator<Entity> eit
                        = (Iterator<Entity>) _store.querySortedLike(_ancestor, _kind, filters, sorts, max, skip, null, keysOnly, false);
                it = new Iterator<V>() {
                    public void remove() {
                        eit.remove();
                    }
                    public V next() {
                        Entity e = eit.next();
                        V instance = null;
                        if(_clazz.equals(Map.class)){
                            instance = (V) new LinkedHashMap<>();
                        } else {
                            instance = createInstance(_clazz);
                        }
                        objectStore.unmarshaller().unmarshall(instance, e);
                        return instance;
                    }
                    public boolean hasNext() {
                        return eit.hasNext();
                    }
                };
            }
        } catch (Exception e) {
            // TODO Handle exception
            e.printStackTrace();
            it = null;
        } finally {

        }
        return it;
    }

    public Iterable<V> asIterable() {
        Iterator<V> it = now();
        return CollectionUtil.iterable(it);
    }

    // TODO: Add test!
    public ListResult<V> asList(){
        ListResult<V> result = new ListResult<V>();
        if (filters == null){
            filters = new MultiMapImpl<String, Pair<Query.FilterOperator, Object>>();
        }
        if(cursor != null){
            QueryResultList<Entity> entities
                    = (QueryResultList<Entity>) _store.querySortedLike(
                    _ancestor, _kind, filters, sorts,
                    max, skip, cursor, keysOnly, true);
            for(Entity e : entities){
                V instance = null;
                if(_clazz.equals(Map.class)){
                    instance = (V) new LinkedHashMap<>();
                } else {
                    instance = createInstance(_clazz);
                }
                objectStore.unmarshaller().unmarshall(instance, e);
                result.getList().add(instance);
            }
            if(entities.getCursor() != null){
                String cursor = entities.getCursor().toWebSafeString();
                result.setWebsafeCursor(cursor);
            }
        } else {
            List<Entity> entities
                    = (List<Entity>) _store.querySortedLike(
                    _ancestor, _kind, filters, sorts,
                    max, skip, null, keysOnly, true);
            for(Entity e : entities){
                V instance = null;
                if(_clazz.equals(Map.class)){
                    instance = (V) new LinkedHashMap<>();
                } else {
                    instance = createInstance(_clazz);
                }
                objectStore.unmarshaller().unmarshall(instance, e);
                result.getList().add(instance);
            }
        }

        return result;
    }

    public V first(){
        if(asList().getList().iterator().hasNext()){
            return asList().getList().iterator().next();
        }
        return null;
    }

    private <T> T createInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (IllegalAccessException e){
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

}
