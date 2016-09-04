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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

public class AnnotationUtil {

    public static class AnnotatedField  {
        private Class<?> clazz;
        private Class<? extends Annotation> type;
        private Field field;
        private Object obj;

        private String fieldName;
        private Object fieldValue;

        public AnnotatedField(){};
        public AnnotatedField(Class<? extends Annotation> type, Object obj, Field field){
            this.type = type;
            this.field = field;
            this.obj = obj;
            this.clazz = obj.getClass();
            try {
                boolean isAccessible = field.isAccessible(); // Do I need this?
                field.setAccessible(true);
                this.fieldName = field.getName();
                this.fieldValue = field.get(obj);
            } catch (IllegalAccessException ex){

            }
        };

        public Class<?> getFieldType(){
            return field.getType();
        }

        public String getFieldName() {;
            return fieldName;
        }

        public Object getFieldValue(){
            return fieldValue;
        }

        public void setFieldValue(Object newValue) {
            boolean isAccessible = field.isAccessible(); // Do I need this?
            field.setAccessible(true);
            Class fieldType = field.getType();
            try {
                Class newType = newValue.getClass().getClass();
                field.set(obj, newValue);
                fieldValue = newValue; // update
                field.setAccessible(isAccessible);
            } catch (IllegalAccessException ex){
                ex.printStackTrace(); // TODO
            }
        }

        public Field getField(){
            return this.field;
        }

        public Annotation annotation(){
            return field.getAnnotation(type);
        }
    }

    public static AnnotatedField getFieldWithAnnotation(Class<? extends Annotation> clazz, Object instance){
        for (Field field : instance.getClass().getDeclaredFields()){
            if (field.isAnnotationPresent(clazz)){
                return new AnnotatedField(clazz, instance, field);
            }
        }
        return null;
    }

    /**
     * Returns the field name(s) and the value(s)
     * @param clazz annotation type
     * @param instance object to check
     * @return
     */
    public static List<AnnotatedField> getFieldsWithAnnotation(Class<? extends Annotation> clazz, Object instance){
        List<AnnotatedField> fields = null;
        for (Field field : instance.getClass().getDeclaredFields()){
            // Keep in mind the original field accessible state
            boolean isAccessible = field.isAccessible(); // Do I need this?
            field.setAccessible(true);
            try {
                if (field.isAnnotationPresent(clazz)){
                    if (fields==null){
                        fields = new LinkedList<AnnotatedField>();
                    }
                    fields.add(new AnnotatedField(clazz, instance, field));
                }
                field.setAccessible(isAccessible);
            } catch (Exception e){

            }
        }
        return fields;
    }

    public static List<Annotation> getAnnotations(Object instance){
        return new ArrayList<Annotation>(Arrays.asList(instance.getClass().getAnnotations()));
    }

    public static Annotation getClassAnnotation(Class<? extends Annotation> clazz, Object instance){
        List<Annotation> list = getAnnotations(instance);
        for (Annotation a : list){
            if (a.annotationType().equals(clazz)){
                return a;
            }
        }
        return null;
    }

    public static boolean isClassAnnotated(Class<? extends Annotation> clazz, Object instance){
        return instance.getClass().isAnnotationPresent(clazz);
    }

}