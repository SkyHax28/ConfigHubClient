package com.dew.utils;

import com.dew.IMinecraft;

public class TimerUtil {

    public static void setTimerSpeed(float speed) {
        IMinecraft.mc.timer.timerSpeed = speed;
    }

    public static void resetTimerSpeed() {
        setTimerSpeed(1.0f);
    }
}