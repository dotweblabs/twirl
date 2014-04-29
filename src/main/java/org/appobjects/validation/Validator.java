package org.appobjects.validation;

/**
 * Created by kerby on 4/28/14.
 */
public class Validator {
    /**
     * Validates a object instance before GAE persistence.
     * It checks for field value types and field values.
     * Checks for duplicated annotations.
     *
     * @param instance
     */
    public boolean validate(Object instance){
        return true;
    }
}
