package com.dew.utils;

public class Clock {

    private long time = System.currentTimeMillis();

    public boolean hasTimeElapsed(long delay, boolean reset) {
        if (System.currentTimeMillis() - time > delay) {
            if (reset) {
                reset();
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean hasTimeElapsed(double delay, boolean reset) {
        return hasTimeElapsed((long) delay, reset);
    }

    public boolean hasTimeElapsed(int delay, boolean reset) {
        return hasTimeElapsed((long) delay, reset);
    }

    public boolean hasTimePassed(int delay) {
        return hasTimePassed((long) delay);
    }

    public boolean hasTimePassed(float delay) {
        return hasTimePassed((long) delay);
    }

    public boolean hasTimePassed(long delay) {
        return System.currentTimeMillis() >= time + delay;
    }

    public long hasTimeLeft(long delay) {
        return delay + time - System.currentTimeMillis();
    }

    public long getReachedTime() {
        return System.currentTimeMillis() - time;
    }

    public void reset() {
        time = System.currentTimeMillis();
    }

}
