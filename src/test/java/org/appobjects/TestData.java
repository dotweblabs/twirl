package org.appobjects;

import org.appobjects.annotations.*;

/**
 * Created by kerby on 4/24/14.
 */
public class TestData {
    //@Entity(name = "ChildKind")
    public static class TestEntity {

        @Id
        private String id;

        @Parent
        private RootEntity parent;

        private String type;

        public TestEntity() {}

        public TestEntity(String type){
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

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    public static class RootEntity {

        @Id
        private String key;
        private Integer count;
        @Child
        private TestEntity newTestEntity;
        @Embedded
        private TestEntity oldTestEntity;

        public RootEntity() {}

        public RootEntity(String key, Integer count){
            setKey(key);
            setCount(count);
        }

        public RootEntity(String key, Integer count, TestEntity testEntity){
            setKey(key);
            setCount(count);
            setNewTestEntity(testEntity);
        }

        public RootEntity(String key, Integer count, TestEntity newTestEntity, TestEntity oldTestEntity){
            setKey(key);
            setCount(count);
            setNewTestEntity(newTestEntity);
            setOldTestEntity(oldTestEntity);
        }

        public TestEntity getNewTestEntity() {
            return newTestEntity;
        }

        public void setNewTestEntity(TestEntity newTestEntity) {
            this.newTestEntity = newTestEntity;
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
                    +" newTestEntity=" + newTestEntity;
        }

        public TestEntity getOldTestEntity() {
            return oldTestEntity;
        }

        public void setOldTestEntity(TestEntity testEntity){
            this.oldTestEntity = testEntity;
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
        TestEntity childObject = new TestEntity("Test City");
        TestEntity embeddedObject = new TestEntity("Old Test City");

        childObject.setId("SomeUniqueId1");

        rootObject.setKey("TestUser");
        rootObject.setCount(25);
        rootObject.setNewTestEntity(childObject); // one Entity
        rootObject.setOldTestEntity(embeddedObject); // not included, @Embedded

        return rootObject;
    }
}
