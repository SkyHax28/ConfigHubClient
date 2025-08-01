package com.dew.system.module.modules.render;

import com.dew.DewCommon;
import com.dew.system.event.events.PostMotionEvent;
import com.dew.system.event.events.WorldEvent;
import com.dew.system.event.events.WorldLoadEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.rotation.RotationManager;
import com.dew.system.settingsvalue.SelectionValue;
import org.lwjgl.input.Keyboard;

public class SilentView extends Module {

    public SilentView() {
        super("Silent View", ModuleCategory.RENDER, Keyboard.KEY_NONE, true, true, true);
    }

    public static final SelectionValue mode = new SelectionValue("Mode", "Normal", "Normal", "GameSense");

    private float prevHeadPitch = 0f;
    private float headPitch = 0f;

    public float getHeadPitch() {
        return headPitch;
    }

    public float getPrevHeadPitch() {
        return prevHeadPitch;
    }

    private void resetState() {
        prevHeadPitch = 0f;
        headPitch = 0f;
    }

    @Override
    public String tag() {
        return mode.get();
    }

    @Override
    public void onDisable() {
        this.resetState();
    }

    @Override
    public void onLoadWorld(WorldLoadEvent event) {
        this.resetState();
    }

    @Override
    public void onPostMotion(PostMotionEvent event) {
        RotationManager rotationManager = DewCommon.rotationManager;
        if (!rotationManager.isRotating()) {
            if (mc.thePlayer == null) {
                this.resetState();
            } else {
                prevHeadPitch = headPitch;
                headPitch = mc.thePlayer.rotationPitch;
            }
            return;
        }

        prevHeadPitch = headPitch;
        headPitch = rotationManager.getClientPitch();
    }
}
