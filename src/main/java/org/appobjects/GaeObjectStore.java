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
 *                    _______ __    __            __
 * .---.-.-----.-----|   _   |  |--|__.-----.----|  |_.-----.
 * |  _  |  _  |  _  |.  |   |  _  |  |  -__|  __|   _|__ --|
 * |___._|   __|   __|.  |   |_____|  |_____|____|____|_____|
 *       |__|  |__|  |:  1   |    |___|
 *                   |::.. . |
 *                   `-------'
 */
package org.appobjects;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Entity;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.appobjects.annotations.Child;
import org.appobjects.annotations.Id;
import org.appobjects.gae.GaeMarshaller;
import org.appobjects.gae.GaeUnmarshaller;
import org.appobjects.object.KeyStructure;
import org.appobjects.object.QueryStore;
import org.appobjects.serializer.ObjectSerializer;
import org.appobjects.types.Find;
import org.appobjects.types.FindOne;
import org.appobjects.types.Update;
import org.appobjects.util.AnnotationUtil;
import org.appobjects.util.AnnotationUtil.AnnotatedField;
import org.appobjects.util.StringHelper;
import org.appobjects.annotations.Parent;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.lang.annotation.Annotation;
import java.util.*;

import static org.boon.Lists.list;

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

    private IdentityHashMap<Class<?>,String> cls = new IdentityHashMap<Class<?>,String>();

    protected static Logger LOG = LogManager.getLogger(GaeObjectStore.class.getName());
    public static String KEY_RESERVED_PROPERTY = Entity.KEY_RESERVED_PROPERTY;

    protected DatastoreService _ds;
    protected static TransactionOptions _options;
    protected ObjectSerializer _serializer;

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
        T result = null;
        try {
            String kind = getKind(clazz);
            _ds.delete(KeyStructure.createKey(kind, key));
        } catch (Exception e1) {
            // TODO: Wrap the exception
            // e.g store.getLastError();
            e1.printStackTrace();
        }
    }

    @Override
    public <T> void delete(Class<T> clazz, Long id) {
        T result = null;
        try {
            String kind = getKind(clazz);
            assert kind != null;
            _ds.delete(KeyStructure.createKey(kind, id));
        } catch (Exception e1) {
            // TODO: Wrap the exception
            // e.g store.getLastError();
            e1.printStackTrace();
        }
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
            unmarshaller().unmarshall(instance, e);
        } catch (EntityNotFoundException e1) {
            e1.printStackTrace();
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
            unmarshaller().unmarshall(result, e);
        } catch (EntityNotFoundException e1) {
            // TODO: Wrap the exception
            e1.printStackTrace();
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
            unmarshaller().unmarshall(result, e);
        } catch (EntityNotFoundException e1) {
            // TODO: Wrap the exception
            e1.printStackTrace();
        }
        return result;    }

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
        Key result = null;
        try {
            Iterable<Entity> entities = marshall(object);
            List<Key> keys = _ds.put(entities);
            assert list(entities).size() == keys.size();
            result = Iterables.getLast(keys);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return result;
    }

    @Override
    public Iterable<Key> put(Iterable<Object> objects) {
        List<Key> keys = new LinkedList<>();
        for (Object o : objects){
           keys.add(put(o));
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
        return result;    }

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
            return clazz.newInstance();
        } catch (IllegalAccessException e){
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

}
