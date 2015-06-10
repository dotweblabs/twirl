package com.textquo.twist.entity;

import com.textquo.twist.annotations.Entity;
import com.textquo.twist.annotations.Id;
import com.textquo.twist.annotations.Entity;
import com.textquo.twist.annotations.Id;
import com.textquo.twist.annotations.Entity;
import com.textquo.twist.annotations.Id;

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
