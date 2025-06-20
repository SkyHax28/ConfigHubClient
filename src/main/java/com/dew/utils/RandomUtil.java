package com.dew.utils;

import java.util.Random;

public class RandomUtil {

    private static final Random RANDOM = new Random();

    public static int nextInt(int startInclusive, int endExclusive) {
        if (endExclusive - startInclusive <= 0) {
            return startInclusive;
        } else {
            return startInclusive + RANDOM.nextInt(endExclusive - startInclusive);
        }
    }

    public static int nextInt() {
        return nextInt(0, Integer.MAX_VALUE);
    }

    public static double nextDouble(double startInclusive, double endInclusive) {
        if (startInclusive == endInclusive || endInclusive - startInclusive <= 0.0) {
            return startInclusive;
        } else {
            return startInclusive + (endInclusive - startInclusive) * RANDOM.nextDouble();
        }
    }

    public static double nextDouble() {
        return nextDouble(0.0, 1.0);
    }

    public static float nextFloat(float startInclusive, float endInclusive) {
        if (startInclusive == endInclusive || endInclusive - startInclusive <= 0f) {
            return startInclusive;
        } else {
            return (float) (startInclusive + (endInclusive - startInclusive) * RANDOM.nextDouble());
        }
    }

    public static float nextFloat() {
        return nextFloat(0f, 1f);
    }

    public static boolean nextBoolean() {
        return RANDOM.nextBoolean();
    }

    public static String randomString(int length) {
        return random(length, "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
    }

    public static String random(int length, String chars) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            stringBuilder.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return stringBuilder.toString();
    }
}

