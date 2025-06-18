package com.dew.system.module.modules.movement.speed.speeds;

import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.module.modules.movement.speed.SpeedMode;
import com.dew.utils.MovementUtil;
import com.dew.utils.PacketUtil;
import com.dew.utils.TimerUtil;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.client.C18PacketSpectate;

public class HypixelPredictionSpeed implements SpeedMode {
    @Override
    public String getName() {
        return "Hypixel Prediction";
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
        MovementUtil.mcJumpNoBoost = false;
        TimerUtil.resetTimerSpeed();
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer == null || mc.thePlayer.isInWater()) return;

        if (mc.thePlayer.onGround && mc.thePlayer.posY > 0.0D) {
            mc.thePlayer.jump();
        }
    }
}