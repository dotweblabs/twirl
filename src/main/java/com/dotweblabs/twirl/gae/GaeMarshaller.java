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
package com.dotweblabs.twirl.gae;

import com.dotweblabs.twirl.annotations.*;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.dotweblabs.twirl.common.AutoGenerateStringIdException;
import com.dotweblabs.twirl.common.TwirlException;
import com.dotweblabs.twirl.util.MapHelper;
import com.dotweblabs.twirl.util.StringHelper;
import com.google.appengine.api.users.User;
import com.dotweblabs.twirl.GaeObjectStore;
import com.dotweblabs.twirl.Marshaller;
import com.dotweblabs.twirl.object.KeyStructure;
import com.dotweblabs.twirl.util.AnnotationUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.boon.Maps;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Mapping Objects into Entities and vice versa
 */
public class GaeMarshaller implements Marshaller {

    private IdentityHashMap<Object,Entity> stack = new IdentityHashMap<Object, Entity>();

    /**
     * GAE Datastore supported types.
     */
    protected static final Set<Class<?>> GAE_SUPPORTED_TYPES =
            DataTypeUtils.getSupportedTypes();
    protected static Logger LOG = LogManager.getLogger(GaeMarshaller.class.getName());

    public GaeMarshaller(){
        stack.clear();
    }

