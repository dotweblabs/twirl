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
package com.dotweblabs.twirl;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.dotweblabs.twirl.common.ObjectNotFoundException;
import com.dotweblabs.twirl.gae.GaeMarshaller;
import com.dotweblabs.twirl.gae.GaeUnmarshaller;
import com.dotweblabs.twirl.object.KeyStructure;
import com.dotweblabs.twirl.serializer.ObjectSerializer;
import com.dotweblabs.twirl.types.Find;
import com.dotweblabs.twirl.types.Update;
import com.dotweblabs.twirl.util.AnnotationUtil;
import com.dotweblabs.twirl.util.StringHelper;
import com.dotweblabs.twirl.wrappers.PrimitiveWrapper;
import com.dotweblabs.twirl.types.Function;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

import static org.boon.Lists.list;

public class GaeObjectStore implements ObjectStore {

    public static Class<com.dotweblabs.twirl.annotations.Entity> entity(){
        return com.dotweblabs.twirl.annotations.Entity.class;
    }

    public static Class<com.dotweblabs.twirl.annotations.ObjectId> objectId(){
        return com.dotweblabs.twirl.annotations.ObjectId.class;
    }

    public static Class<com.dotweblabs.twirl.annotations.Id> key(){
        return com.dotweblabs.twirl.annotations.Id.class;
    }

    public static Class<com.dotweblabs.twirl.annotations.Kind> kind(){
        return com.dotweblabs.twirl.annotations.Kind.class;
    }

    public static Class<com.dotweblabs.twirl.annotations.Parent> parent(){
        return com.dotweblabs.twirl.annotations.Parent.class;
    }

    public static Class<com.dotweblabs.twirl.annotations.Ancestor> ancestor(){
        return com.dotweblabs.twirl.annotations.Ancestor.class;
    }

    public static Class<com.dotweblabs.twirl.annotations.Child> child(){
        return com.dotweblabs.twirl.annotations.Child.class;
    }

    public static Class<com.dotweblabs.twirl.annotations.Unindexed> unIndexed(){
        return com.dotweblabs.twirl.annotations.Unindexed.class;
    }

    private IdentityHashMap<Class<?>,String> cls = new IdentityHashMap<Class<?>,String>();

    protected static Logger LOG = LogManager.getLogger(GaeObjectStore.class.getName());
    public static String KEY_RESERVED_PROPERTY = Entity.KEY_RESERVED_PROPERTY;
    public static String KIND_RESERVED_PROPERTY = "__kind__";
    public static String NAMESPACE_RESERVED_PROPERTY = "__namespace__";

    protected DatastoreService _ds;
    protected MemcacheService _cache;
    protected static TransactionOptions _options;
    protected ObjectSerializer _serializer;

    /**
     * GAE Datastore supported types.
     */
    protected static final Set<Class<?>> GAE_SUPPORTED_TYPES =
            DataTypeUtils.getSupportedTypes();

    public GaeObjectStore(){
        if (_ds == null) {
            _ds = DatastoreServiceFactory.getDatastoreService();
            _options = TransactionOptions.Builder.withXG(true);
            LOG.debug("Create a new DatastoreService instance");
        }
        if(_cache == null){
            _cache = MemcacheServiceFactory.getMemcacheService();
        }
    }

    @Override
    public void delete(Key key) {
        _ds.delete(key);
    }

    @Override
    public void delete(Key... keys) {
        delete(list(keys));
    }

    @Override
    public <T> void delete(Iterable<T> keysOrObjects) {
        for(Object o : keysOrObjects){
            if(o instanceof Key){
                _ds.delete((Key)o);
            } else {
                Key key = getParent(o);
                _ds.delete(key);
            }
        }
        //_ds.delete(keys);
    }

    @Override
    public void deleteInTransaction(Key key) {
        Transaction tx = _ds.beginTransaction();
        try {
            _ds.delete(key);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
        }
    }

    @Override
    public void deleteInTransaction(Key... keys) {
        Transaction tx = _ds.beginTransaction();
        try {
            _ds.delete(list(keys));
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
        }
    }

