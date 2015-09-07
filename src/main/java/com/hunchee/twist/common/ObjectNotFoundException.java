package com.hunchee.twist.common;

import com.google.appengine.api.datastore.EntityNotFoundException;

public class ObjectNotFoundException extends TwistException {
	private static final long serialVersionUID = 6006726482159235720L;

	public ObjectNotFoundException(String message, EntityNotFoundException e){
        super(message, e);
    }
}
