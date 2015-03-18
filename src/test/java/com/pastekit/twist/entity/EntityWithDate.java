package com.pastekit.twist.entity;

import com.pastekit.twist.annotations.Entity;
import com.pastekit.twist.annotations.Id;
import com.pastekit.twist.annotations.Entity;
import com.pastekit.twist.annotations.Id;

import java.util.Date;

@Entity
public class EntityWithDate {
    @Id
    private String id;
    private Date created;
    public EntityWithDate(){}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
}
