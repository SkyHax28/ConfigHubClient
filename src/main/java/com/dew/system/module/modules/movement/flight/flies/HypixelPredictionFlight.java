package com.dew.system.module.modules.movement.flight.flies;

import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.event.events.ReceivedPacketEvent;
import com.dew.system.event.events.SendPacketEvent;
import com.dew.system.module.modules.movement.flight.FlightMode;
import com.dew.utils.LogUtil;
import com.dew.utils.MovementUtil;
import com.dew.utils.PacketUtil;
import com.dew.utils.TimerUtil;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0CPacketInput;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.status.server.S01PacketPong;

public class HypixelPredictionFlight implements FlightMode {
    @Override
    public String getName() {
        return "Hypixel Prediction";
    }

    private int ticks;

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
        if (mc.thePlayer == null) return;

        mc.thePlayer.capabilities.isFlying = false;
        TimerUtil.resetTimerSpeed();
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer == null) return;

        mc.thePlayer.capabilities.isFlying = true;
        mc.thePlayer.motionY = 0f;
        TimerUtil.setTimerSpeed(0.7f);
    }

    @Override
    public void onSendPacket(SendPacketEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        Packet<?> packet = event.packet;

        if (packet instanceof C0FPacketConfirmTransaction) {
            PacketUtil.sendPacketAsSilent(new C0FPacketConfirmTransaction(((C0FPacketConfirmTransaction) packet).getWindowId(), (short) Math.abs(((C0FPacketConfirmTransaction) packet).getUid()), false));
            event.cancel();
        }
    }

    @Override
    public void onReceivedPacket(ReceivedPacketEvent event) {
    }
}
