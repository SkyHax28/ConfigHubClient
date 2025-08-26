package com.dew.utils;

import java.util.Base64;

public class Base64Util {

    public static String decodeToString(String string) {
        byte[] decodedBytes = Base64.getDecoder().decode(string);
        return new String(decodedBytes);
    }
}
