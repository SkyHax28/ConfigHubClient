package com.dew.system.module.modules.render;

import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.event.events.WorldLoadEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import org.lwjgl.input.Keyboard;

public class FreeLook extends Module {
    private float cameraYaw, cameraPitch;
    private int prevThirdPersonView;
    private boolean enableLook = false;
    public FreeLook() {
        super("Free Look", ModuleCategory.RENDER, Keyboard.KEY_F9, false, true, true);
    }

    @Override
    public void onDisable() {
        enableLook = false;
        mc.gameSettings.thirdPersonView = prevThirdPersonView;
    }

    @Override
    public void onLoadWorld(WorldLoadEvent event) {
        enableLook = false;
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer != null && !enableLook) {
            prevThirdPersonView = mc.gameSettings.thirdPersonView;
            mc.gameSettings.thirdPersonView = 1;
            cameraYaw = mc.thePlayer.rotationYaw;
            cameraPitch = mc.thePlayer.rotationPitch;
            enableLook = true;
        }
    }

    public boolean freeLooked() {
        return enableLook;
    }

    public float getCameraYaw() {
        return cameraYaw;
    }

    public void setCameraYaw(float cameraYaw) {
        this.cameraYaw = cameraYaw;
    }

    public float getCameraPitch() {
        return cameraPitch;
    }

    public void setCameraPitch(float cameraPitch) {
        this.cameraPitch = cameraPitch;
    }
}
