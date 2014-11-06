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
package org.appobjects.gae;

import com.google.common.collect.Lists;
import org.appobjects.GaeObjectStore;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.users.User;
import com.google.common.base.Preconditions;
import org.appobjects.Unmarshaller;
import org.appobjects.util.AnnotationUtil;
import org.appobjects.util.AnnotationUtil.AnnotatedField;
import org.appobjects.validation.Validator;
import org.apache.commons.lang3.ArrayUtils;
import org.boon.Maps;


import java.lang.reflect.Field;
import java.util.*;

import static org.boon.Lists.list;

/**
 * Created by kerby on 4/28/14.
 */
public class GaeUnmarshaller implements Unmarshaller {

    private final GaeObjectStore store;
    private final Validator validator = new Validator();

    public GaeUnmarshaller(GaeObjectStore store){
        this.store = store;
    }

    @Override
    public Object unmarshall(Entity entity) {
        Map<String, Object> unmarshalled = new LinkedHashMap<String, Object>();
        unmarshall(unmarshalled, entity);
        return unmarshalled;
    }

    @Override
    public void unmarshall(Object destination, Entity entity) {
        doUnmarshall(destination, null, entity);
    }

    @Override
    public Object unmarshall(Transaction transaction, Entity entity) {
        return null;
    }

    @Override
    public void unmarshall(Object destination, Transaction transaction, Entity entity) {

    }

