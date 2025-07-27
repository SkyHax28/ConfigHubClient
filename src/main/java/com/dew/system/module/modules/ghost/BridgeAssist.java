package com.dew.system.module.modules.ghost;

import com.dew.DewCommon;
import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.event.events.TickEvent;
import com.dew.system.event.events.WorldEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.module.modules.player.Scaffold;
import com.dew.system.settingsvalue.NumberValue;
import com.dew.utils.LogUtil;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import org.lwjgl.input.Keyboard;

public class BridgeAssist extends Module {

    public BridgeAssist() {
        super("Bridge Assist", ModuleCategory.GHOST, Keyboard.KEY_NONE, false, true, true);
    }

    private static final NumberValue maxSneakTime = new NumberValue("Max Sneak Time", 1.0, 1.0, 5.0, 1.0);

    private int resetTick = 0;
    private int bridTick = 0;
    private boolean bridging = false;

    public boolean isBridging() {
        return this.bridging;
    }

    @Override
    public void onDisable() {
        this.resetState();
    }

    @Override
    public void onWorld(WorldEvent event) {
        this.resetState();
    }

    private void resetState() {
        resetTick = 0;
        bridTick = 0;
        bridging = false;
        mc.gameSettings.keyBindSneak.setKeyDown(false);
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer == null) return;

        if (bridTick >= 1 && resetTick == 0) {
            bridTick++;
            if (bridTick >= 7) {
                bridging = false;
                bridTick = 0;
            }
        }

        if (GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)) return;

        if (DewCommon.moduleManager.getModule(Scaffold.class).isNearEdge() && mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemBlock && mc.thePlayer.rotationPitch >= 65f) {
            mc.gameSettings.keyBindSneak.setKeyDown(true);
            mc.rightClickDelayTimer = 0;
            bridging = true;
            bridTick = -1;
        } else {
            if (resetTick >= maxSneakTime.get().intValue()) {
                mc.gameSettings.keyBindSneak.setKeyDown(false);
                bridTick = 1;
                resetTick = 0;
            } else if (bridTick == -1) {
                resetTick++;
            }
        }
    }
}
