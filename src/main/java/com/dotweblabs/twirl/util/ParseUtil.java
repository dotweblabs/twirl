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

package com.dotweblabs.twirl.util;

import com.google.appengine.api.datastore.Entity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ParseUtil<T> {

    public T getObjectFromEntity(Entity entity, T pojo) {
        if (entity.getClass().getSimpleName().equals(pojo.getClass().getSimpleName())) {
            for (Field fieldPojo : pojo.getClass().getDeclaredFields()) {
                try {
                    fieldPojo.setAccessible(true);

                    fieldPojo.set(fieldPojo.getType(), entity.getProperty(fieldPojo.getName()));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return pojo;
        }
        return null;
    }

    public List<T> getListObjectFromListEntity(List<Entity> listEntity, T pojo) {
        List<T> listPojo = new ArrayList<T>();

        for (Entity entity : listEntity) {

            if (entity.getKind().equals(pojo.getClass().getSimpleName())) {
                for (Field fieldPojo : pojo.getClass().getDeclaredFields()) {
                    try {
                        fieldPojo.setAccessible(true);
                        fieldPojo.set(pojo, entity.getProperty(fieldPojo.getName()));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                listPojo.add(pojo);
            }
        }

        return listPojo;
    }


}
