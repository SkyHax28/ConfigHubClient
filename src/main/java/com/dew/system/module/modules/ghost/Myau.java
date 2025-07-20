package com.dew.system.module.modules.ghost;

import com.dew.system.event.events.*;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import org.lwjgl.input.Keyboard;

public class Myau extends Module {

    public Myau() {
        super("Myau", ModuleCategory.GHOST, Keyboard.KEY_NONE, false, true, true);
    }

    @Override
    public void onEnable() {
        mc.shutdown();
    }

    @Override
    public void onDisable() {
        mc.shutdown();
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        mc.shutdown();
    }

    @Override
    public void onPreMotion(PreMotionEvent event) {
        mc.shutdown();
    }

    @Override
    public void onStrafe(StrafeEvent event) {
        mc.shutdown();
    }

    @Override
    public void onKeyboard(KeyboardEvent event) {
        mc.shutdown();
    }

    @Override
    public void onLivingUpdate(LivingUpdateEvent event) {
        mc.shutdown();
    }

    @Override
    public void onTick(TickEvent event) {
        mc.shutdown();
    }
}
