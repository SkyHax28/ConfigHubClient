package com.dew.system.altmanager.alt.openauth.model;

/**
 * JSON model of a profile
 *
 * @author Litarvan
 * @version 1.0.4
 */
public class AuthProfile {

    /**
     * The profile name
     */
    private final String name;

    /**
     * The profile UUID
     */
    private final String id;

    /**
     * Blank auth profile
     */
    public AuthProfile() {
        this.name = "";
        this.id = "";
    }

    /**
     * Normal auth profile
     *
     * @param name The profile name
     * @param id   The profile UUID
     */
    public AuthProfile(String name, String id) {
        this.name = name;
        this.id = id;
    }

    /**
     * Returns the name of the profile
     *
     * @return The profile name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the profile UUID
     *
     * @return The profile UUID
     */
    public String getId() {
        return this.id;
    }

}
