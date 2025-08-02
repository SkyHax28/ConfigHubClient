package com.dew.system.altmanager.alt.openauth.model;

/**
 * JSON model of an error
 *
 * @author Litarvan
 * @version 1.0.4
 */
public class AuthError {

    /**
     * Short description of the error
     */
    private final String error;

    /**
     * Longer description wich can be shown to the user
     */
    private final String errorMessage;

    /**
     * Cause of the error (optional)
     */
    private final String cause;

    /**
     * Auth Error constructor
     *
     * @param error        Short description of the error
     * @param errorMessage Longer description wich can be shown to the user
     * @param cause        Cause of the error
     */
    public AuthError(String error, String errorMessage, String cause) {
        this.error = error;
        this.errorMessage = errorMessage;
        this.cause = cause;
    }

    /**
     * Returns the error (Short description of the error)
     *
     * @return The error
     */
    public String getError() {
        return error;
    }

    /**
     * Returns the error message (Longer description of the error)
     *
     * @return The error message
     */
    public String getErrorMessage() {
        return this.errorMessage;
    }

    /**
     * Returns the cause of the error
     *
     * @return The cause
     */
    public String getCause() {
        return cause;
    }

}
