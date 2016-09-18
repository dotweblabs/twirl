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
package com.dotweblabs.twirl.object;

import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.KeyRange;
import com.google.appengine.repackaged.com.google.api.client.util.Lists;

import java.util.List;

/**
 *
 * Helper class to create Datastore {@code Key}
 * @author Kerby Martino (kerbymart@gmail.com)
 *
 */
public class KeyStructure {

    public static Key createKey(Key parent, Class<?> clazz, String key){
        return KeyFactory.createKey(parent, clazz.getSimpleName(), key);
    }

    public static Key createKey(Class<?> clazz, String key){
        return KeyFactory.createKey(clazz.getSimpleName(), key);
    }

    public static Key createKey(Key parent, Class<?> clazz, Long id){
        return KeyFactory.createKey(parent, clazz.getSimpleName(), id);
    }

    public static Key createKey(Class<?> clazz, Long id){
        return KeyFactory.createKey(clazz.getSimpleName(), id);
    }

    public static Key createKey(Key parent, Class<?> clazz, long id){
        return KeyFactory.createKey(parent, clazz.getSimpleName(), id);
    }

    public static Key createKey(Class<?> clazz, long id){
        return KeyFactory.createKey(clazz.getSimpleName(), id);
    }

	public static Key createKey(String kind, String key) {
        try {
            Key decodedKey = KeyFactory.stringToKey(key);
            return decodedKey;
        } catch (Exception e){
        }
        return KeyFactory.createKey(kind, key);
	}

	public static Key createKey(Key parent, String kind, String key) {
        if (parent == null){
            return KeyFactory.createKey(kind, key);
        }
		return KeyFactory.createKey(parent, kind, key);
    }

    public static Key createKey(String kind, Long id) {
        return KeyFactory.createKey(kind, id);
    }

    public static Key createKey(String kind, long id) {
        return KeyFactory.createKey(kind, id);
    }

    public static Key createKey(Key parent, String kind, Long id) {
        if (parent == null){
            return KeyFactory.createKey(kind, id);
        }
        return KeyFactory.createKey(parent, kind, id);
    }

    public static Key createKey(Key parent, String kind, long id) {
        if (parent == null){
            return KeyFactory.createKey(kind, id);
        }
        Long _id = new Long(id);
        if (_id == null){
            return null;
        }
        return KeyFactory.createKey(parent, kind, id);
    }

    // Helper method
    public static List<Key> autIds(String kind, int count) {
        KeyRange range = DatastoreServiceFactory.getDatastoreService().allocateIds(kind, count);
        return Lists.newArrayList(range);
    }

    public static Key autoId(String kind){
        KeyRange range = DatastoreServiceFactory.getDatastoreService().allocateIds(kind, 1);
        return range.iterator().next();
    }

    public static long autoLongId(String kind){
       return DatastoreServiceFactory.getDatastoreService().allocateIds(kind, 1).getStart().getId();
    }



}