    @Override
    public void deleteInTransaction(Iterable<Key> keys) {
        Transaction tx = _ds.beginTransaction();
        try {
            _ds.delete(keys);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
        }
    }

    @Override
    public <T> void delete(Class<T> clazz, String key) {
        String kind = getKind(clazz);
        _ds.delete(KeyStructure.createKey(kind, key));
    }

    @Override
    public <T> void delete(Class<T> clazz, Long id) {
        String kind = getKind(clazz);
        assert kind != null;
        _ds.delete(KeyStructure.createKey(kind, id));
    }

    @Override
    public <T> void delete(Class<T> clazz, String kind, Long id){
        Key key = KeyStructure.createKey(kind, id);
        _ds.delete(key);
    }

    @Override
    public <T> void delete(Class<T> clazz, String kind, String key){
        Key _key = KeyStructure.createKey(kind, key);
        _ds.delete(_key);
    }

    @Override
    public <T> Find find(Class<T> clazz, String kind, Key ancestor) {
        return new Find(this, clazz, kind, ancestor);
    }

    @Override
    public <T> Find find(Class<T> clazz, Key ancestor) {
        return new Find(this, clazz, getKind(clazz), ancestor);
    }

    @Override
    public <T> Find find(Class<T> clazz, String kind) {
        return new Find(this, clazz, kind);
    }

    @Override
    public <T> Find find(Class<T> clazz){
        return new Find(this, clazz, getKind(clazz));
    }

    @Override
    public <T> T findOne(Class<T> clazz) {
        return (T) find(clazz).first();
    }

    @Override
    public <T> Update update(Class<T> clazz){
        return new Update(this, clazz, getKind(clazz));
    }

    @Override
    public <T> T transact(Function<T> function) {
        T result = null;
        Transaction tx = _ds.beginTransaction(_options);
        try {
            result = function.execute(tx);
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (tx.isActive()) {
                tx.rollback();
            }
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
        }
        return result;
    }

    @Override
    public <T> T get(Class<T> clazz, Key key) {
    	return get(clazz, key, false);
    }
    
    private <T> T get(Class<T> clazz, Key key, boolean safe) {
        T instance = null;
        try {
            Entity e = _ds.get(key);
            instance = createInstance(clazz);
            if(isPrimitive(clazz)){
                PrimitiveWrapper<T> wrapper = new PrimitiveWrapper<T>(instance);
                unmarshaller().unmarshall(wrapper, e);
                instance = wrapper.getValue();
            } else {
                if (isCached(instance)) {
                    Object cached = _cache.get(key);
                    if(cached != null && cached.getClass().equals(Entity.class)){
                        unmarshaller().unmarshall(instance, (Entity) cached);
                        return (T) instance;
                    }
                }
                unmarshaller().unmarshall(instance, e);
            }
        } catch (EntityNotFoundException e1) {
        	if (safe) {
        		throw new ObjectNotFoundException(e1.getMessage(), e1);
        	}
        }
        return instance;
    }
    
    @Override
	public <T> T safeGet(Class<T> clazz, Key key) {
		return get(clazz, key, true);
	}

	@Override
	public <T> T safeGet(Class<T> clazz, String key) {
		String kind = getKind(clazz);
        return get(clazz, KeyStructure.createKey(kind, key), true);
	}

	@Override
	public <T> T safeGet(Class<T> clazz, Long id) {
		String kind = getKind(clazz);
        return get(clazz, KeyStructure.createKey(kind, id), true);
	}

	@Override
	public <T> T safeGet(Class<T> clazz, String kind, String key) {
		T result = null;
        try {
            Entity e = _ds.get(KeyStructure.createKey(kind, key));
            if(clazz.equals(Map.class)){
                result = (T) new LinkedHashMap<>();
            } else {
                result = createInstance(clazz);
            }
            unmarshaller().unmarshall(result, e);
            return result;
        } catch (EntityNotFoundException e1) {
        	throw new ObjectNotFoundException(e1.getMessage(), e1);
        }
	}

