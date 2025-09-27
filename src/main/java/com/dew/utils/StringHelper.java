package com.dew.utils;

public class StringHelper {

    public static String removeMcColorCodes(String string) {
        if (!string.contains("§")) return string;
        return string.replaceAll("§.", "");
    }
}
