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

package com.dotweblabs.twirl.types;

import java.util.LinkedList;
import java.util.List;

public class ListResult<T> {
    private String websafeCursor;
    private Cursor cursor;
    private List<T> list = new LinkedList<>();

    public ListResult(){}

    public ListResult(String websafeCursor, List<T> list) {
        this.cursor = new Cursor(websafeCursor);
        this.websafeCursor = websafeCursor;
        this.list = list;
    }

    public List<T> getList() {
        if(list == null){
            list = new LinkedList<T>();
        }
        return list;
    }

    public void setWebsafeCursor(String websafeCursor){
        this.cursor = new Cursor(websafeCursor);
        this.websafeCursor = websafeCursor;
    }

    public String getWebsafeCursor() {
        return websafeCursor;
    }

    public Cursor getCursor() {
        if(websafeCursor == null){
            return null;
        }
        return cursor;
    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
        this.websafeCursor = cursor.getWebSafeString();
    }
}
