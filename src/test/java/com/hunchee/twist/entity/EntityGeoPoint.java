package com.hunchee.twist.entity;

import com.google.appengine.api.datastore.GeoPt;
import com.hunchee.twist.annotations.Id;
import com.hunchee.twist.annotations.Entity;

@DummyAnnotation
@Entity(name = "Entity")
public class EntityGeoPoint {

    @Id
    private Long id;

    private GeoPt geoPoint;

    private String content;

    public EntityGeoPoint() {
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

    public GeoPt getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPt geoPoint) {
        this.geoPoint = geoPoint;
    }
}