    /**
     *
     * Create entity objects that can be persisted into the GAE Datastore,
     * including its Parent-Child relationships (if necessary).
     *
     * @param parent parent of the generated entity or Entities
     * @param instance to marshall
     * @return
     */
    @Override
    public IdentityHashMap marshall(Key parent, Object instance){
        if(instance == null){
            throw new RuntimeException("Object cannot be null");
        }
        Entity e = null;
        // Its possible that a Entity to be saved without id and just a parent key
        if(parent != null && hasNoIdField(instance)){
            String kind = getKindOf(instance);
            e = new Entity(kind, parent);
        } else {
            Key key = createKeyFrom(parent, instance); // inspect kind and create key
            e = new Entity(key);
        }

        boolean indexed = !AnnotationUtil.isClassAnnotated(GaeObjectStore.unIndexed(), instance);

        Map<String,Object> props = new LinkedHashMap<String, Object>();
        List<Entity> target = null;
        // Marshall java.util.Map
        if(instance instanceof Map){
            Map map = (Map) instance;
            if(String.class.getName().equals(MapHelper.getKeyType(map))){
                Iterator<Map.Entry<String,Object>> it = map.entrySet().iterator();
                while(it.hasNext()){
                    Map.Entry<String,Object> entry = it.next();
                    String entryKey = entry.getKey();
                    Object entryVal = entry.getValue();
                    if(!entryKey.equals(GaeObjectStore.KEY_RESERVED_PROPERTY)
                            && !entryKey.equals(GaeObjectStore.KIND_RESERVED_PROPERTY)
                            && !entryKey.equals(GaeObjectStore.NAMESPACE_RESERVED_PROPERTY)){
                        if(entryVal instanceof Map){
                            setProperty(e, entryKey, createEmbeddedEntityFromMap((Map) entryVal), indexed);
                        } else if(entryVal instanceof List){
                            setProperty(e, entryKey, createEmbeddedEntityFromList((List) entryVal), indexed);
                        } else {
                            setProperty(e, entryKey, entryVal, indexed);
                        }
                    }
                }
            } else {
                throw new RuntimeException(String.class.getName()
                        + " is the only supported " + Map.class.getName() + " key");
            }
        } else {
            // Marshall all other object types
            Field[] fields = instance.getClass().getDeclaredFields();
            GeoPt geoPt = null;
            for (Field field : fields) {
                if (target == null){
                    target = new LinkedList<Entity>();
                }
                if((field.getModifiers() & java.lang.reflect.Modifier.FINAL)
                        == java.lang.reflect.Modifier.FINAL){
                    // do nothing for a final field
                    // usually static UID fields
                    continue;
                }
                String fieldName = field.getName();
                if(field.isAnnotationPresent(GaeObjectStore.key())){
                    // skip
                    continue;
                } else if(field.isAnnotationPresent(GaeObjectStore.objectId())){
                    // skip
                    continue;
                } else if(field.isAnnotationPresent(GaeObjectStore.kind())){
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
                        setProperty(e, fieldName, fieldValue, indexed);
                    } else if(fieldValue instanceof Number
                            || fieldValue instanceof Long
                            || fieldValue instanceof Integer
                            || fieldValue instanceof Short) {
                        setProperty(e, fieldName, fieldValue, indexed);
                    } else if(fieldValue instanceof Boolean) {
                        setProperty(e, fieldName, fieldValue, indexed);
                    } else if(fieldValue instanceof Date) {
                        setProperty(e, fieldName, fieldValue, indexed);
                    } else if(fieldValue instanceof User) { // GAE support this type
                        setProperty(e, fieldName, fieldValue, indexed);
                    } else if(fieldValue instanceof Blob) {
                      setProperty(e, fieldName, fieldValue, indexed);
                    } else if(fieldValue instanceof List) {
                        LOG.debug( "Processing List valueType");
                        if (field.isAnnotationPresent(Embedded.class)){
                            setProperty(e, fieldName, createEmbeddedEntityFromList((List) fieldValue), indexed);
                        } else {
                            boolean supported = true;
                            List list = (List) fieldValue;
                            for (Object item : list){
                                if(!GAE_SUPPORTED_TYPES.contains(item.getClass())){
                                    supported = false;
                                }
                            }
                            if(supported){
                                setProperty(e, fieldName, fieldValue, indexed);
                            } else{
                                throw new RuntimeException("List should only include GAE supported types " + GAE_SUPPORTED_TYPES
                                + " otherwise annotate the List with @Embedded");
                            }
                        }
                    } else if(fieldValue instanceof GeoPt) {
                        setProperty(e, fieldName, fieldValue, indexed);
                    } else if (fieldValue instanceof Enum){
                        String enumValue = fieldValue.toString();
                        LOG.info("Enum marshalled as \"" + enumValue + "\"" );
                        setProperty(e, fieldName, enumValue, indexed);
                    } else if(fieldValue instanceof Map){
                        LOG.debug( "Processing Map valueType");
                        if (field.isAnnotationPresent(Embedded.class)){
                            setProperty(e, fieldName, createEmbeddedEntityFromMap((Map) fieldValue), indexed);
                        } else if (field.isAnnotationPresent(Flat.class)){
                            Map<String,Object> flat = (Map)fieldValue;
                            Iterator<Map.Entry<String, Object>> it = flat.entrySet().iterator();
                            if (!it.hasNext()){
                                LOG.debug("Iterator is empty");
                            }
                            while (it.hasNext()){
                                Object entry = it.next();
                                try {
                                    Map.Entry<Object, Object> mapEntry
                                            = (Map.Entry<Object, Object>) entry;
                                    Object entryKey = mapEntry.getKey();
                                    Object entryVal = mapEntry.getValue();
                                    if(entryVal == null){
                                        setProperty(e, (String) entryKey, null, indexed);
                                    } else if (entryVal instanceof Map){
                                        setProperty(e, (String) entryKey, createEmbeddedEntityFromMap((Map) entryVal), indexed);
                                    } else if (entryVal instanceof List){
                                        throw new RuntimeException("List values are not yet supported");
                                    } else if (entryVal instanceof String
                                            || entryVal instanceof Number
                                            || entryVal instanceof Boolean
                                            || entryVal instanceof Date
                                            || entryVal instanceof User
                                            || entryVal instanceof EmbeddedEntity) {
                                        setProperty(e, (String) entryKey, entryVal, indexed);
                                    } else {
                                        throw new RuntimeException("Unsupported GAE property type: " + entryVal.getClass().getName());
                                    }
                                    if(e == null){
                                        throw new RuntimeException("Entity is null");
                                    }
                                } catch (ClassCastException ex) {
                                    // Something is wrong here
                                    ex.printStackTrace();
                                } catch (Exception ex){
                                    ex.printStackTrace();
                                }
                            }
                        } else {
                            throw new TwirlException("Map type should be annotated with @Embedded or @Flat");
                        }
                    } else {
                        // For primitives
                        if (fieldType.equals(int.class)){
                            int i = (Integer) fieldValue;
                            setProperty(e, fieldName, i, indexed);
                        } else if (fieldType.equals(boolean.class)){
                            boolean i = (Boolean) fieldValue;
                            setProperty(e, fieldName, i, indexed);
                        } else if (fieldType.equals(byte.class)){
                            byte i = (Byte) fieldValue;
                            setProperty(e, fieldName, i, indexed);
                        } else if (fieldType.equals(short.class)){
                            short i = (Short) fieldValue;
                            setProperty(e, fieldName, i, indexed);
                        } else if (fieldType.equals(long.class)){
                            long i = (Long) fieldValue;
                            setProperty(e, fieldName, i, indexed);
                        } else if (fieldType.equals(float.class)){
                            float i = (Float) fieldValue;
                            setProperty(e, fieldName, i, indexed);
                        } else if (fieldType.equals(double.class)){
                            double i = (Double) fieldValue;
                            setProperty(e, fieldName, i, indexed);
                        } else if(fieldType.equals(byte.class)){
                            byte b = (byte) fieldValue;
                            Blob blob = new Blob(new byte[b]);
                            setProperty(e, fieldName, blob, indexed);
                        } else if(fieldType.equals(byte[].class)){
                            byte[] bytes = (byte[]) fieldValue;
                            Blob blob = new Blob(bytes);
                            setProperty(e, fieldName, blob, indexed);
                        } else if(fieldType.equals(Enum.class)) {
                            throw new RuntimeException("Enum primitive type not yet implemented");
                        } else { // POJO
                            if (field.isAnnotationPresent(Embedded.class)){
                                Map<String,Object> map = createMapFrom(fieldValue);
                                EmbeddedEntity ee = createEmbeddedEntityFromMap(map);
                                setProperty(e, fieldName, ee, indexed);
                            } else if (field.isAnnotationPresent(GaeObjectStore.parent())){
                                // @Parent first before @Child, don't switch. @Child needs parent Key.
                                if (field.getType().equals(Key.class)){
                                    // skip it
                                    continue;
                                } else {
                                    if (parent != null){
                                        // Case where a Parent entity is marshalled then during the
                                        // iteration process, a @Child entity is found
                                        Entity _target = new Entity(createKeyFrom(parent, instance));
                                        _target.setPropertiesFrom(e);
                                        e = _target;
                                    } else {
                                        // Case where a Child entity is first marshalled
                                        // at this point this child is not yet in the stack
                                        // and the Parent entity is not yet also in the stack
                                        // so we will create a Key from the "not yet marshalled" parent instance
                                        // or is it not? Let's check.
                                        Object parentField = field.get(instance);
                                        Entity parentEntity = stack.get(parentField);
                                        if(parentEntity!=null){
                                            Entity _target = new Entity(createKeyFrom(parentEntity.getKey(), instance));
                                            _target.setPropertiesFrom(e);
                                            e = _target;
                                        } else{
                                            Key generatedParentKey = createKeyFrom(null, parentField);
                                            Entity _target = new Entity(createKeyFrom(generatedParentKey, instance));
                                            _target.setPropertiesFrom(e);
                                            e = _target;
                                            marshall(null, parentField);
                                        }
                                    }
                                }
                            } else if (field.isAnnotationPresent(GaeObjectStore.child())){
                                Object childField = field.get(instance);
                                marshall(e.getKey(), childField);
                                Entity childEntity = stack.get(childField);
                                Key childEntityKey = childEntity.getKey();
                                setProperty(e, fieldName, childEntityKey, indexed);
                            } else if (field.isAnnotationPresent(GaeObjectStore.ancestor())){
                                // already processed above, skip it
                            } else {
                                throw new RuntimeException("POJO's must be annotated with @Embedded, @Parent or @Child annotations for field "
                                        + fieldName);
                            }
                        }
                    }
                    field.setAccessible(isAccessible);
                } catch(IllegalAccessException ex){
                    ex.printStackTrace();
                }
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
     * Create a new entity from Java primitive types, String, Number, Boolean
     * When obj is a primitive type, or String, Number, Boolean, the entity that will be created
     * is a single property entity with key from {@code target} and a single value of obj
     *
     * @param target entity with Key that will be populated
     * @param obj to get properties from
     * @return marshalled
     */
    public static Entity createEntityFromBasicTypes(Entity target, Object obj){
        if(obj == null){
            throw new RuntimeException("Object should not be null");
        }
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
     * @param instance key
     * @return
     */
    private Key inspectObjectAndCreateKey(Object instance){
        AnnotationUtil.AnnotatedField parentField = AnnotationUtil.getFieldWithAnnotation(GaeObjectStore.parent(), instance);
        String fieldName = parentField.getFieldName();
        Object fieldValue = parentField.getFieldValue();

        return null;
    }

    private static String getKindOf(Object instance){
        String kind;
        if(instance instanceof Map){
            kind = (String) ((Map) instance).get(GaeObjectStore.KIND_RESERVED_PROPERTY);
            if(kind == null || kind.isEmpty()){
                kind = instance.getClass().getSimpleName();
            }
            return kind;
        }
        com.dotweblabs.twirl.annotations.Entity anno
                = (com.dotweblabs.twirl.annotations.Entity) AnnotationUtil.getClassAnnotation(com.dotweblabs.twirl.annotations.Entity.class, instance);
        AnnotationUtil.AnnotatedField kindField = AnnotationUtil.getFieldWithAnnotation(Kind.class, instance);
        if(kindField != null && kindField.getFieldValue() != null){
            kind = (String) kindField.getFieldValue();
        } else if (anno != null){
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



    private static boolean hasNoIdField(Object instance){
        AnnotationUtil.AnnotatedField idField = AnnotationUtil.getFieldWithAnnotation(GaeObjectStore.key(), instance);
        return idField == null ? true : false;
    }

    /**
     * Creates a Key from a object instance, the kind is inspected in
     * the process.
     *
     * @param parent key or null
     * @param instance String, Long/long key object
     * @return GAE {@code Key}
     */
    public static Key createKeyFrom(Key parent, Object instance){
        Key key = null;
        Object id = null;
        String kind = getKindOf(instance);
        if(instance instanceof Map){
            id = ((Map) instance).get(GaeObjectStore.KEY_RESERVED_PROPERTY);
            if(id != null){
                if(id instanceof Long || instance.getClass().equals(long.class)){
                    key = KeyStructure.createKey(parent, kind, (Long)id);
                } else if (id instanceof String){
                    key = KeyStructure.createKey(parent, kind, (String)id);
                } else {
                    throw new RuntimeException("Unsupported " + GaeObjectStore.KEY_RESERVED_PROPERTY + ". Use String or Long type");
                }
            } else {
                // auto-generate "key" when not supplied
                key = KeyStructure.createKey(parent, kind, KeyStructure.autoLongId(kind));
            }
            return key;
        }

        AnnotationUtil.AnnotatedField ancestorField
                = AnnotationUtil
                    .getFieldWithAnnotation(GaeObjectStore.ancestor(), instance);

        AnnotationUtil.AnnotatedField parentField
                = AnnotationUtil.getFieldWithAnnotation(GaeObjectStore.parent(), instance);

        AnnotationUtil.AnnotatedField idField
                = AnnotationUtil
                    .getFieldWithAnnotation(GaeObjectStore.key(), instance);

        AnnotationUtil.AnnotatedField objectIdField
                = AnnotationUtil
                .getFieldWithAnnotation(GaeObjectStore.objectId(), instance);

        if(ancestorField != null){
            Class<?> clazz = ancestorField.getFieldType();
            if(clazz.equals(Key.class)){
                Key ancestor = (Key) ancestorField.getFieldValue();
                // TODO: No use use it seems
            } else {
                throw new RuntimeException("Only " + Key.class + " is supported to be annoated with @Ancestor");
            }
        }

        if (objectIdField != null) {
            if(parentField != null && parentField.getFieldValue() != null){
                if(parentField.getFieldValue().getClass().equals(Key.class)){
                    parent = (Key) parentField.getFieldValue();
                }
            }
            Class<?> clazz = objectIdField.getFieldType();
            id = objectIdField.getFieldValue();
            if (clazz.equals(String.class)){
                if(id !=null){
                    key = KeyStructure.createKey(parent, kind, (String)id);
                } else {
                    ObjectId annotation = (ObjectId) objectIdField.annotation();
                    Key auto = KeyStructure.createKey(parent, kind, KeyStructure.autoLongId(kind));
                    Long autoId = auto.getId();
                    key = KeyStructure.createKey(parent, kind, autoId);
                }
            } else {
                throw new RuntimeException("Unsupported @ObjectId type " + id.getClass() + " Use String type only");
            }
        } else if (idField != null){
            if(parentField != null && parentField.getFieldValue() != null){
                if(parentField.getFieldValue().getClass().equals(Key.class)){
                    parent = (Key) parentField.getFieldValue();
                }
            }
            Class<?> clazz = idField.getFieldType();
            id = idField.getFieldValue();
            if (clazz.equals(String.class)){
                if(id !=null){
                    key = KeyStructure.createKey(parent, kind, (String)id);
                } else {
                    Id annotation = (Id) idField.annotation();
                    String prefix = annotation.prefix();
                    if(prefix != null && !prefix.isEmpty()){
                        Key auto = KeyStructure.createKey(parent, kind, KeyStructure.autoLongId(kind));
                        Long autoId = auto.getId();
                        key = KeyStructure.createKey(parent, kind, prefix + autoId);
                    } else {
                        throw new AutoGenerateStringIdException();
                    }
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
     * @param entity to marshall into {@code EmbeddedEntity}
     * @return marshalled
     */
    //TODO: This method is quite the most problematic part, since there is no list implementation in the Datastore, unlike with a <code>Map</code>.
    public EmbeddedEntity createEmbeddedEntityFromList(List entity){
        if(entity == null){
            throw new RuntimeException("List entity cannot be nulll");
        }
        EmbeddedEntity ee = null;
        try {
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

    /**
     * Creates a <code>EmbeddedEntity</code> from a <code>Map</code>
     * Which may include inner <code>EmbeddedEntity</code>.
     *
     * @param map to marshall into {@code EmbeddedEntity}
     * @return marshalled
     */
    public EmbeddedEntity createEmbeddedEntityFromMap(Map<String,Object> map){

        if(map == null){
            throw new RuntimeException("Map entity cannot be null");
        }

        // I think this is not necessary:
        // Deal with empty map
//        if (entity.size() == 0){
//            EmbeddedEntity ee = new EmbeddedEntity();
//            if (parent != null)
//                ee.setKey(parent);
//            return ee;
//        }

        EmbeddedEntity ee = null;

        Object oid = map.get(GaeObjectStore.KEY_RESERVED_PROPERTY); // TODO!


        Iterator<Map.Entry<String, Object>> it
                = map.entrySet().iterator();
        while (it.hasNext()){
            if (ee == null) {
                ee = new EmbeddedEntity();
            }
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
                Map<String, Object> newMap = (Map<String, Object>) value;
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
     * parent entity because JSON array can contain arbitrary values and even objects too.
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
     * @param ee {@code EmbeddedEntity} to unmarshall
     * @return {@code List} of unmarshalled {@code EmbeddedEntity}
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
     * @param ee {@code EmbeddedEntity} to unmarshall into map
     * @return unmarshalled
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
     * Used as a method in the chain of making a {@code EmbeddedEntity} from a given
     * POJO instance. Should not be used other than for EmbeddedEntity objects.
     *
     * @param instance TODO
     * @return Map
     */
    public Map<String,Object> createMapFrom(Object instance){
        Map<String,Object> result = Maps.toMap(instance);
        return result;
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
     * Helper method to convert a retrieved Date object from the GAE Datastore
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

    private static void setProperty(Entity entity, String key, Object value, boolean isIndexed){
        if(isIndexed){
            setIndexedProperty(entity, key, value);
        }else{
            setUnindexProperty(entity, key, value);
        }
    }

    private static void setUnindexProperty(Entity entity, String key, Object value){
        LOG.debug("Setting unindexed property key: " + key);

        if(entity == null){
            throw new RuntimeException("entity can't be null");
        }
        if(key == null){
            throw new RuntimeException("entity property key can't be null");
        }

        if (key.isEmpty()){
            throw new IllegalArgumentException("entity property key can't be empty");
        }


        if (value == null){
            entity.setUnindexedProperty(key, null);
            return;
        }

        if (!GAE_SUPPORTED_TYPES.contains(value.getClass())
                && !(value instanceof Blob) && !(value instanceof EmbeddedEntity)
                && !(value instanceof List)) {
            throw new RuntimeException("Unsupported type[class=" + value.
                    getClass().getName() + "] in GAE repository");
        }
        if (value instanceof String) {
            final String valueString = (String) value;
            if (valueString.length()
                    > DataTypeUtils.MAX_STRING_PROPERTY_LENGTH) {
                final Text text = new Text(valueString);

                entity.setUnindexedProperty(key, text);
            } else {
                entity.setUnindexedProperty(key, value);
            }
        } else if (value instanceof Number
                || value instanceof Date
                || value instanceof Boolean
                || GAE_SUPPORTED_TYPES.contains(value.getClass())) {
            entity.setUnindexedProperty(key, value);
        } else if (value instanceof EmbeddedEntity) {
            entity.setUnindexedProperty(key, value);
        } else if (value instanceof Blob) {
            final Blob blob = (Blob) value;
            entity.setUnindexedProperty(key,
                    new com.google.appengine.api.datastore.Blob(
                            blob.getBytes()));
        } else if(value instanceof List){
            entity.setUnindexedProperty(key, value);
        }
    }

    private static void setIndexedProperty(Entity entity, String key, Object value){

        if(entity == null){
            throw new RuntimeException("entity can't be null");
        }
        if(key == null){
            throw new RuntimeException("entity property key can't be null");
        }

        if (key.isEmpty()){
            throw new IllegalArgumentException("entity property key can't be empty");
        }


        if (value == null){
            entity.setProperty(key, null);
            return;
        }

        if (!GAE_SUPPORTED_TYPES.contains(value.getClass())
                && !(value instanceof Blob) && !(value instanceof EmbeddedEntity)
                && !(value instanceof List)) {
            throw new RuntimeException("Unsupported type[class=" + value.
                    getClass().getName() + "] in GAE repository");
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
        } else if(value instanceof List){
            entity.setProperty(key, value);
        }
    }

}
