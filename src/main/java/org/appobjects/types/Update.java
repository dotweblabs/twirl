package org.appobjects.types;

import com.google.appengine.api.datastore.Query;
import org.appobjects.object.QueryStore;
import org.appobjects.util.Pair;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by kerby on 4/29/14.
 */
public class Update {

    protected Map<String, Pair<Query.FilterOperator, Object>> filters;
    final QueryStore _store;
    final Class<?> _clazz;
    final String _kind;

    public Update(QueryStore store, Class<?> clazz, String kind){
        filters = new LinkedHashMap<String, Pair<Query.FilterOperator, Object>>();
        _store = store;
        _clazz = clazz;
        _kind = kind;
    }

    public Update greaterThan(String key, Object value){
        filters.put(key, new Pair<Query.FilterOperator, Object>(Query.FilterOperator.GREATER_THAN, value));
        return this;
    }

    public Update greaterThanOrEqual(String key, Object value){
        filters.put(key, new Pair<Query.FilterOperator, Object>(Query.FilterOperator.GREATER_THAN, value));
        return this;
    }

    public Update lessThan(String key, Object value){
        filters.put(key, new Pair<Query.FilterOperator, Object>(Query.FilterOperator.LESS_THAN, value));
        return this;
    }

    public Update lessThanOrEqual(String key, Object value){
        filters.put(key, new Pair<Query.FilterOperator, Object>(Query.FilterOperator.LESS_THAN, value));
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
     * @param field
     * @param value
     * @return
     */
    public Update increment(String field, long value){
        return this;
    }

    /**
     * Operator multiplies a value by specified amount
     *
     * @param field
     * @param value
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
     * @param ref
     * @return
     */
    public Update with(Object ref){
        filters.clear();
        return this;
    }

    public <V> Iterator<V> now() {
        throw new RuntimeException("Not yet implemented");
    }
}