    private void setFieldValue(Field field, Object instance, Object value){
        boolean accessible = field.isAccessible();
        Class<?> clazz = field.getType();
        field.setAccessible(true);
        try {
            field.set(instance, value);
            field.setAccessible(accessible);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void doUnmarshall(Object destination, Transaction transaction, Entity entity){
        Preconditions.checkNotNull(destination, "Destination object cannot be null");
        Preconditions.checkNotNull(entity, "Source entity cannot be null");

        assert validator.validate(destination) == true;

        Map<String,Object> props = entity.getProperties();
        Key key = entity.getKey();

        if(destination instanceof Map){
            ((Map)destination).putAll(entity.getProperties());
            ((Map)destination).put(Entity.KEY_RESERVED_PROPERTY, key.getName());
            return;
        }

        AnnotatedField idField
                = AnnotationUtil.getFieldWithAnnotation(GaeObjectStore.key(), destination);
        if(idField.getFieldType().equals(String.class)){
            idField.setFieldValue(key.getName());
        } else if(idField.getFieldType().equals(Long.class)){
            idField.setFieldValue(key.getId());
        } else if(idField.getFieldType().equals(long.class)){
            idField.setFieldValue(key.getId());
        } else {
            throw new RuntimeException("Invalid key was retrieved with type " + idField.getFieldType());
        }

        Iterator<Map.Entry<String,Object>> it = props.entrySet().iterator();
        Class<?> clazz = destination.getClass();
        List<Field> fields = Lists.newArrayList(clazz.getDeclaredFields());

        while(it.hasNext()){
            Map.Entry<String,Object> entry = it.next();
            String fieldName = entry.getKey();
            Object fieldValue = entry.getValue();

            for (Field field : fields){
                if(field.getName().equals(fieldName)){
                    if(fieldValue == null){
                        setFieldValue(field, destination, fieldValue);
                    } else if(fieldValue instanceof Key){ // child
                        try{
                            Entity e = store.getDatastoreService()
                                    .get((com.google.appengine.api.datastore.Key)fieldValue);
                            AnnotatedField annotatedField
                                    = AnnotationUtil.getFieldWithAnnotation(GaeObjectStore.child(), destination);
                            Object childInstance = store.createInstance(annotatedField.getFieldType());
                            unmarshall(childInstance, e);
                            setFieldValue(field, destination, childInstance);
                        } catch (EntityNotFoundException e){
                            fieldValue = null;
                        }
                    } else if(fieldValue instanceof String
                            || fieldValue instanceof Boolean
                            || fieldValue instanceof Number) {
                        if(field.getName().equals(fieldName)){
                            Class<?> fieldType = field.getType();
                            if (field.getType().equals(String.class)){
                                setFieldValue(field, destination, String.valueOf(fieldValue));
                            } else if (field.getType().equals(Boolean.class)){
                                setFieldValue(field, destination, (Boolean)fieldValue);
                            } else if (field.getType().equals(Long.class)){
                                setFieldValue(field, destination, (Long) fieldValue);
                            } else if (field.getType().equals(Integer.class)){
                                if(fieldValue.getClass().equals(Long.class)){
                                    Long value = (Long) fieldValue;
                                    setFieldValue(field, destination, value.intValue());
                                } else {
                                    setFieldValue(field, destination, (Integer)fieldValue);
                                }
                            } else if (field.getType().equals(int.class)){
                                if(fieldValue.getClass().equals(Long.class) ||
                                        fieldValue.getClass().equals(long.class)){
                                    Long value = (Long) fieldValue;
                                    setFieldValue(field, destination, value.intValue());
                                } else {
                                    setFieldValue(field, destination, ((Integer)fieldValue).intValue());
                                }
                            } else if (field.getType().equals(long.class)){
                                if(fieldValue.getClass().equals(Integer.class) ||
                                        fieldValue.getClass().equals(int.class)){
                                    Integer value = (Integer) fieldValue;
                                    setFieldValue(field, destination, value.longValue());
                                } else {
                                    setFieldValue(field, destination, ((Long)fieldValue).longValue());
                                }
                            } else if (field.getType().equals(boolean.class)){
                                setFieldValue(field, destination, ((Boolean)fieldValue).booleanValue());
                            }
                        }
                    } else if (fieldValue instanceof EmbeddedEntity) { // List or Java  primitive types and standard types, Map or  POJO's
                        Class<?> fieldValueType = fieldValue.getClass();
                        EmbeddedEntity ee = (EmbeddedEntity) fieldValue;
                        Map<String,Object> map = ee.getProperties();
                        store.createInstance(fieldValueType);
                        if (field.getType().equals(List.class) || field.getType().equals(Map.class)){
                            Object mapOrList = getMapOrList((EmbeddedEntity) fieldValue);
                            setFieldValue(field, destination, mapOrList);
                        } else { // Must be a POJO
                            Map<String,Object> getMap = getMapFromEmbeddedEntity((EmbeddedEntity) fieldValue);
                            Object pojo = Maps.fromMap(getMap, field.getType());
                            setFieldValue(field, destination, pojo);
                        }
                    } else if (fieldValue.getClass().isPrimitive()){
                        // TODO
                    }
                }
            }
        }
    }

    /**
     * Process <code>EmbeddedEntity</code> and inner <code>EmbeddedEntity</code>
     * of this entity.
     *
     * @param ee to unmarshall into {@code Map}
     * @return
     */
    public  Map<String,Object> getMapFromEmbeddedEntity(final EmbeddedEntity ee){
        Map<String,Object> map = null;
        try {
            map = new LinkedHashMap<String, Object>();
            map.putAll(ee.getProperties());

            Map<String,Object> newMap = new LinkedHashMap<String, Object>();
            Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
            while(it.hasNext()){
                Map.Entry<String, Object> entry = it.next();
                if (entry.getValue() instanceof EmbeddedEntity){
//                    LOG.debug( "Inner embedded entity found with key=" + entry.getKey());
                    newMap.put(entry.getKey(), getMapOrList( (EmbeddedEntity) entry.getValue()));
                    it.remove();
                }
            }
            map.putAll(newMap);
        } catch (Exception e) {
            // TODO Handle exception
            e.printStackTrace();
//            LOG.error("Error when processing EmbeddedEntity to Map");
        }
        return map;
    }

    /**
     * Get the <code>List</code> out of the Embedded entity.
     * The <code>List</code> is expected to be stored following a dot (.) notation.
     * E.g. A JSON array with a key of "numbers" will be stored as a <code>EmbeddedEntity</code>
     * with property names:
     *
     * <code>
     * numbers.0
     * numbers.1
     * numbers.2
     * </code>
     *
     * And so on. And since it is stored a a  <code>EmbeddedEntity</code> then it is ambiguous to a
     * <code>Map</code> that is also stored in the same Datastore type.
     *
     * @param ee entity to unmarshall into {@code List}
     * @return
     */
    public List<Object> getListFromEmbeddedEntity(final EmbeddedEntity ee){
        List<Object> list = null;
        Iterator<Map.Entry<String, Object>> it = ee.getProperties().entrySet().iterator();
        Object[] arr = new Object[1024];
        List<Integer> indexToRemove = new ArrayList<Integer>();
        for (int i=0;i<arr.length;i++){
            indexToRemove.add(i);
        }
        while (it.hasNext()){
            Map.Entry<String, Object> entry = it.next();
            try {
                if (list == null){
                    list = new LinkedList<Object>();
                }
                Object value = entry.getValue();
                Integer i = Integer.valueOf(entry.getKey());
//                LOG.debug("Value="+entry.getValue());
                if (value instanceof String
                        || value instanceof Boolean
                        || value instanceof Number
                        || value instanceof Date
                        || value instanceof User) // GAE supported type
                {
                    arr[i] = value;
                    indexToRemove.remove(i);
                } else if (value instanceof EmbeddedEntity){
                    arr[i] = getMapOrList((EmbeddedEntity)value);
                    indexToRemove.remove(i);
                } else {
                    throw new RuntimeException("Invalid JSON field type in embedded list entity");
                }
            } catch (Exception e) {
                // TODO Handle exception
                e.printStackTrace();
            }
        }
        int[] intArray = ArrayUtils.toPrimitive(indexToRemove.toArray(new Integer[indexToRemove.size()]));
        arr = copyArrayRemove(arr, intArray);
        return Arrays.asList(arr);
    }

    /**
     *
     * Evaluates whether to return a <code>List</code> or a <code>Map</code> from the
     * values in the given <code>EmbeddedEntity</code>
     * <br>
     * <br>
     * Since a JSON List or Map is stored in the same type as a <code>EmbeddedEntity</code>
     * it is needed to analyze the property names of the specified embedded entity to decide whether its
     * a <code>List</code> or a <code>Map</code> instance.
     *
     * An <code>EmbeddedEntity</code> was chosen approach than directly mapping the list into the
     * parent Entity because JSON array can contain arbitrary values and even objects too.
     *
     * This method will read all the property names of the entity and if all of its properties have
     * a dot-number prefix then it will be transformed into a List, otherwise a Map
     *
     * @param ee
     * @return
     */
    public Object getMapOrList(final EmbeddedEntity ee){
        boolean isList = true;
        Iterator<String> it = ee.getProperties().keySet().iterator();
        while (it.hasNext()){
            String propName = it.next();
            if (!propName.matches("[0-9]{1,9}")){
                isList = false;
            }
        }
        if (isList){
            return getListFromEmbeddedEntity(ee);
        } else {
            return getMapFromEmbeddedEntity(ee);
        }
    }

    private static Object[] copyArrayRemove(Object[] objects, int[] elemToRemove){
        //LOG.debug("Removing elements from array="+elemToRemove);
        Object[] nobjs = Arrays.copyOf(objects, objects.length - elemToRemove.length);
        for (int i = 0, j = 0, k = 0; i < objects.length; i ++) {
            if (j < elemToRemove.length && i == elemToRemove[j]) {
                j ++;
            } else {
                nobjs[k ++] = objects[i];
            }
        }
        return nobjs;
    }

}
