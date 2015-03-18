package com.pastekit.twist.entity;

import com.google.appengine.api.datastore.Key;
import com.pastekit.twist.annotations.Ancestor;
import com.pastekit.twist.annotations.Entity;
import com.pastekit.twist.annotations.Ancestor;

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
