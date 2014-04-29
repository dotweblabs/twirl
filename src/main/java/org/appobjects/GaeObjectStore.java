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
package org.appobjects;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.repackaged.com.google.common.collect.Iterables;
import org.appobjects.annotations.Child;
import org.appobjects.annotations.Id;
import org.appobjects.gae.GaeMarshaller;
import org.appobjects.gae.GaeUnmarshaller;
import org.appobjects.object.KeyStructure;
import org.appobjects.serializer.ObjectSerializer;
import org.appobjects.types.Find;
import org.appobjects.types.FindOne;
import org.appobjects.types.Update;
import org.appobjects.util.StringHelper;
import org.appobjects.annotations.Parent;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * Created by kerby on 4/27/14.
 */
public class GaeObjectStore implements ObjectStore {

    public static Class<org.appobjects.annotations.Entity> entity(){
        return org.appobjects.annotations.Entity.class;
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

    private static IdentityHashMap<Class<?>,String> cls = new IdentityHashMap<Class<?>,String>();

    protected static Logger LOG = LogManager.getLogger(GaeObjectStore.class.getName());
    public static String KEY_RESERVED_PROPERTY = Entity.KEY_RESERVED_PROPERTY;

    protected DatastoreService _ds;
    protected static TransactionOptions _options;
    protected ObjectSerializer _serializer;
    protected Marshaller _marshaller;
    protected Unmarshaller _unmarshaller;

    /**
     * GAE datastore supported types.
     */
    protected static final Set<Class<?>> GAE_SUPPORTED_TYPES =
            DataTypeUtils.getSupportedTypes();

    public GaeObjectStore(){
        if (_ds == null) {
            _ds = DatastoreServiceFactory.getDatastoreService();
            _options = TransactionOptions.Builder.withXG(true);
            LOG.info("Create a new DatastoreService instance");
        }
        _marshaller = new GaeMarshaller();
        _unmarshaller = new GaeUnmarshaller(this);
    }

    @Override
    public void delete(Key key) {
        _ds.delete(key);
    }

    @Override
    public void delete(Key... keys) {
        delete(com.google.common.collect.Lists.newArrayList(keys));
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
    public <T> Find find(Class<T> clazz){
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public <T> FindOne findOne(Class<T> clazz) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public <T> Update update(Class<T> clazz){
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
    public <T> T get(Class<T> clazz, Key key) {
        T instance = null;
        try {
            Entity e = _ds.get(key);
            instance = createInstance(clazz);
            _unmarshaller.unmarshall(instance, e);
        } catch (EntityNotFoundException e1) {
            // TODO: Wrap the exception
        }
        return instance;
    }

    @Override
    public <T> T get(Class<T> clazz, String key) {
        T result = null;
        try {
            String kind = getKind(clazz);
            assert kind != null;
            Entity e = _ds.get(KeyStructure.createKey(kind, key));

        } catch (EntityNotFoundException e1) {
            // TODO: Wrap the exception
        }
        return result;
    }

    @Override
    public Iterable<Object> get(Iterable<Key> keys) {
        List<Object> result = null;
        try {
            Map<Key,Entity> entities = _ds.get(keys);
        } catch (Exception e) {
            // TODO: Wrap the exception
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
        Transaction tx = _ds.beginTransaction();
        Key result = null;
        try {
            List<Key> keys = _ds.put(marshall(object));
            if(!keys.isEmpty()){
                result = Iterables.getLast(keys);
            }
        } catch (Exception e) {
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
    public Iterable<Key> put(Iterable<Object> objects) {
        return null;
    }

    @Override
    public Key putInTransaction(Object object) {
        return null;
    }

    @Override
    public Iterable<Key> putInTransaction(Iterable<Object> objects) {
        return null;
    }


    private Iterable<Entity> marshall(Object instance){
        List<Entity> entities = null;
        IdentityHashMap<Object, Entity> stack
                = _marshaller.marshall(null, instance);
        final Iterator it = stack.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<Object,Entity> entry
                    = (Map.Entry<Object, Entity>) it.next();
            Entity e = entry.getValue();
            if (entities == null){
                entities = new LinkedList<Entity>();
            }
            entities.add(e);
        }
        return entities;
    }

    /**
     * Register the class into DS kind
     *
     * @param clazz
     */
    static void register(Class<?> clazz){
        Annotation[] annotations = clazz.getAnnotations();
        for (Annotation annotation : annotations) {
            org.appobjects.annotations.Entity entityAnnotation = (org.appobjects.annotations.Entity)annotation;
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
            }
        }
    }

    public DatastoreService getDatastoreService(){
        return _ds;
    }

    static String getKind(Class<?> clazz){
        String kind =  cls.get(clazz);
        if (kind == null){
            throw new RuntimeException("Class " + clazz.getName() + " was not registered.");
        }
        return kind;
    }

    public <T> T createInstance(Class<T> clazz) {
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
