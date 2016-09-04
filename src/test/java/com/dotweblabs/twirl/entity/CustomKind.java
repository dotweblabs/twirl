/**
 *
 * Copyright (c) 2016 Dotweblabs Web Technologies and others. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  __            __       __
 * |  |_.--.--.--|__.-----|  |
 * |   _|  |  |  |  |   --|  |_
 * |____|________|__|___| |____|
 * :: twirl :: Object Mapping ::
 *
 */
package com.dotweblabs.twirl.entity;

import com.dotweblabs.twirl.annotations.Entity;
import com.dotweblabs.twirl.annotations.Flat;
import com.dotweblabs.twirl.annotations.Id;
import com.dotweblabs.twirl.annotations.Kind;

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
