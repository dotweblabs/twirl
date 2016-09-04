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
package com.dotweblabs.twirl;

import java.util.ArrayDeque;
import java.util.Deque;

public class ObjectStoreService {

    private static ObjectStoreFactory factory = new ObjectStoreFactory();

    private static final ThreadLocal<Deque<ObjectStore>> STACK = new ThreadLocal<Deque<ObjectStore>>() {
        @Override
        protected Deque<ObjectStore> initialValue() {
            return new ArrayDeque<ObjectStore>();
        }
    };

    public ObjectStoreService(){

    }

    public static ObjectStore store(){
        Deque<ObjectStore> stack = STACK.get();
        if (stack.isEmpty())
            stack.add(factory.begin());
        return stack.getLast();
    }
}
