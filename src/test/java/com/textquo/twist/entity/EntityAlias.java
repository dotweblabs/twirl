package com.textquo.twist.entity;

import com.textquo.twist.annotations.Alias;
import com.textquo.twist.annotations.Entity;
import com.textquo.twist.annotations.Id;

@DummyAnnotation
@Entity(name = "Entity")
public class EntityAlias {

    @Id
    private Long id;

    private String content;

    @Alias(field = "activeFlag")
    private boolean active;

    private Boolean activeFlag;

    public EntityAlias() {
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Boolean isActiveFlag() {
        return activeFlag;
    }

    public void setActiveFlag(Boolean activeFlag) {
        this.activeFlag = activeFlag;
    }
}
