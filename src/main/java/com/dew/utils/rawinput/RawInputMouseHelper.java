package com.dew.utils.rawinput;

import com.dew.DewCommon;
import com.dew.system.module.modules.mods.RawInput;
import net.minecraft.util.MouseHelper;
import org.lwjgl.input.Mouse;

public class RawInputMouseHelper extends MouseHelper {
    private int fails = 0;

    public RawInputMouseHelper() {
        RawInputThread.INSTANCE.start();
    }

    @Override
    public void grabMouseCursor() {
        RawInputThread.INSTANCE.reset();
        super.grabMouseCursor();
    }

    @Override
    public void mouseXYChange() {
        if (RawInputThread.INSTANCE.getState() == Thread.State.NEW) {
            RawInputThread.INSTANCE.start();
        }

        if (DewCommon.moduleManager.getModule(RawInput.class).isEnabled() && !RawInputThread.INSTANCE.mice.isEmpty() && RawInputThread.INSTANCE.isAlive()) {
            this.deltaX = RawInputThread.INSTANCE.dx.getAndSet(0);
            this.deltaY = RawInputThread.INSTANCE.dy.getAndSet(0);

            boolean movement = deltaX != 0 || deltaY != 0;

            if (!((Math.abs(Mouse.getDX()) <= 5 && Math.abs(Mouse.getDY()) <= 5) || movement)) {
                if (fails++ > 5) {
                    RawInputThread.INSTANCE.rescan();
                }
            } else if (movement) {
                fails = 0;
            }
        } else {
            super.mouseXYChange();
        }
    }
}