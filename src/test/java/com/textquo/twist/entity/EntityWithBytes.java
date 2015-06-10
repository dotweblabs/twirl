package com.textquo.twist.entity;

import com.textquo.twist.annotations.Id;
import com.textquo.twist.annotations.Entity;
import com.textquo.twist.annotations.Id;
import com.textquo.twist.annotations.Entity;
import com.textquo.twist.annotations.Id;

@Entity
public class EntityWithBytes {
    @Id
    private Long id;
    private byte[] bytes;

    public EntityWithBytes(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}