	@Override
	public <T> T safeGet(Class<T> clazz, String kind, Long id) {
		T result = null;
        try {
            Entity e = _ds.get(KeyStructure.createKey(kind, id));
            if(clazz.equals(Map.class)){
                result = (T) new LinkedHashMap<>();
            } else {
                result = createInstance(clazz);
            }
            unmarshaller().unmarshall(result, e);
            return result;
        } catch (EntityNotFoundException e1) {
        	throw new ObjectNotFoundException(e1.getMessage(), e1);
        }
	}

    @Override
    public <T> T get(Class<T> clazz, String key) {
        String kind = getKind(clazz);
        return get(clazz, KeyStructure.createKey(kind, key));
    }

    @Override
    public <T> T get(Class<T> clazz, Long id) {
        String kind = getKind(clazz);
        return get(clazz, KeyStructure.createKey(kind, id));
    }

    @Override
    public <T> T get(Class<T> clazz, String kind, String key) {
        T result = null;
        try {
            Entity e = _ds.get(KeyStructure.createKey(kind, key));
            if(clazz.equals(Map.class)){
                result = (T) new LinkedHashMap<>();
            } else {
                result = createInstance(clazz);
            }
            unmarshaller().unmarshall(result, e);
        } catch (EntityNotFoundException e1) {
        }
        return result;
    }

    @Override
    public <T> T get(Class<T> clazz, String kind, Long id) {
        T result = null;
        try {
            Entity e = _ds.get(KeyStructure.createKey(kind, id));
            if(clazz.equals(Map.class)){
                result = (T) new LinkedHashMap<>();
            } else {
                result = createInstance(clazz);
            }
            unmarshaller().unmarshall(result, e);
        } catch (EntityNotFoundException e1) {
        }
        return result;
    }

    @Override
    public Iterable<Object> get(Iterable<Key> keys) {
        List<Object> result = null;
        try {
            Map<Key,Entity> entities = _ds.get(keys);
        } catch (Exception e) {
        }
        return result;
    }

    @Override
    public Object getInTransaction(Key key) {
        Transaction tx = _ds.beginTransaction();
        Object result = null;
        try {
            Entity e = _ds.get(key);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
        }
        return result;
    }

    @Override
    public Iterable<Object> getInTransaction(Iterable<Key> keys) {
        Transaction tx = _ds.beginTransaction();
        List<Object> result = null;
        try {
            Map<Key,Entity> entities = _ds.get(keys);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
        }
        return result;
    }

    @Override
    public Key put(Object object) {
        Key key = null;
        Iterable<Entity> entities = marshall(object);
        List<Key> keys = _ds.put(entities);
        assert list(entities).size() == keys.size();
        key = getLast(keys);
        if(isCached(object)){
            _cache.put(key, getLast(list(entities)));
        }
        updateObjectKey(key, object);
        return key;
    }

    @Override
    public Key put(Transaction tx, Object object) {
        Key key = null;
        Iterable<Entity> entities = marshall(object);
        List<Key> keys = _ds.put(tx, entities);
        assert list(entities).size() == keys.size();
        key = getLast(keys);
        if(isCached(object)){
            _cache.put(key, getLast(list(entities)));
        }
        updateObjectKey(key, object);
        return key;
    }

    @Override
    public Iterable<Key> put(Object... objects){
        return put(list(objects));
    }

    @Override
    public Iterable<Key> put(Iterable<Object> objects) {
        List<Key> keys = new LinkedList<>();
        for (Object o : objects){
            Key key = put(o);
            updateObjectKey(key, o);
            keys.add(key);
        }
        return keys;
    }

    @Override
    public Key putInTransaction(Object object) {
        Transaction tx = _ds.beginTransaction(_options);
        Key result = null;
        try {
            result = put(object);
            tx.commit();
        } catch (Exception e) {
            // TODO Wrap exception and add a getLastError() method
            e.printStackTrace();
            tx.rollback();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
        }
        return result;
    }

