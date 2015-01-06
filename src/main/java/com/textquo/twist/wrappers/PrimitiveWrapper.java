package com.textquo.twist.wrappers;

public class PrimitiveWrapper<T> {
    private T value = null;
    public PrimitiveWrapper(T value){
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
