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
package com.textquo.twist.object;

import com.google.appengine.api.datastore.*;
import com.textquo.twist.Marshaller;
import com.textquo.twist.gae.GaeMarshaller;
import com.textquo.twist.serializer.ObjectSerializer;
import com.textquo.twist.Marshaller;
import com.textquo.twist.gae.GaeMarshaller;
import com.textquo.twist.serializer.ObjectSerializer;
import com.textquo.twist.gae.GaeMarshaller;
import com.textquo.twist.serializer.ObjectSerializer;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Set;

public abstract class AbstractStore {

    protected static Logger LOG = LogManager.getLogger(AbstractStore.class.getName());
    public static String KEY_RESERVED_PROPERTY = Entity.KEY_RESERVED_PROPERTY;

    protected DatastoreService _ds;
    protected ObjectSerializer _serializer;
    protected Marshaller _marshaller;
    protected static TransactionOptions _options;

    /**
     * GAE Datastore supported types.
     */
    protected static final Set<Class<?>> GAE_SUPPORTED_TYPES =
            DataTypeUtils.getSupportedTypes();

    public AbstractStore(DatastoreService ds, ObjectSerializer serializer){
        if (ds == null) {
            _ds = DatastoreServiceFactory.getDatastoreService();
            _options = TransactionOptions.Builder.withXG(true);
        } else {
            _ds = ds;
        }
        _marshaller = new GaeMarshaller();
    }

    protected DatastoreService getDatastoreService(){
        return _ds;
    }
}
