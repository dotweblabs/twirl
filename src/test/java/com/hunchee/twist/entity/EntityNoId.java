package com.hunchee.twist.entity;

import com.google.appengine.api.datastore.Key;
import com.hunchee.twist.annotations.Ancestor;
import com.hunchee.twist.annotations.Entity;

@Entity
public class EntityNoId {
    @Ancestor
    private Key parent;

    private String content;

    public EntityNoId() {
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Key getParent() {
        return parent;
    }

    public void setParent(Key parent) {
        this.parent = parent;
    }
}
