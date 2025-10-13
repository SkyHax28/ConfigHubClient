package com.dew.utils.rawinput;

import com.dew.DewCommon;
import com.dew.IMinecraft;
import com.dew.system.module.modules.other.RawInput;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Mouse;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.Arrays;

public final class RawInputThread extends Thread {

    public static final RawInputThread INSTANCE = new RawInputThread();

    public final AtomicInteger dx = new AtomicInteger(0);
    public final AtomicInteger dy = new AtomicInteger(0);

    public volatile List<Mouse> mice = Collections.emptyList();

    private RawInputThread() {
        super("Raw Mouse Input");
        setDaemon(true);
    }

    @Override
    public void run() {
        rescan();

        while (true) {
            try {
                if (RawInput.shouldReplace() && DewCommon.moduleManager.getModule(RawInput.class).isEnabled() && !IMinecraft.mc.gameSettings.touchscreen) {
                    for (Mouse mouse : mice) {
                        if (!mouse.poll()) {
                            rescan();
                        }

                        dx.addAndGet((int) mouse.getX().getPollData());
                        dy.addAndGet(-(int) mouse.getY().getPollData());
                    }
                    Thread.sleep(1);
                } else {
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public void rescan() {
        try {
            Class<?> clazz = Class.forName("net.java.games.input." + IMinecraft.mc.environment);
            ControllerEnvironment env = (ControllerEnvironment) clazz.getDeclaredConstructor().newInstance();

            mice = Arrays.stream(env.getControllers())
                    .filter(c -> c instanceof Mouse)
                    .map(c -> (Mouse) c)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reset() {
        dx.set(0);
        dy.set(0);
    }
}