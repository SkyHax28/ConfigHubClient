package com.dew.system.altmanager.alt.openauth;

import com.dew.system.altmanager.alt.openauth.model.AuthError;

/**
 * Authentication exceptions
 *
 * @author Litarvan
 * @version 1.0.4
 */
public class AuthenticationException extends Exception {

    /**
     * The given JSON model instance of the error
     */
    private final AuthError model;

    /**
     * Create a new Authentication Exception
     *
     * @param model The given JSON model instance of the error
     */
    public AuthenticationException(AuthError model) {
        super(model.getErrorMessage());
        this.model = model;
    }

    /**
     * Returns the given JSON model instance of the error
     *
     * @return The error model
     */
    public AuthError getErrorModel() {
        return model;
    }
}