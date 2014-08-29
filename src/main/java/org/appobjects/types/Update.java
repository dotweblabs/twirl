package org.appobjects.types;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.gson.Gson;
import org.appobjects.GaeObjectStore;
import org.appobjects.object.QueryStore;
import org.appobjects.util.BoundedIterator;
import org.appobjects.util.Pair;
import org.boon.json.JsonParser;
import org.boon.json.JsonParserFactory;

import java.util.*;

import static org.boon.Boon.puts;

/**
 * Created by kerby on 4/29/14.
 */
public class Update<V> {

    final JsonParser mapper = new JsonParserFactory().create();

    protected Map<String, Pair<Query.FilterOperator, Object>> filters;
    protected Map<String, Query.SortDirection> sorts;
    protected Object ref;

    final GaeObjectStore objectStore;
    final QueryStore _store;
    final Class<V> _clazz;
    final String _kind;

    public Update(GaeObjectStore store, Class<V> clazz, String kind){
        filters = new LinkedHashMap<String, Pair<Query.FilterOperator, Object>>();
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
        //filters.clear();
        this.ref = ref;
        return this;
    }

    public Iterator<V> now() {
        if (filters == null){
            filters = new HashMap<String, Pair<Query.FilterOperator, Object>>();
        }
        final Set<Map.Entry<Object,Entity>> entities
                = objectStore.marshaller().marshall(null, ref).entrySet();
        /**
         * Map of fields and its matching filter operator and compare valueType
         */
        Iterator<V> it = null;
        try {
            final Iterator<Entity> eit = _store.querySortedLike(_kind, filters, sorts);
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
        if (it == null){
            //LOG.debug("Returning null iterator");
        }
//        if (max != null){
//            if (skip != null){
//                return new BoundedIterator<V>(skip, max, it);
//            } else {
//                return new BoundedIterator<V>(0, max, it);
//            }
//        }
        //List asList = Lists.newArrayList(it);
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
