package com.hunchee.twist.entity;

import com.hunchee.twist.annotations.Flat;
import com.hunchee.twist.annotations.Id;
import com.hunchee.twist.annotations.Kind;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class JSONEntityWithMap implements Serializable {

	@Kind
	private String kind;

	@Id
	private Long id;

	@Flat
	private Map<String,Object> fields;

	public JSONEntityWithMap() {
		Map<String,Object> fields = new LinkedHashMap<>();
		setFields(fields);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Map<String, Object> getFields() {
		return fields;
	}

	public void setFields(Map<String, Object> fields) {
		this.fields = fields;
	}

	public void setField(String fieldName, String fieldType) {
		Map<String,String> fieldTypeMap = new LinkedHashMap<>();
		fieldTypeMap.put("type", fieldType);
		getFields().put(fieldName, fieldTypeMap);
	}

	public String getField(String fieldName) {
		return (String) ((Map)getFields().get(fieldName)).get("type");
	}

}
