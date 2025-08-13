package com.dew.utils;

import net.minecraft.util.Vec3;

import java.awt.*;

public class Lerper {

    public static float deltaTime = 1.0F;

    public static float lerp(float from, float to, float speed) {
        return from + (to - from) * speed;
    }

    public static double lerpDouble(double from, double to, float speed) {
        return from + (to - from) * speed;
    }

    public static float lerpDoubleDelta(float from, float to, double speed) {
        return (float) (from + (to - from) * speed);
    }

    public static float animate(float target, float current, float speed) {
        float diff = target - current;

        if (Math.abs(diff) < 0.1f) {
            return target;
        }

        return current + diff * Math.min(speed, 1f);
    }

    public static float easeOutCubic(float t) {
        return (float) (1 - Math.pow(1 - t, 3));
    }

    public static Color lerpColor(Color a, Color b, float t) {
        int r = (int) (a.getRed() + (b.getRed() - a.getRed()) * t);
        int g = (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int bl = (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t);
        return new Color(r, g, bl);
    }

    public static Vec3 lerpVec3(Vec3 from, Vec3 to, double tickDelta) {
        return new Vec3(
                from.xCoord + (to.xCoord - from.xCoord) * tickDelta,
                from.yCoord + (to.yCoord - from.yCoord) * tickDelta,
                from.zCoord + (to.zCoord - from.zCoord) * tickDelta
        );
    }

    public static double deltaTimeNormalized(int ticks) {
        if (ticks <= 0) ticks = 1;
        return Math.min((deltaTime / ticks) * 50.0, 1.0);
    }

    public static double safeDivD(double value, double divisor) {
        return divisor == 0.0 ? 0.0 : value / divisor;
    }
}
