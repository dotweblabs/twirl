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
 *  __            __       __
 * |  |_.--.--.--|__.-----|  |_
 * |   _|  |  |  |  |__ --|   _|
 * |____|________|__|_____|____|
 * :: Twist :: Object Mapping ::
 *
 */
package com.textquo.twist;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.textquo.twist.annotations.Cached;
import com.textquo.twist.annotations.Child;
import com.textquo.twist.annotations.Id;
import com.textquo.twist.common.CacheInconsistencyException;
import com.textquo.twist.common.ObjectNotFoundException;
import com.textquo.twist.gae.GaeMarshaller;
import com.textquo.twist.gae.GaeUnmarshaller;
import com.textquo.twist.object.KeyStructure;
import com.textquo.twist.serializer.ObjectSerializer;
import com.textquo.twist.types.Find;
import com.textquo.twist.types.FindOne;
import com.textquo.twist.types.Update;
import com.textquo.twist.util.AnnotationUtil;
import com.textquo.twist.util.StringHelper;
import com.textquo.twist.annotations.Parent;
import com.textquo.twist.wrappers.PrimitiveWrapper;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.lang.annotation.Annotation;
import java.util.*;

import static org.boon.Lists.list;

public class GaeObjectStore implements ObjectStore {

    public static Class<com.textquo.twist.annotations.Entity> entity(){
        return com.textquo.twist.annotations.Entity.class;
    }

    public static Class<Id> key(){
        return Id.class;
    }

    public static Class<Parent> parent(){
        return Parent.class;
    }

    public static Class<Child> child(){
        return Child.class;
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
     * GAE com.textquo.twist.datastore supported types.
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
        delete(Lists.newArrayList(keys));
    }

    @Override
    public void delete(Iterable<Key> keys) {
        _ds.delete(keys);
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
            _ds.delete(com.google.common.collect.Lists.newArrayList(keys));
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
    public <T> Find find(Class<T> clazz, String kind) {
        return new Find(this, clazz, kind);
    }

    @Override
    public <T> Find find(Class<T> clazz){
        return new Find(this, clazz, getKind(clazz));
    }

    @Override
    public <T> FindOne findOne(Class<T> clazz) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public <T> Update update(Class<T> clazz){
        return new Update(this, clazz, getKind(clazz));
    }

    @Override
    public <T> T get(Class<T> clazz, Key key) {
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
            throw new ObjectNotFoundException("Object with key=" + key.getName() + " not found");
        }
        return instance;
    }

    @Override
    public <T> T get(Class<T> clazz, String key) {
        T result = null;
        try {
            String kind = getKind(clazz);
            Entity e = _ds.get(KeyStructure.createKey(kind, key));
            result = createInstance(clazz);
            if(isPrimitive(clazz)){
                PrimitiveWrapper<T> wrapper = new PrimitiveWrapper<T>(result);
                unmarshaller().unmarshall(wrapper, e);
                result = wrapper.getValue();
            } else {
                unmarshaller().unmarshall(result, e);
            }
        } catch (EntityNotFoundException e1) {
            throw new ObjectNotFoundException("Object with key=" + key + " not found");
        }
        return result;
    }

    @Override
    public <T> T get(Class<T> clazz, Long id) {
        T result = null;
        try {
            String kind = getKind(clazz);
            Entity e = _ds.get(KeyStructure.createKey(kind, id));
            result = createInstance(clazz);
            if(isPrimitive(clazz)){
                PrimitiveWrapper<T> wrapper = new PrimitiveWrapper<T>(result);
                unmarshaller().unmarshall(wrapper, e);
                result = wrapper.getValue();
            } else {
                unmarshaller().unmarshall(result, e);
            }
        } catch (EntityNotFoundException e1) {
            throw new ObjectNotFoundException("Object with id=" + id + " not found");
        }
        return result;
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
            throw new ObjectNotFoundException("Object with key=" + key + " not found");
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
            throw new ObjectNotFoundException("Object with id=" + id + " not found");
        }
        return result;
    }

    @Override
    public Iterable<Object> get(Iterable<Key> keys) {
        List<Object> result = null;
        try {
            Map<Key,Entity> entities = _ds.get(keys);
        } catch (Exception e) {
            throw new ObjectNotFoundException("Object with keys=" + keys + " not found");
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
        key = Iterables.getLast(keys);
        if(isCached(object)){
            _cache.put(key, Iterables.getLast(entities));
        }
        updateObjectKey(key, object);
        return key;
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
            Iterable<Entity> entities = marshall(object);
            List<Key> keys = _ds.put(entities);
            assert list(entities).size() == keys.size();
            result = Iterables.getLast(keys);
        } catch (Exception e) {
            e.printStackTrace();
            tx.rollback();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
        }
        updateObjectKey(result, object);
        return result;
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

    private Iterable<Entity> marshall(Object instance){
        List<Entity> entities = new LinkedList<Entity>();
        IdentityHashMap<Object, Entity> stack
                = marshaller().marshall(null, instance);
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
        if(field != null && key !=null){
            if(field.getFieldType().equals(String.class)){
                field.setFieldValue(key.getName());
            } else if(field.getFieldType().equals(Long.class)){
                field.setFieldValue(key.getId());
            } else if(field.getFieldType().equals(Integer.class)){
                throw new RuntimeException("Not yet supported");
            }
        }
    }

    /**
     * Register the class into DS kind
     * TODO: Register or just call this check for each operation?
     * @param clazz type to register
     */
    public void register(Class<?> clazz){
        List<Annotation> annotations = Lists.newArrayList(clazz.getAnnotations());
        if(annotations.isEmpty()){
            String kind = StringHelper.getClassNameFrom(clazz.getName());
            cls.put(clazz, kind);
        } else {
            for (Annotation annotation : annotations) {
                com.textquo.twist.annotations.Entity entityAnnotation = null;
                if(annotation instanceof com.textquo.twist.annotations.Entity){
                    entityAnnotation = (com.textquo.twist.annotations.Entity)annotation;
                }
                if(entityAnnotation != null){
                    String entityName = entityAnnotation.name();
                    String entitySpacer = entityAnnotation.spacer();
                    if (cls.get(clazz) == null){
                        if (entityName == null || entityName.isEmpty()){
                            cls.put(clazz, StringHelper.getClassNameFrom(clazz.getName()));
                        } else {
                            cls.put(clazz, entityName);
                        }
                    }
                } else {
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
            }
            return clazz.newInstance();
        } catch (IllegalAccessException e){
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return null;
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
        boolean isCached = AnnotationUtil.isClassAnnotated(Cached.class, object);
        return isCached;
    }

}
