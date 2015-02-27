package com.textquo.twist.types;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.textquo.twist.util.StringHelper;

import java.lang.annotation.Annotation;
import java.util.IdentityHashMap;
import java.util.List;

import static org.boon.Lists.list;

public class QueryBuilder {

    protected Class clazz;
    private IdentityHashMap<Class<?>,String> cls = new IdentityHashMap<Class<?>,String>();


    public <T> Query build(Class<T> clazz, Key ancestor){
        Query q = null;
        if(ancestor != null){
            q = new Query(getKind(clazz), ancestor);
        } else {
            q = new Query(getKind(clazz));
        }
        return q;
    }

    public <T> Query build(Class<T> clazz){
        Query q = null;
        q = new Query(getKind(clazz));
        return q;
    }

    public String getKind(Class<?> clazz){
        String kind =  cls.get(clazz);
        if (kind == null){
            register(clazz);
            return cls.get(clazz);
        } else {
            return cls.get(clazz);
        }
    }

    /**
     * Register the class into DS kind
     * TODO: Register or just call this check for each operation?
     * @param clazz type to register
     */
    public void register(Class<?> clazz){
        List<Annotation> annotations = list(clazz.getAnnotations());
        if(annotations.isEmpty()){
            String kind = StringHelper.getClassNameFrom(clazz.getName());
            cls.put(clazz, kind);
        } else {
            for (Annotation annotation : annotations) {
                com.textquo.twist.annotations.Entity entityAnnotation = null;
                if(annotation instanceof com.textquo.twist.annotations.Entity){
                    entityAnnotation = (com.textquo.twist.annotations.Entity)annotation;
                }
                if(entityAnnotation != null){
                    String entityName = entityAnnotation.name();
                    if (cls.get(clazz) == null){
                        if (entityName == null || entityName.isEmpty()){
                            cls.put(clazz, StringHelper.getClassNameFrom(clazz.getName()));
                        } else {
                            cls.put(clazz, entityName);
                        }
                    }
                }
            }
            // Fallback
            if (cls.get(clazz) == null){
                String kind = StringHelper.getClassNameFrom(clazz.getName());
                if (kind == null || kind.isEmpty()){
                    cls.put(clazz, StringHelper.getClassNameFrom(clazz.getName()));
                } else {
                    cls.put(clazz, kind);
                }
            }
        }
    }
}
