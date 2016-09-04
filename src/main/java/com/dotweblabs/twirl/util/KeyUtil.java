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

import com.google.appengine.api.datastore.Key;

public class KeyUtil {

  private KeyUtil() {}
  
  public static Key getRootKey(Key key) {
    Key rootKey = key;
    while (rootKey.getParent() != null) {
      rootKey = rootKey.getParent();
    }
    return rootKey;
  }
  
  public static boolean inSameEntityGroup(Key... keys) {
    Key entityGroupKey = null;
    
    for (Key key : keys) {
      Key currentRootKey = getRootKey(key);
      
      if (entityGroupKey == null) {
        entityGroupKey = currentRootKey;
      } else if (!entityGroupKey.equals(currentRootKey)) {
        return false;
      }
    }
    
    return true;
  }
  
}
