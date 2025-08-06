package com.dew.system.module.modules.movement;

import com.dew.DewCommon;
import com.dew.system.event.events.PreMotionEvent;
import com.dew.system.event.events.StrafeEvent;
import com.dew.system.event.events.WorldLoadEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.rotation.RotationManager;
import com.dew.system.settingsvalue.SelectionValue;
import com.dew.utils.BlinkUtil;
import com.dew.utils.LogUtil;
import com.dew.utils.MovementUtil;
import com.dew.utils.TimerUtil;
import net.minecraft.client.settings.GameSettings;
import org.lwjgl.input.Keyboard;

public class Spider extends Module {

    private static final SelectionValue mode = new SelectionValue("Mode", "Prediction Infinite", "Prediction Infinite");
    private int hypTick = -1;
    public Spider() {
        super("Spider", ModuleCategory.MOVEMENT, Keyboard.KEY_NONE, false, true, true);
    }

    @Override
    public String tag() {
        return mode.get();
    }

    @Override
    public void onEnable() {
        if (mode.get().equals("Prediction Infinite")) {
            LogUtil.printChat("If you hold the jump key near a wall, you can climb up");
        }
    }

    @Override
    public void onDisable() {
        BlinkUtil.sync(true, true);
        BlinkUtil.stopBlink();
        TimerUtil.resetTimerSpeed();
        mc.gameSettings.keyBindJump.setKeyDown(Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()));
        hypTick = -1;
    }

    @Override
    public void onLoadWorld(WorldLoadEvent event) {
        BlinkUtil.sync(true, true);
        BlinkUtil.stopBlink();
        TimerUtil.resetTimerSpeed();
        mc.gameSettings.keyBindJump.setKeyDown(Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()));
        hypTick = -1;
    }

    @Override
    public void onPreMotion(PreMotionEvent event) {
        if (mc.thePlayer == null) return;

        if (mode.get().equals("Prediction Infinite")) {
            if (MovementUtil.isBlockUnderPlayer(mc.thePlayer, 1, false) && mc.gameSettings.keyBindJump.isKeyDown()) {
                if (hypTick >= 3) {
                    BlinkUtil.sync(true, true);
                    BlinkUtil.stopBlink();
                    TimerUtil.resetTimerSpeed();
                    hypTick--;
                } else {
                    BlinkUtil.doBlink();
                    TimerUtil.setTimerSpeed(0.3f);
                    mc.thePlayer.motionY = 0.0;
                    MovementUtil.fakeJump();
                    event.onGround = true;
                    mc.thePlayer.onGround = true;
                    hypTick++;
                }
            } else {
                BlinkUtil.sync(true, true);
                BlinkUtil.stopBlink();
                TimerUtil.resetTimerSpeed();
                hypTick = -1;
            }
        }
    }
}
