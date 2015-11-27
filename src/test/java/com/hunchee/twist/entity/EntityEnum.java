package com.hunchee.twist.entity;

import com.hunchee.twist.annotations.Entity;
import com.hunchee.twist.annotations.Id;

public class EntityEnum {

    public TestEnum getTestEnum() {
        return testEnum;
    }

    public void setTestEnum(TestEnum testEnum) {
        this.testEnum = testEnum;
    }

    public enum TestEnum {
        ONE, TWO, THREE
    }

    @Id
    private Long id;

    private TestEnum testEnum;

    public EntityEnum() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
