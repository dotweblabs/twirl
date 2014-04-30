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

import org.appobjects.common.AppobjectsException;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.users.User;
import com.google.common.base.Preconditions;
import org.appobjects.GaeObjectStore;
import org.appobjects.Marshaller;
import org.appobjects.annotations.*;
import org.appobjects.common.AutoGenerateStringIdException;
import org.appobjects.object.KeyStructure;
import org.appobjects.util.AnnotationUtil;
import org.appobjects.util.StringHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.boon.Lists.*;


/**
 * Mapping Objects into Entities and vice versa
 */
public class GaeMarshaller implements Marshaller {

    private IdentityHashMap<Object,Entity> stack = new IdentityHashMap<Object, Entity>();

    /**
     * GAE datastore supported types.
     */
    protected static final Set<Class<?>> GAE_SUPPORTED_TYPES =
            DataTypeUtils.getSupportedTypes();
    protected static Logger LOG = LogManager.getLogger(GaeMarshaller.class.getName());

    public GaeMarshaller(){}


    /**
     *
     * Create Entity objects that can be persisted into the GAE datastore,
     * including its Parent-Child relationships (if necessary).
     *
     * @param parent parent of the generated Entity or Entities
     * @param instance
     * @return
     */
    @Override
    public IdentityHashMap marshall(Key parent, Object instance){
        Preconditions.checkNotNull(instance, "Object should not be null");
        stack.clear();
        Key key = createKeyFrom(parent, instance); // inspect kind and create key
        Map<String,Object> props = new LinkedHashMap<String, Object>();
        List<Entity> target = null;
        Entity e = new Entity(key);
        Field[] fields = instance.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (target == null){
                target = new LinkedList<Entity>();
            }
            String fieldName = field.getName();
            if(field.isAnnotationPresent(Id.class)){
                // skip
                continue;
            } else if(field.isAnnotationPresent(Volatile.class)){
                // skip
                continue;
            }
            try {
                boolean isAccessible = field.isAccessible();
                field.setAccessible(true);
                Class<?> fieldType = field.getType();
                Object fieldValue = field.get(instance);
                if (fieldValue == null){
                    e.setProperty(fieldName, null);
                } else if (fieldValue instanceof String) {
                    setProperty(e, fieldName, fieldValue);
                } else if(fieldValue instanceof Number
                        || fieldValue instanceof Long
                        || fieldValue instanceof Integer
                        || fieldValue instanceof Short) {
                    setProperty(e, fieldName, fieldValue);
                } else if(fieldValue instanceof Boolean) {
                    setProperty(e, fieldName, fieldValue);
                } else if(fieldValue instanceof Date) {
                    setProperty(e, fieldName, fieldValue);
                } else if(fieldValue instanceof User) { // GAE support this type
                    setProperty(e, fieldName, fieldValue);
                } else if(fieldValue instanceof List) {
                    LOG.debug( "Processing List valueType");
                    if (field.isAnnotationPresent(Embedded.class)){
                        setProperty(e, fieldName, createEmbeddedEntityFromList((List) fieldValue));
                    } else {
                        throw new AppobjectsException("Map type should be annotated with @Embedded");
                    }
                } else if(fieldValue instanceof Map){
                    LOG.debug( "Processing Map valueType");
                    if (field.isAnnotationPresent(Embedded.class)){
                        setProperty(e, fieldName, createEmbeddedEntityFromMap((Map) fieldValue));
                    } else {
                        throw new AppobjectsException("Map type should be annotated with @Embedded");
                    }
                } else {
                    // For primitives
                    if (fieldType.equals(int.class)){
                        int i = (Integer) fieldValue;
                        setProperty(e, fieldName, i);
                    } else if (fieldType.equals(boolean.class)){
                        boolean i = (Boolean) fieldValue;
                        setProperty(e, fieldName, i);
                    } else if (fieldType.equals(byte.class)){
                        byte i = (Byte) fieldValue;
                        setProperty(e, fieldName, i);
                    } else if (fieldType.equals(short.class)){
                        short i = (Short) fieldValue;
                        setProperty(e, fieldName, i);
                    } else if (fieldType.equals(long.class)){
                        long i = (Long) fieldValue;
                        setProperty(e, fieldName, i);
                    } else if (fieldType.equals(float.class)){
                        float i = (Float) fieldValue;
                        setProperty(e, fieldName, i);
                    } else if (fieldType.equals(double.class)){
                        double i = (Double) fieldValue;
                        setProperty(e, fieldName, i);
                    } else { // POJO
                        if (field.isAnnotationPresent(Embedded.class)){
                            Object pojoField = field.get(instance);
                            Map<String,Object> map = createMapFromPOJO(pojoField);
                            EmbeddedEntity ee = new EmbeddedEntity();
                            // TODO!
                            //setProperty(e, fieldName, ee);
                        } else if (field.isAnnotationPresent(Parent.class)){
                            // @Parent first before @Child, don't switch. @Child needs parent Key.
                            Object parentFieldValue = field.get(instance);
                            marshall(null, parentFieldValue);
                            Entity parentEntity = stack.get(parentFieldValue);
                            Entity _target = new Entity(createKeyFrom(parentEntity.getKey(), instance));
                            _target.setPropertiesFrom(e);
                            e = _target;
                        } else if (field.isAnnotationPresent(Child.class)){
                            Object childFieldValue = field.get(instance);
                            marshall(e.getKey(), childFieldValue);
                            Entity childEntity = stack.get(childFieldValue);
                            Key childEntityKey = childEntity.getKey();
                            setProperty(e, fieldName, childEntityKey);
                        } else {
                            throw new RuntimeException("POJO's must be annotated with @Embedded, @Parent or @Child annotations.");
                        }
                    }
                }
                field.setAccessible(isAccessible);
            } catch(IllegalAccessException ex){
                ex.printStackTrace();
            }
        }
        stack.put(instance, e);
        return stack;
    }

    public static <T> T createObjectFrom(Class<T> clazz, Entity source){
        T obj = null;
        Map<String,Object> props = source.getProperties();
        Iterator<Map.Entry<String,Object>> it = props.entrySet().iterator();
        if (props.size() > 1){
            if (clazz == List.class){
                throw new RuntimeException("Not yet implemented");
            } else if (clazz == Map.class){
                throw new RuntimeException("Not yet implemented");
            } else {
                //obj = createPOJOFrom(clazz, createMapFromEntity(source));
            }
        } else if (props.size() == 1) {
            Map.Entry<String,Object> entry = it.next();
            Object value = entry.getValue();
            if (clazz.isPrimitive()){
                obj = (T) value;
            } else if (clazz == String.class){
                String prop = String.valueOf(value);
                obj = (T) prop;
            } else if (clazz == Number.class || clazz == Long.class || clazz == Integer.class){
                Long prop = Long.valueOf(String.valueOf(value));
                obj = (T) prop;
            } else if (clazz == Boolean.class){
                Boolean prop = Boolean.valueOf(String.valueOf(value));
                obj = (T) prop;
            }
        } else {
            // empty
        }
        return obj;
    }

    /**
     *
     * Create a new Entity from Java primitive types, String, Number, Boolean
     * When obj is a primitive type, or String, Number, Boolean, the Entity that will be created
     * is a single property Entity with key from {@code target} and a single value of obj
     *
     * @param target Entity with Key that will be populated
     * @param obj to get properties from
     * @return
     */
    public static Entity createEntityFromBasicTypes(Entity target, Object obj){
        Preconditions.checkNotNull(obj, "Object should not be null");
        Map<String,Object> props = null;
        try {
            if (obj.getClass().isPrimitive()){
                String key = target.getKey().getName();
                // TODO: Must wrap primitives to object representation
                target.setProperty(key, obj);
            } else if (obj instanceof String || obj instanceof Number || obj instanceof Boolean){
                String key = target.getKey().getName();
                target.setProperty(key, obj);
            } else if (obj instanceof List){
                List<Object> list = (List<Object>) obj;

            } else if (obj instanceof Map){
                Map map = (Map) obj;
            } else {
                throw new RuntimeException("Method can only process basic types");
            }
        } catch (Exception ex){
            target = null;
        }
        return target;
    }

    /**
     * Inspect the surface of object
     *
     * @param instance
     * @return
     */
    private Key inspectObjectAndCreateKey(Object instance){
        AnnotationUtil.AnnotatedField parentField = AnnotationUtil.getFieldWithAnnotation(Parent.class, instance);
        String fieldName = parentField.getFieldName();
        Object fieldValue = parentField.getFieldValue();

        return null;
    }

    private String getKindOf(Object instance){
        String kind;
        org.appobjects.annotations.Entity anno = (org.appobjects.annotations.Entity)
                AnnotationUtil.getClassAnnotation(org.appobjects.annotations.Entity.class, instance);
        if (anno != null){
            // Get the kind
            if (anno.name() != null && !anno.name().isEmpty()){
                kind = anno.name();
            } else {
                kind = StringHelper.getClassNameOf(instance);
            }
        } else {
            kind = StringHelper.getClassNameOf(instance);
        }
        return kind;
    }

    /**
     * Creates a Key from a object instance, the kind is inspected in
     * the process.
     *
     * @param parent
     * @param instance
     * @return
     */
    private Key createKeyFrom(Key parent, Object instance){
        Key key = null;
        Object id = null;
        String kind = getKindOf(instance);
        AnnotationUtil.AnnotatedField idField = AnnotationUtil.getFieldWithAnnotation(Id.class, instance);
        if (idField != null){
            Class<?> clazz = idField.getFieldType();
            id = idField.getFieldValue();
            if (clazz.equals(String.class)){
                if(id !=null){
                    key = KeyStructure.createKey(parent, kind, (String)id);
                } else {
                    throw new AutoGenerateStringIdException();
                }
            } else if (clazz.equals(Long.class)){
                if(id != null){
                    key = KeyStructure.createKey(parent, kind, (Long)id);
                } else { // auto-generate
                    key = KeyStructure.createKey(parent, kind, KeyStructure.autoLongId(kind));
                }
            } else if (clazz.equals(long.class)){
                key = KeyStructure.createKey(parent, kind, (Long)id);
            } else {
                throw new RuntimeException("Unsupported @Id type " + id.getClass() + " Use String or Long type");
            }
        } else {
            throw new  RuntimeException("Object does not have id or key. Must put @Id annotation on " + instance.getClass());
        }
        return key;
    }



    /**
     * Create <code>EmbeddedEntity</code> from List
     *
     * @param entity
     * @return
     */
    //TODO: This method is quite the most problematic part, since there is no list implementation in the datastore, unlike with a <code>Map</code>.
    public  EmbeddedEntity createEmbeddedEntityFromList(List entity){
        EmbeddedEntity ee = null;
        try {
            Preconditions.checkNotNull(entity, "List entity cannot be null");
            int index = 0;
            ee = new EmbeddedEntity();
//            if (parent != null)
//                ee.setKey(parent);
            for (Object o : entity){
                if (o instanceof String
                        || o instanceof Boolean
                        || o instanceof Number
                        || o instanceof Date
                        || o instanceof User){
                    ee.setProperty(String.valueOf(index), o);
                } else if (o instanceof List){
                    ee.setProperty(String.valueOf(index),
                            createEmbeddedEntityFromList((List)o));
                } else if (o instanceof Map){
                    ee.setProperty(String.valueOf(index),
                            createEmbeddedEntityFromMap((Map)o));
                }
                if (o == null){
                    ee.setProperty(String.valueOf(index), null);
                }
                index++;
            }
        } catch (Exception e) {
            // TODO Handle exception
            e.printStackTrace();
        }
        return ee;
    }

    public EmbeddedEntity createEmbeddedEntityFromPOJO(Object instance){
        return createEmbeddedEntityFromMap(createMapFromPOJO(instance));
    }


        /**
         * Creates a <code>EmbeddedEntity</code> from a <code>Map</code>
         * Which may include inner <code>EmbeddedEntity</code>.
         *
         * @param entity
         * @return
         */
    public EmbeddedEntity createEmbeddedEntityFromMap(Map<String,Object> entity){

        Preconditions.checkNotNull(entity, "Map entity cannot be null");

        // I think this is not necessary:
        // Deal with empty map
//        if (entity.size() == 0){
//            EmbeddedEntity ee = new EmbeddedEntity();
//            if (parent != null)
//                ee.setKey(parent);
//            return ee;
//        }

        EmbeddedEntity ee = null;

        Object oid = entity.get(GaeObjectStore.KEY_RESERVED_PROPERTY); // TODO!


        Iterator<Map.Entry<String, Object>> it
                = entity.entrySet().iterator();
        while (it.hasNext()){
//            if (ee == null) {
//                ee = new EmbeddedEntity();
//                if (parent != null)
//                    ee.setKey(parent);
//            }
            Map.Entry<String, Object> entry = it.next();
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value == null){
                ee.setProperty(key, null);
            } else if (value instanceof String) {
                ee.setProperty(key, value);
            } else if(value instanceof Number) {
                ee.setProperty(key, value);
            } else if(value instanceof Boolean) {
                ee.setProperty(key, value);
            } else if(value instanceof Date) {
                ee.setProperty(key, value);
            } else if(value instanceof User) {
                ee.setProperty(key, value);
            } else if(value instanceof List) {
                ee.setProperty(key, createEmbeddedEntityFromList((List)value));
            } else if(value instanceof Map){
                Map<String, Object> map = (Map<String, Object>) value;
                ee.setProperty(key, createEmbeddedEntityFromMap(map));
            }
        }
        LOG.debug("Warning method is returning null valueType");
        return ee;
    }


    private static void validateKey(Object key){
        throw new IllegalArgumentException("Invalid key");
    }



    /** TODOS **/

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
    public static Object getMapOrList(final EmbeddedEntity ee){
        boolean isList = true;
        Iterator<String> it = ee.getProperties().keySet().iterator();
        while (it.hasNext()){
            String propName = it.next();
            if (!propName.matches("[0-9]{1,9}")){
                isList = false;
            }
        }
        if (isList){
            return createListFromEmbeddedEntity(ee);
        } else {
            return createMapFromEmbeddedEntity(ee);
        }
    }

    /**
     * Get the <code>List</code> out of the Embedded entity.
     * The <code>List</code> is expected to be stored following a dot (.) notation.
     * E.g. A JSON array with a keyType of "numbers" will be stored as a <code>EmbeddedEntity</code>
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
    private static List<Object> createListFromEmbeddedEntity(final EmbeddedEntity ee){
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
                LOG.debug("Value="+entry.getValue());
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
     * Process <code>EmbeddedEntity</code> and inner <code>EmbeddedEntity</code>
     * of this entity.
     *
     * @param ee
     * @return
     */
    public static Map<String,Object> createMapFromEmbeddedEntity(final EmbeddedEntity ee){
        Map<String,Object> map = null;
        try {
            map = new HashMap<String, Object>();
            map.putAll(ee.getProperties());

            Map<String,Object> newMap = new HashMap<String, Object>();
            Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
            while(it.hasNext()){
                Map.Entry<String, Object> entry = it.next();
                if (entry.getValue() instanceof EmbeddedEntity){
                    LOG.debug( "Inner embedded entity found with keyType=" + entry.getKey());
//					newMap.put(entry.getKey(), createMapFromEmbeddedEntity( (EmbeddedEntity) entry.getValue()));
                    newMap.put(entry.getKey(), getMapOrList( (EmbeddedEntity) entry.getValue()));
                    it.remove();
                }
            }
            map.putAll(newMap);
        } catch (Exception e) {
            // TODO Handle exception
            e.printStackTrace();
            LOG.error("Error when processing EmbeddedEntity to Map");
        }
        return map;
    }

    /**
     * Used as a method in the chain of making a EmbeddedEntity from a given
     * POJO instance.
     *
     * @param instance
     * @return
     */
    public Map<String,Object> createMapFromPOJO(Object instance){
        Iterator<Field> it = list(instance.getClass().getDeclaredFields()).iterator();
        while(it.hasNext()){
            Field field = it.next();
            // TODO
        }
        return null;
    }

    private static Object[] copyArrayRemove(Object[] objects, int[] elemToRemove){
        LOG.debug("Removing elements from array="+elemToRemove);
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

    /**
     * Helper method to convert a retrieved Date object from the GAE datastore
     * to a string format that Google Gson library can map to a Date field on a POJO
     *
     * @param map
     * @return
     */
    private static Map<String,Object> formatDates(Map<String,Object> map){
        Iterator<Map.Entry<String,Object>> it = map.entrySet().iterator();
        Map<String,Object> dateMap = new HashMap<String,Object>();
        while(it.hasNext()){
            Map.Entry<String,Object> entry = it.next();
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Date){
                Date date = (Date) value;
                String dateString = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz").format(date);
                // Causes, Caused by: java.text.ParseException: Unparseable date: "Mon Jul 15 17:17:32 UTC 2013"
                dateMap.put(key, dateString);
            }
        }
        map.putAll(dateMap);
        return map;
    }

    private static <T> T getAs(Object obj, Class<T> clazz){
        if (obj == null) {
            return null;
        } if (obj.getClass().equals(clazz)){
            return (T) obj;
        }
        T newObj = null;
        try {
            newObj = (T) obj;
        } catch (ClassCastException e) {
            throw new RuntimeException("Invalid object valueType type is being assigned to Object");
        }
        return newObj;
    }

    private static void setProperty(Entity entity, String key, Object value){
        LOG.info("Setting property key: " + key);

        Preconditions.checkNotNull(entity, "Entity can't be null");
        Preconditions.checkNotNull(key, "Entity property key can't be null");

        if (key.isEmpty()){
            throw new IllegalArgumentException("Entity property key can't be empty");
        }


        if (value == null){
            entity.setProperty(key, null);
            return;
        }

        if (!GAE_SUPPORTED_TYPES.contains(value.getClass())
                && !(value instanceof Blob) && !(value instanceof EmbeddedEntity)) {
            throw new RuntimeException("Unsupported type[class=" + value.
                    getClass().getName() + "] in Latke GAE repository");
        }
        if (value instanceof String) {
            final String valueString = (String) value;
            if (valueString.length()
                    > DataTypeUtils.MAX_STRING_PROPERTY_LENGTH) {
                final Text text = new Text(valueString);

                entity.setProperty(key, text);
            } else {
                entity.setProperty(key, value);
            }
        } else if (value instanceof Number
                || value instanceof Date
                || value instanceof Boolean
                || GAE_SUPPORTED_TYPES.contains(value.getClass())) {
            entity.setProperty(key, value);
        } else if (value instanceof EmbeddedEntity) {
            entity.setProperty(key, value);
        } else if (value instanceof Blob) {
            final Blob blob = (Blob) value;
            entity.setProperty(key,
                    new com.google.appengine.api.datastore.Blob(
                            blob.getBytes()));
        }
    }

}
