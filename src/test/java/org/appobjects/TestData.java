package org.appobjects;

import org.appobjects.annotations.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by kerby on 4/24/14.
 */
public class TestData {

    public static class ChildChildEntity {

        @Id
        private Long id;

        @Child
        private ChildEntity child;

        private String type;

        public ChildChildEntity() {}

        public ChildChildEntity(String type){
            setType(type);
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return "[" + type + "]";
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public ChildEntity getChild() {
            return child;
        }

        public void setChild(ChildEntity child) {
            this.child = child;
        }
    }

    //@Entity(name = "ChildKind")
    public static class ChildEntity {

        @Id
        private Long id;

        @Parent
        private RootEntity parent;

        private String type;

        public ChildEntity() {}

        public ChildEntity(String type){
            setType(type);
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return "[" + type + "]";
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public RootEntity getParent() {
            return parent;
        }

        public void setParent(RootEntity parent) {
            this.parent = parent;
        }
    }

    public static class RootEntity {

        @Id
        private String key;
        private Integer count;
        @Child
        private ChildEntity newChildEntity;
        @Embedded
        private ChildEntity embeddedEntity;

        public RootEntity() {}

        public RootEntity(String key) {
            this.key = key;
        }

        public RootEntity(String key, Integer count){
            setId(key);
            setCount(count);
        }

        public RootEntity(String key, Integer count, ChildEntity childEntity){
            setId(key);
            setCount(count);
            setNewChildEntity(childEntity);
        }

        public RootEntity(String key, Integer count, ChildEntity newChildEntity, ChildEntity embeddedEntity){
            setId(key);
            setCount(count);
            setNewChildEntity(newChildEntity);
            setEmbeddedEntity(embeddedEntity);
        }

        public ChildEntity getNewChildEntity() {
            return newChildEntity;
        }

        public void setNewChildEntity(ChildEntity newChildEntity) {
            this.newChildEntity = newChildEntity;
        }

        public String getKey() {
            return key;
        }

        public void setId(String key) {
            this.key = key;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        @Override
        public String toString() {
            return "RootEntity"
                    +" key="+ key
                    +" count=" + count
                    +" newChildEntity=" + newChildEntity;
        }

        public ChildEntity getEmbeddedEntity() {
            return embeddedEntity;
        }

        public void setEmbeddedEntity(ChildEntity childEntity){
            this.embeddedEntity = childEntity;
        }
    }

    /**
     * Note, getDeclaredField is empty for this class
     */
    @Entity(name = "CustomEntityName")
    public static class RootEntityWithAnno extends RootEntity {
    }

    /**
     * Note, getDeclaredField is empty for this class
     */
    @Entity
    public static class RootEntityWithNoAnno extends RootEntity {
    }

    public static class User {
        private String username;
        public User(){}
        public User(String username){
            this.username = username;
        }
        public String getUsername() {
            return username;
        }
        public void setUsername(String username) {
            this.username = username;
        }

    }

    public static RootEntity createTestRootEnity(){
        RootEntity rootObject = new RootEntity(); // one Entity
        ChildEntity childObject = new ChildEntity("TestChild");
        ChildEntity embeddedObject = new ChildEntity("TestEmbedded");

        rootObject.setId("TestRoot");
        rootObject.setCount(25);
        rootObject.setNewChildEntity(childObject); // one Entity
        rootObject.setEmbeddedEntity(embeddedObject); // not included, @Embedded

        return rootObject;
    }

    public static class JSONEntity {

        @Kind
        private String kind;

        @Id
        private String id;
        @Flat
        private Map<String,Object> fields;

        public Map<String, Object> getFields() {
            if(fields == null){
                fields = new LinkedHashMap<>();
            }
            return fields;
        }

        public void setFields(Map<String, Object> fields) {
            if(fields == null){
                fields = new LinkedHashMap<>();
            }
            this.fields = fields;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getKind() {
            return kind;
        }

        public void setKind(String kind) {
            this.kind = kind;
        }
    }
}
