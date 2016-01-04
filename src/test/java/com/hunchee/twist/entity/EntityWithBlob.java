package com.hunchee.twist.entity;

import com.google.appengine.api.datastore.Blob;
import com.hunchee.twist.annotations.Entity;
import com.hunchee.twist.annotations.Id;

@Entity
public class EntityWithBlob {
    @Id
    private Long id;
    private Blob blob;

    public EntityWithBlob(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Blob getBlob() {
        return blob;
    }

    public void setBlob(Blob blob) {
        this.blob = blob;
    }
}
