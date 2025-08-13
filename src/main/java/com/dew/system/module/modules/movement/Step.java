package com.dew.system.module.modules.movement;

import com.dew.system.event.events.PreMotionEvent;
import com.dew.system.event.events.WorldLoadEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.NumberValue;
import com.dew.system.settingsvalue.SelectionValue;
import com.dew.utils.MovementUtil;
import com.dew.utils.PacketUtil;
import com.dew.utils.TimerUtil;
import net.minecraft.network.play.client.C03PacketPlayer;
import org.lwjgl.input.Keyboard;

public class Step extends Module {
    private static final SelectionValue mode = new SelectionValue("Mode", "Normal", "Normal", "NCP");
    private static final NumberValue height = new NumberValue("Height", 1.5, 1.0, 10.0, 0.5);
    private static final NumberValue timerSpeed = new NumberValue("Timer Speed", 0.7, 0.1, 2.0, 0.1);
    private int ticksSinceLastStep = -1;

    public Step() {
        super("Step", ModuleCategory.MOVEMENT, Keyboard.KEY_NONE, false, true, true);
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

    private void resetState() {
        if (mc.thePlayer != null) {
            mc.thePlayer.stepHeight = 0.6f;
        }

        if (ticksSinceLastStep != -1) {
            TimerUtil.resetTimerSpeed();
            ticksSinceLastStep = -1;
        }
    }

    @Override
    public void onPreMotion(PreMotionEvent event) {
        if (mc.thePlayer == null) return;

        mc.thePlayer.stepHeight = height.get().floatValue();

        if (ticksSinceLastStep >= 0) {
            ticksSinceLastStep++;
        }

        if (ticksSinceLastStep > 1) {
            TimerUtil.resetTimerSpeed();
            ticksSinceLastStep = -1;
        }
    }

    public void onStep() {
        if (mc.thePlayer == null) return;

        final double steppingHeight = mc.thePlayer.getEntityBoundingBox().minY - mc.thePlayer.posY;

        if (steppingHeight <= 0.6f || ticksSinceLastStep >= 0) return;

        MovementUtil.fakeJump();

        if (mode.get().equals("NCP")) {
            PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.41999998688698, mc.thePlayer.posZ, false));
            PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.7531999805212, mc.thePlayer.posZ, false));
        }

        TimerUtil.setTimerSpeed(timerSpeed.get().floatValue());
        ticksSinceLastStep = 0;
    }
}
