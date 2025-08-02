package com.dew.system.altmanager.alt.openauth.microsoft;

import com.dew.system.altmanager.alt.openauth.microsoft.model.response.MinecraftProfile;

/**
 * Microsoft authentication result
 *
 * <p>
 * This class contains the result of a successful Microsoft authentication: a player profile and its tokens (both
 * access and refresh token).
 * </p>
 *
 * @author Litarvan
 * @version 1.1.0
 */
public class MicrosoftAuthResult {
    private final MinecraftProfile profile;
    private final String accessToken;
    private final String refreshToken;

    public MicrosoftAuthResult(MinecraftProfile profile, String accessToken, String refreshToken) {
        this.profile = profile;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    /**
     * @return The player Minecraft profile (contains its UUID and username)
     */
    public MinecraftProfile getProfile() {
        return profile;
    }

    /**
     * @return The Minecraft access token
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * @return The Microsoft refresh token that can be used to log the user back silently using
     * {@link MicrosoftAuthenticator#loginWithRefreshToken(String)}
     */
    public String getRefreshToken() {
        return refreshToken;
    }
}
