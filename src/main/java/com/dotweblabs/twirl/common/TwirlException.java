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
package com.dotweblabs.twirl.common;

@SuppressWarnings("serial")
public class TwirlException extends RuntimeException {

    public TwirlException() {
		super();
	}

    public TwirlException(String message){
		super(message);
	}
	
	public TwirlException(String message, Throwable cause){
		super(message, cause);
	}	

	public TwirlException(int code, String message){
		super("Exception " + code + " " + message);
	}

    public TwirlException(Throwable cause) {
        super(cause);
    }

}
