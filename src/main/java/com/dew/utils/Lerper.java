package com.dew.utils;

public class Lerper {

    public static float lerp(float from, float to, float speed) {
        return from + (to - from) * speed;
    }

    public static float animate(float target, float current, float speed) {
        float diff = target - current;

        // 終了条件：十分近いなら完全に目標値へスナップ
        if (Math.abs(diff) < 0.1f) {
            return target;
        }

        // 線形補間
        return current + diff * Math.min(speed, 1f);
    }

    public static float easeOutCubic(float t) {
        return (float) (1 - Math.pow(1 - t, 3));
    }
}
