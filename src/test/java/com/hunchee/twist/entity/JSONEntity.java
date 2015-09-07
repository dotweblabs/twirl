/**
 *
 * Copyright (c) 2014 Kerby Martino and others. All rights reserved.
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
 * |  |_.--.--.--|__.-----|  |_
 * |   _|  |  |  |  |__ --|   _|
 * |____|________|__|_____|____|
 * :: Twist :: Object Mapping ::
 *
 */
package com.hunchee.twist.entity;

import com.hunchee.twist.annotations.Flat;
import com.hunchee.twist.annotations.Id;
import com.hunchee.twist.annotations.Kind;

import java.util.LinkedHashMap;
import java.util.Map;

public class JSONEntity {

    @Kind
    private String kind;

    @Id
    private String id;

    private String content;

    @Flat
    private Map<String,Object> fields;

    public Map<String, Object> getFields() {
        if(fields == null){
            fields = new LinkedHashMap<>();
        }
        return fields;
    }

    public void setFields(Map<String, Object> fields) {
        if(fields == null){
            fields = new LinkedHashMap<>();
        }
        this.fields = fields;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
