package org.appobjects;

import org.appobjects.annotations.*;

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
        private ChildEntity oldChildEntity;

        public RootEntity() {}

        public RootEntity(String key, Integer count){
            setKey(key);
            setCount(count);
        }

        public RootEntity(String key, Integer count, ChildEntity childEntity){
            setKey(key);
            setCount(count);
            setNewChildEntity(childEntity);
        }

        public RootEntity(String key, Integer count, ChildEntity newChildEntity, ChildEntity oldChildEntity){
            setKey(key);
            setCount(count);
            setNewChildEntity(newChildEntity);
            setOldChildEntity(oldChildEntity);
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

        public void setKey(String key) {
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

        public ChildEntity getOldChildEntity() {
            return oldChildEntity;
        }

        public void setOldChildEntity(ChildEntity childEntity){
            this.oldChildEntity = childEntity;
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
        ChildEntity childObject = new ChildEntity("Test City");
        ChildEntity embeddedObject = new ChildEntity("Old Test City");

        rootObject.setKey("TestUser");
        rootObject.setCount(25);
        rootObject.setNewChildEntity(childObject); // one Entity
        rootObject.setOldChildEntity(embeddedObject); // not included, @Embedded

        return rootObject;
    }
}
