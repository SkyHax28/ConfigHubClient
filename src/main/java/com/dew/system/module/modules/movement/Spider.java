package com.dew.system.module.modules.movement;

import com.dew.system.event.events.PreMotionEvent;
import com.dew.system.event.events.WorldLoadEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.SelectionValue;
import com.dew.utils.*;
import net.minecraft.network.play.client.C03PacketPlayer;
import org.lwjgl.input.Keyboard;

public class Spider extends Module {

    private static final SelectionValue mode = new SelectionValue("Mode", "Prediction Infinite", "Prediction Infinite");
    private final Clock hypTimer = new Clock();

    public Spider() {
        super("Spider", ModuleCategory.MOVEMENT, Keyboard.KEY_NONE, false, true, true);
    }

    public boolean ignoreJumpDelay() {
        return this.isEnabled() && mode.get().equals("Prediction Infinite");
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
        hypTimer.reset();
    }

    @Override
    public void onLoadWorld(WorldLoadEvent event) {
        BlinkUtil.sync(true, true);
        BlinkUtil.stopBlink();
        TimerUtil.resetTimerSpeed();
        hypTimer.reset();
    }

    @Override
    public void onPreMotion(PreMotionEvent event) {
        if (mc.thePlayer == null) return;

        if (mode.get().equals("Prediction Infinite")) {
            if ((MovementUtil.isBlockUnderPlayer(mc.thePlayer, 2, 0.5, false) || mc.thePlayer.isCollidedHorizontally) && mc.gameSettings.keyBindJump.isKeyDown()) {
                event.forceC06 = true;
                if (hypTimer.hasTimePassed(300)) {
                    BlinkUtil.doBlink();
                    TimerUtil.setTimerSpeed(0.3f);
                    PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + (0.41999998688698 + Math.random() * 0.003), mc.thePlayer.posZ, false));
                    MovementUtil.fakeJump();
                    event.onGround = true;
                    mc.thePlayer.onGround = true;
                    hypTimer.reset();
                } else {
                    BlinkUtil.sync(true, true);
                    BlinkUtil.stopBlink();
                    event.onGround = false;
                    mc.thePlayer.onGround = false;
                    TimerUtil.resetTimerSpeed();
                }
            } else {
                BlinkUtil.sync(true, true);
                BlinkUtil.stopBlink();
                TimerUtil.resetTimerSpeed();
                hypTimer.reset();
            }
        }
    }
}
