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

import com.google.appengine.api.datastore.Key;
import org.appobjects.types.Find;
import org.appobjects.types.FindOne;
import org.appobjects.types.Update;

/**
 * Simple wrapper around the low-level Datastore service to allow
 * storage of any {@code Object} types
 */
public interface ObjectStore {
    public void delete(Key key);
    public void delete(Key... keys);
    public void deleteInTransaction(Key key);
    public void deleteInTransaction(Key... keys);
    public void delete(Iterable<Key> keys);
    public void deleteInTransaction(Iterable<Key> keys);
    public <T> T get(Class<T> clazz, Key key);
    public <T> T get(Class<T> clazz, String key);
    public Iterable<Object> get(Iterable<Key> keys);
    public Object getInTransaction(Key key);
    public Iterable<Object> getInTransaction(Iterable<Key> keys);
    public Key put(Object object);
    public Iterable<Key> put(Iterable<Object> objects);
    public Key putInTransaction(Object object);
    public Iterable<Key> putInTransaction(Iterable<Object> objects);

    public <T> Find find(Class<T> clazz);
    public <T> FindOne findOne(Class<T> clazz);
    public <T> Update update(Class<T> clazz);
    public abstract Marshaller marshaller();
    public abstract Unmarshaller unmarshaller();


}
