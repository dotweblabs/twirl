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
package com.textquo.twist.entity;

import com.textquo.twist.annotations.Child;
import com.textquo.twist.annotations.Embedded;
import com.textquo.twist.annotations.Id;

import java.util.List;

public class RootEntity {

    @Id
    private String key;
    private Integer count;
    @Child
    private ChildEntity newChildEntity;
    @Embedded
    private ChildEntity embeddedEntity;

    private List<ChildEntity> children;

    public RootEntity() {}

    public RootEntity(String key) {
        this.key = key;
    }

    public RootEntity(String key, Integer count){
        setId(key);
        setCount(count);
    }

    public RootEntity(String key, Integer count, ChildEntity childEntity){
        setId(key);
        setCount(count);
        setNewChildEntity(childEntity);
    }

    public RootEntity(String key, Integer count, ChildEntity newChildEntity, ChildEntity embeddedEntity){
        setId(key);
        setCount(count);
        setNewChildEntity(newChildEntity);
        setEmbeddedEntity(embeddedEntity);
    }

    public ChildEntity getNewChildEntity() {
        return newChildEntity;
    }

    public void setNewChildEntity(ChildEntity newChildEntity) {
        this.newChildEntity = newChildEntity;
    }

    public String getKey() {
        return key;
    }

    public void setId(String key) {
        this.key = key;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "RootEntity"
                +" key="+ key
                +" count=" + count
                +" newChildEntity=" + newChildEntity;
    }

    public ChildEntity getEmbeddedEntity() {
        return embeddedEntity;
    }

    public void setEmbeddedEntity(ChildEntity childEntity){
        this.embeddedEntity = childEntity;
    }

    public List<ChildEntity> getChildren() {
        return children;
    }

    public void setChildren(List<ChildEntity> children) {
        this.children = children;
    }
}
