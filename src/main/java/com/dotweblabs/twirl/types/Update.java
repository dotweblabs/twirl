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

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.dotweblabs.twirl.object.QueryStore;
import com.dotweblabs.twirl.GaeObjectStore;
import com.dotweblabs.twirl.util.Pair;
import org.boon.collections.MultiMap;
import org.boon.collections.MultiMapImpl;
import org.boon.json.JsonParser;
import org.boon.json.JsonParserFactory;

import java.util.*;

import static org.boon.Boon.puts;

/**
 * Created by kerby on 4/29/14.
 */
public class Update<V> {

    final JsonParser mapper = new JsonParserFactory().create();

    protected MultiMap<String, Pair<Query.FilterOperator, Object>> filters;
    protected Map<String, Query.SortDirection> sorts;
    protected Object ref;

    private Key _ancestor;

    final GaeObjectStore objectStore;
    final QueryStore _store;
    final Class<V> _clazz;
    final String _kind;

    public Update(GaeObjectStore store, Class<V> clazz, String kind, Key ancestor){
        this(store, clazz, kind);
        _ancestor = ancestor;
    }

    public Update(GaeObjectStore store, Class<V> clazz, String kind){
        filters = new MultiMapImpl<String, Pair<Query.FilterOperator, Object>>();
        sorts = new LinkedHashMap<String, Query.SortDirection>();
        objectStore = store;
        _store = new QueryStore(store.getDatastoreService(), null);
        _clazz = clazz;
        _kind = kind;
    }

    public Update greaterThan(String key, Object value){
        filters.put(key, new Pair<Query.FilterOperator, Object>(Query.FilterOperator.GREATER_THAN, value));
        return this;
    }

    public Update greaterThanOrEqual(String key, Object value){
        filters.put(key, new Pair<Query.FilterOperator, Object>(Query.FilterOperator.GREATER_THAN_OR_EQUAL, value));
        return this;
    }

    public Update lessThan(String key, Object value){
        filters.put(key, new Pair<Query.FilterOperator, Object>(Query.FilterOperator.LESS_THAN, value));
        return this;
    }

    public Update lessThanOrEqual(String key, Object value){
        filters.put(key, new Pair<Query.FilterOperator, Object>(Query.FilterOperator.LESS_THAN_OR_EQUAL, value));
        return this;
    }

    public Update in(String key, Object value){
        filters.put(key, new Pair<Query.FilterOperator, Object>(Query.FilterOperator.IN, value));
        return this;
    }

    public Update equal(String key, Object value){
        filters.put(key, new Pair<Query.FilterOperator, Object>(Query.FilterOperator.EQUAL, value));
        return this;
    }

    public Update notEqual(String key, Object value){
        filters.put(key, new Pair<Query.FilterOperator, Object>(Query.FilterOperator.NOT_EQUAL, value));
        return this;
    }

    /**
     * Operator increments a value by a specified amount
     *
     * @param field to increment
     * @param value to set
     * @return
     */
    public Update increment(String field, long value){
        return this;
    }

    /**
     * Operator multiplies a value by specified amount
     *
     * @param field to increment
     * @param value to set
     * @return
     */
    public Update multiply(String field, float value){
        return this;
    }


    public Update set(String field, Object value){
        return this;
    }

    public Update unset(String field, Object value){
        return this;
    }

    /**
     * Operator updates queried entity with the values of the
     * reference object {@code ref}
     *
     * @param ref Reference object
     * @return
     */
    public Update with(Object ref){
        this.ref = ref;
        return this;
    }

    public Iterator<V> now() {
        if (filters == null){
            filters = new MultiMapImpl<String, Pair<Query.FilterOperator, Object>>();
        }
        final Set<Map.Entry<Object,Entity>> entities
                = objectStore.marshaller().marshall(null, ref).entrySet();
        /**
         * Map of fields and its matching filter operator and compare valueType
         */
        Iterator<V> it = null;
        try {
            final Iterator<Entity> eit
                    = (Iterator<Entity>) _store.querySortedLike(
                        _ancestor, _kind, filters, sorts,
                        null, null, null, false, false);
            it = new Iterator<V>() {
                public void remove() {
                    eit.remove();
                }
                public V next() {
                    Entity e = eit.next();

                    String kind = e.getKind();
                    Iterator<Map.Entry<Object,Entity>> it = entities.iterator();
                    while(it.hasNext()){
                        Map.Entry<Object,Entity> entry = it.next();
                        Object key = entry.getKey();
                        Entity value = entry.getValue();
                        if(kind.equals(value.getKind())){ // if same kind, update the values
                            puts("found same kind: " + value.getKind() + "entity key: " + e.getKey()
                                    + " kind: " + e.getKind());
                            e.setPropertiesFrom(value);
                        }
                    }

                    V instance = createInstance(_clazz);
                    objectStore.unmarshaller().unmarshall(instance, e);
                    objectStore.put(instance);
                    return instance;
                }
                public boolean hasNext() {
                    return eit.hasNext();
                }
            };
        } catch (Exception e) {
            // TODO Handle exception
            e.printStackTrace();
            it = null;
        } finally {

        }
        return it;
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
