package com.pastekit.twist.entity;

import com.pastekit.twist.annotations.Id;
import com.pastekit.twist.annotations.Entity;
import com.pastekit.twist.annotations.Id;

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
