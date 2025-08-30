package com.dew.system.module.modules.movement;

import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.SelectionValue;
import com.dew.utils.*;
import org.lwjgl.input.Keyboard;

public class Phase extends Module {

    private static final SelectionValue mode = new SelectionValue("Mode", "Vclip", "Vclip");
    public Phase() {
        super("Phase", ModuleCategory.MOVEMENT, Keyboard.KEY_NONE, false, true, true);
    }

    @Override
    public String tag() {
        return mode.get();
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer == null) return;

        if (mode.get().equals("Vclip")) {
            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY - 3, mc.thePlayer.posZ);
            this.setState(false);
        }
    }
}
