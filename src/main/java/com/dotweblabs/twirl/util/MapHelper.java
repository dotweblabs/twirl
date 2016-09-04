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

package com.dotweblabs.twirl.util;

import java.util.Iterator;
import java.util.Map;

public class MapHelper {
    public static String getKeyType(Map map){
        String keyKind = Object.class.getName();
        Iterator<Map.Entry<Object,Object>> it = map.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<Object,Object> entry = it.next();
            Object entryKey = entry.getKey();
            keyKind = entryKey.getClass().getName();
        }
        return keyKind;
    }
    public static String getValueType(Map map){
        String valueKind = Object.class.getName();
        Iterator<Map.Entry<Object,Object>> it = map.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<Object,Object> entry = it.next();
            Object entryVal = entry.getValue();
            valueKind = entryVal.getClass().getName();
        }
        return valueKind;
    }
}
