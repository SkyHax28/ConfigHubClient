package com.dew.system.module.modules.render;

import com.dew.DewCommon;
import com.dew.system.event.events.Render3DEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import org.lwjgl.input.Keyboard;

public class Wings extends Module {

    public Wings() {
        super("Wings", ModuleCategory.RENDER, Keyboard.KEY_NONE, false, false, true);
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (mc.gameSettings.thirdPersonView == 0 || mc.thePlayer == null || mc.theWorld == null) return;
        DewCommon.wingsManager.renderWings(event.partialTicks);
    }
}