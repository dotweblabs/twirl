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
package org.appobjects.gae;

import org.appobjects.GaeObjectStore;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.users.User;
import com.google.common.base.Preconditions;
import org.appobjects.Unmarshaller;
import org.appobjects.util.AnnotationUtil;
import org.appobjects.util.AnnotationUtil.AnnotatedField;
import org.appobjects.validation.Validator;
import org.apache.commons.lang3.ArrayUtils;


import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by kerby on 4/28/14.
 */
public class GaeUnmarshaller implements Unmarshaller {

    private final GaeObjectStore bender;
    private final Validator validator = new Validator();

    public GaeUnmarshaller(GaeObjectStore bender){
        this.bender = bender;
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

        //Map<String, Object> unmarshalled = new LinkedHashMap<String, Object>();
        Map<String,Object> props = entity.getProperties();

        Key key = entity.getKey();
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
        while(it.hasNext()){
            Map.Entry<String,Object> entry = it.next();
            String fieldName = entry.getKey();
            Object fieldValue = entry.getValue();
            Class<?> fieldValueType = fieldValue.getClass();
            if(fieldValue instanceof Key){ // child
                try{
                    Entity e = bender.getDatastoreService()
                            .get((com.google.appengine.api.datastore.Key)fieldValue);
                    AnnotatedField annotatedField
                            = AnnotationUtil.getFieldWithAnnotation(GaeObjectStore.child(), destination);
                    Object childInstance = bender.createInstance(annotatedField.getFieldType());
                    unmarshall(childInstance, e);
                } catch (EntityNotFoundException e){
                    fieldValue = null;
                }
            } else if(fieldValue instanceof String
                    || fieldValue instanceof Boolean
                    || fieldValue instanceof Number) {
                Class<?> clazz = destination.getClass();
                for (Field field : clazz.getDeclaredFields()){
                    if(field.getName().equals(fieldName)){
                        if(fieldValueType.equals(field.getType())){
                            setFieldValue(field, destination, fieldValue);
                        }
                    }
                }
            } else if (fieldValue instanceof EmbeddedEntity) { // POJO's
                EmbeddedEntity ee = (EmbeddedEntity) fieldValue;
                Map<String,Object> map = ee.getProperties();
                bender.createInstance(fieldValueType);
            } else if (fieldValue.getClass().isPrimitive()){
                // TODO
            }

        }



        for (Field f : destination.getClass().getDeclaredFields()){
            if(f.isAnnotationPresent(GaeObjectStore.key())) {
                // skip
            } else if(f.isAnnotationPresent(GaeObjectStore.parent())){

            } else if(f.isAnnotationPresent(GaeObjectStore.child())){
                Object child = bender.createInstance(f.getType());
                setFieldValue(f, destination, child);
            }
        }
    }

    /**
     * Process <code>EmbeddedEntity</code> and inner <code>EmbeddedEntity</code>
     * of this entity.
     *
     * @param ee
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
     * @param ee
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
