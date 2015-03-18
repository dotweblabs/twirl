package com.pastekit.twist.entity;

import com.pastekit.twist.annotations.Entity;
import com.pastekit.twist.annotations.Flat;
import com.pastekit.twist.annotations.Id;
import com.pastekit.twist.annotations.Kind;

import java.util.Map;

/**
 * Created by kmartino on 1/9/15.
 */
@Entity
public class CustomKind {
    @Kind
    private String kind;

    @Id
    private Long id;

    private Long value;

    @Flat
    private Map fields;

    public CustomKind(){}

    public CustomKind(String kind, Long value){
        setKind(kind);
        setValue(value);
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public Map getFields() {
        return fields;
    }

    public void setFields(Map fields) {
        this.fields = fields;
    }
}
