/**
 *
 * Copyright (c) 2014 Kerby Martino and others. All rights reserved.
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
 */
package org.appobjects.types;

import org.appobjects.object.QueryStore;
import org.appobjects.util.BoundedIterator;
import org.appobjects.util.Pair;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Builds GAE filters and sorts
 */
public class Find {

    protected Map<String, Pair<Query.FilterOperator, Object>> filters;
    protected Map<String, Query.SortDirection> sorts;
    protected Integer skip;
    protected Integer max;

    final QueryStore _store;
    final Class<?> _clazz;
    final String _kind;

    public Find(QueryStore store, Class<?> clazz, String kind){
        filters = new LinkedHashMap<String, Pair<Query.FilterOperator, Object>>();
        sorts = new LinkedHashMap<String, Query.SortDirection>();
        _store = store;
        _clazz = clazz;
        _kind = kind;
    }

    public Find greaterThan(String key, Object value){
        filters.put(key, new Pair<Query.FilterOperator, Object>(Query.FilterOperator.GREATER_THAN, value));
        return this;
    }

    public Find greaterThanOrEqual(String key, Object value){
        filters.put(key, new Pair<Query.FilterOperator, Object>(Query.FilterOperator.GREATER_THAN, value));
        return this;
    }

    public Find lessThan(String key, Object value){
        filters.put(key, new Pair<Query.FilterOperator, Object>(Query.FilterOperator.LESS_THAN, value));
        return this;
    }

    public Find lessThanOrEqual(String key, Object value){
        filters.put(key, new Pair<Query.FilterOperator, Object>(Query.FilterOperator.LESS_THAN, value));
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
        filters.put(key, new Pair<Query.FilterOperator, Object>(Query.FilterOperator.NOT_EQUAL, value));
        return this;
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

    public <V> Iterator<V> now() {
        if (filters == null){
            filters = new HashMap<String, Pair<Query.FilterOperator, Object>>();
        }
        /**
         * Map of fields and its matching filter operator and compare valueType
         */
        Iterator<V> it = null;
        try {
            if (sorts == null){
                sorts = new HashMap<String, Query.SortDirection>();
            }
            final Iterator<Entity> eit = _store.querySortedLike(_kind, filters, sorts);
            it = new Iterator<V>() {
                public void remove() {
                    eit.remove();
                }
                public V next() {
                    Entity e = eit.next();
                    return null; // TODO!
                    //Map<String,Object> map = EntityMapper.createMapObjectFromEntity(e);
                    //return EntityMapper.createPOJOFrom(clazz, map);
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
        if (it == null){
            //LOG.debug("Returning null iterator");
        }
        if (max != null){
            if (skip != null){
                return new BoundedIterator<V>(skip, max, it);
            } else {
                return new BoundedIterator<V>(0, max, it);
            }
        }
        //List asList = Lists.newArrayList(it);
        return it;
    }


}
