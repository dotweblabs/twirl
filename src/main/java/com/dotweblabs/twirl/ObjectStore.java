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

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Transaction;
import com.dotweblabs.twirl.types.Find;
import com.dotweblabs.twirl.types.Function;
import com.dotweblabs.twirl.types.Update;

/**
 * Simple wrapper around the low-level Datastore service to allow
 * storage of any {@code Object} types
 */
public interface ObjectStore {

    public void delete(Key key);
    public void delete(Key... keys);
    public void deleteInTransaction(Key key);
    public void deleteInTransaction(Key... keys);
    public <T> void delete(Iterable<T> keysOrObjects);
    public void deleteInTransaction(Iterable<Key> keys);
    public <T> void delete(Class<T> clazz, String key);
    public <T> void delete(Class<T> clazz, Long id);
    public <T> void delete(Class<T> clazz, String kind, Long id);
    public <T> void delete(Class<T> clazz, String kind, String key);

    public <T> T get(Class<T> clazz, Key key);
    public <T> T get(Class<T> clazz, String key);
    public <T> T get(Class<T> clazz, Long id);
    public <T> T get(Class<T> clazz, String kind, String key);
    public <T> T get(Class<T> clazz, String kind, Long id);

    public <T> T safeGet(Class<T> clazz, Key key);
    public <T> T safeGet(Class<T> clazz, String key);
    public <T> T safeGet(Class<T> clazz, Long id);
    public <T> T safeGet(Class<T> clazz, String kind, String key);
    public <T> T safeGet(Class<T> clazz, String kind, Long id);

    public Iterable<Object> get(Iterable<Key> keys);
    public Object getInTransaction(Key key);
    public Iterable<Object> getInTransaction(Iterable<Key> keys);
    public Key put(Object object);
    public Key put(Transaction tx, Object object);
    public Iterable<Key> put(Object... objects);
    public Iterable<Key> put(Iterable<Object> objects);
    public Key putInTransaction(Object object);
    public Iterable<Key> putInTransaction(Object... object);
    public Iterable<Key> putInTransaction(Iterable<Object> objects);

    public <T> Find<T> find(Class<T> clazz, String kind, Key ancestor);
    public <T> Find<T> find(Class<T> clazz, Key ancestor);
    public <T> Find<T> find(Class<T> clazz, String kind);
    public <T> Find<T> find(Class<T> clazz);
    public <T> T findOne(Class<T> clazz);
    public <T> Update update(Class<T> clazz);

    public <T> T transact(Function<T> function);

    public abstract Marshaller marshaller();
    public abstract Unmarshaller unmarshaller();

    public Transaction getTransaction();
}