    @Override
    public Iterable<Key> putInTransaction(Object... objects) {
        List<Key> keys = new LinkedList<>();
        Transaction tx = _ds.beginTransaction(_options);
        try {
            for (Object o : objects){
                Key key = put(o);
                updateObjectKey(key, o);
                keys.add(key);
            }
            tx.commit();
        } catch (Exception e){
            e.printStackTrace();
            tx.rollback();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
        }

        return keys;
    }

    @Override
    public Iterable<Key> putInTransaction(Iterable<Object> objects) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public Marshaller marshaller() {
        return new GaeMarshaller();
    }

    @Override
    public Unmarshaller unmarshaller() {
        return new GaeUnmarshaller(this);
    }

    @Override
    public Transaction getTransaction() {
        return _ds.getCurrentTransaction();
    }

    private Iterable<Entity> marshall(Object instance){
        List<Entity> entities = new LinkedList<Entity>();
        Key parent = getParentKey(instance);
        IdentityHashMap<Object, Entity> stack
                = marshaller().marshall(parent, instance);
        Entity root = stack.get(instance);
        assert root != null;
        stack.remove(instance);
        final Iterator it = stack.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<Object,Entity> entry
                    = (Map.Entry<Object, Entity>) it.next();
            Entity e = entry.getValue();
            entities.add(e);
        }
        entities.add(root);
        return entities;
    }

    /**
     * Updates @Id field of object from key
     *
     * @param key
     * @param object
     */
    private void updateObjectKey(Key key, Object object){
        AnnotationUtil.AnnotatedField field = AnnotationUtil.getFieldWithAnnotation(key(), object);
        AnnotationUtil.AnnotatedField objectIdField = AnnotationUtil.getFieldWithAnnotation(objectId(), object);
        if(field != null && key !=null){
            if(field.getFieldType().equals(String.class)){
                field.setFieldValue(key.getName());
            } else if(field.getFieldType().equals(Long.class)){
                field.setFieldValue(key.getId());
            } else if(field.getFieldType().equals(Integer.class)){
                throw new RuntimeException("Not yet supported");
            }
        } else if(objectIdField != null && key != null) {
            if(objectIdField.getFieldType().equals(String.class)){
                String objectId = KeyFactory.keyToString(key);
                objectIdField.setFieldValue(objectId);
            } else {
                throw new RuntimeException("Only String type is supported as ObjectId");
            }
        }
    }

    /**
     * Register the class into DS kind
     * TODO: Register or just call this check for each operation?
     * @param clazz type to register
     */
    public void register(Class<?> clazz){
        List<Annotation> annotations = list(clazz.getAnnotations());
        if(annotations.isEmpty()){
            String kind = StringHelper.getClassNameFrom(clazz.getName());
            cls.put(clazz, kind);
        } else {
            for (Annotation annotation : annotations) {
                com.dotweblabs.twirl.annotations.Entity entityAnnotation = null;
                if(annotation instanceof com.dotweblabs.twirl.annotations.Entity){
                    entityAnnotation = (com.dotweblabs.twirl.annotations.Entity)annotation;
                }
                if(entityAnnotation != null){
                    String entityName = entityAnnotation.name();
                    if (cls.get(clazz) == null){
                        if (entityName == null || entityName.isEmpty()){
                            cls.put(clazz, StringHelper.getClassNameFrom(clazz.getName()));
                        } else {
                            cls.put(clazz, entityName);
                        }
                    }
                }
            }
            // Fallback
            if (cls.get(clazz) == null){
                String kind = StringHelper.getClassNameFrom(clazz.getName());
                if (kind == null || kind.isEmpty()){
                    cls.put(clazz, StringHelper.getClassNameFrom(clazz.getName()));
                } else {
                    cls.put(clazz, kind);
                }
            }
        }
    }

    public DatastoreService getDatastoreService(){
        return _ds;
    }

    public String getKind(Class<?> clazz){
        String kind =  cls.get(clazz);
        if (kind == null){
            LOG.info(clazz.getName() + " is not registered. Registering now.");
            register(clazz);
            return cls.get(clazz);
        } else {
            return cls.get(clazz);
        }
    }

    // TODO: Warning this is not tested!
    public Key getParent(Object instance){
        Deque<Object> ancestors = new LinkedBlockingDeque<>();
        Key parentKey = null;
        Object parentObject = AnnotationUtil.getFieldWithAnnotation(GaeObjectStore.parent(), instance);
        if(parentObject != null){
            ancestors.add(parentObject);
        }
        while(!ancestors.isEmpty()){
            Object last = ancestors.getLast();
            AnnotationUtil.AnnotatedField idField
                    = AnnotationUtil
                    .getFieldWithAnnotation(GaeObjectStore.key(), last);
            if(idField != null){
                Object parentId = idField.getFieldValue();
                if(parentId.getClass().equals(String.class)){
                    parentKey = KeyStructure.createKey(parentKey, parentObject.getClass(), (String)parentId);
                } else if(parentId.getClass().equals(Long.class) || parentId.getClass().equals(long.class)){
                    parentKey = KeyStructure.createKey(parentKey, parentObject.getClass(), (Long)parentId);
                }
            }
        }
        return parentKey;
    }

    public <T> T createInstance(Class<T> clazz) {
        try {
            // TODO: This could be very dangerous
            if(clazz.equals(String.class)){
                return (T) new String();
            } else  if(clazz.equals(Long.class) || clazz.equals(long.class)){
                return (T) new Long(0);
            } else  if(clazz.equals(Float.class) || clazz.equals(float.class)){
                return (T) new Float(0);
            } else  if(clazz.equals(Integer.class) || clazz.equals(int.class)){
                return (T) new Integer(0);
            } else  if(clazz.equals(Double.class) || clazz.equals(double.class)){
                return (T) new Double(0);
            } else  if(clazz.equals(Boolean.class) || clazz.equals(boolean.class)){
                return (T) new Boolean(false);
            } else if(clazz.equals(Map.class)){
                return (T) new LinkedHashMap<>();
            } else if(clazz.equals(List.class)){
                return (T) new LinkedList<>();
            }
            try {
                Constructor<?> constructor  = clazz.getDeclaredConstructor(new Class[0]);
                boolean isAccessible = constructor.isAccessible();
                constructor.setAccessible(true);
                T instance = (T) constructor.newInstance();
                constructor.setAccessible(isAccessible);
                return instance;
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } catch (IllegalAccessException e){
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns parent key or null
     *
     * @param instance
     * @return
     */
    private static Key getParentKey(Object instance){
        Key parent = null;
        AnnotationUtil.AnnotatedField parentKeyField
                = AnnotationUtil.getFieldWithAnnotation(ancestor(), instance);
        if(parentKeyField != null){
            parent = (Key) parentKeyField.getFieldValue();
        }
        return parent;
    }

    public boolean isPrimitive(Class<?> clazz){
        // TODO: This could be very dangerous
        if(clazz.equals(String.class)){
            return true;
        } else  if(clazz.equals(Long.class) || clazz.equals(long.class)){
            return true;
        } else  if(clazz.equals(Float.class) || clazz.equals(float.class)){
            return true;
        } else  if(clazz.equals(Integer.class) || clazz.equals(int.class)){
            return true;
        } else  if(clazz.equals(Double.class) || clazz.equals(double.class)){
            return true;
        } else  if(clazz.equals(Boolean.class) || clazz.equals(boolean.class)){
            return true;
        }
        return false;
    }

    public boolean isCached(Object object){
        boolean isCached = AnnotationUtil.isClassAnnotated(com.dotweblabs.twirl.annotations.Cached.class, object);
        return isCached;
    }

    private <T> T getLast(T...objs){
        T last = list(objs).get(list(objs).size() - 1);
        return last;
    }

    private <T> T getLast(List<T> objs){
        T last = list(objs).get(list(objs).size() - 1);
        return last;
    }

}
