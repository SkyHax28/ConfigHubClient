package com.dew.utils;

public class Lerper {

    public static float lerp(float from, float to, float speed) {
        return from + (to - from) * speed;
    }

    public static float easeOutCubic(float t) {
        return (float) (1 - Math.pow(1 - t, 3));
    }
}
