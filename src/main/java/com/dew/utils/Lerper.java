package com.dew.utils;

public class Lerper {

    public static float lerp(float from, float to, float speed) {
        return from + (to - from) * speed;
    }
}
