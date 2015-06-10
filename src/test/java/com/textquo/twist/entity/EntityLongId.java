package com.textquo.twist.entity;

import com.google.appengine.api.datastore.Key;
import com.textquo.twist.annotations.Entity;
import com.textquo.twist.annotations.Id;
import com.textquo.twist.annotations.Ancestor;
import com.textquo.twist.annotations.Entity;
import com.textquo.twist.annotations.Id;
import com.textquo.twist.annotations.Entity;
import com.textquo.twist.annotations.Id;

@DummyAnnotation
@Entity(name = "Entity")
public class EntityLongId {

    @Id
    private Long id;

    private String content;

    public EntityLongId() {
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
