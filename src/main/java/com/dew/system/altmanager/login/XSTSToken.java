package com.dew.system.altmanager.login;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class XSTSToken {
    public final String token;
    public final DisplayClaims displayClaims;

    public XSTSToken(String token, DisplayClaims displayClaims) {
        this.token = token;
        this.displayClaims = displayClaims;
    }

    public static XSTSToken fromJson(JsonObject json) {
        try {
            String token = json.get("Token").getAsString();
            JsonObject displayClaimsObj = json.getAsJsonObject("DisplayClaims");
            JsonArray xui = displayClaimsObj.getAsJsonArray("xui");

            int count = xui.size();
            DisplayClaims claims = new DisplayClaims(new Claim[count]);
            for (int i = 0; i < count; i++) {
                JsonObject element = xui.get(i).getAsJsonObject();
                String uhs = element.get("uhs").getAsString();
                claims.xui[i] = new Claim(uhs);
            }

            return new XSTSToken(token, claims);
        } catch (Throwable t) {
            throw new JsonParseException("Unable to parse Device Auth Code: " + json, t);
        }
    }
}
