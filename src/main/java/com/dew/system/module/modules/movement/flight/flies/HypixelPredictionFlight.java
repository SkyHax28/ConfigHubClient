package com.dew.system.module.modules.movement.flight.flies;

import com.dew.system.event.events.PreMotionEvent;
import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.event.events.ReceivedPacketEvent;
import com.dew.system.event.events.SendPacketEvent;
import com.dew.system.module.modules.movement.flight.FlightMode;
import com.dew.utils.BlinkUtil;
import com.dew.utils.LogUtil;
import com.dew.utils.PacketUtil;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.BlockPos;

public class HypixelPredictionFlight implements FlightMode {
    private int boostSpeed = 0;

    @Override
    public String getName() {
        return "Hypixel Prediction";
    }

    @Override
    public void onEnable() {
        BlinkUtil.doBlink();
    }

    @Override
    public void onDisable() {
        boostSpeed = 0;
        BlinkUtil.sync(true, true);
        BlinkUtil.stopBlink();
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
    }

    @Override
    public void onPreMotion(PreMotionEvent event) {
        if (mc.thePlayer == null) return;

        boostSpeed++;

        BlockPos pos = mc.thePlayer.getPosition().add(0, -1.5, 0);

        ItemStack stack = new ItemStack(Blocks.stone.getItem(mc.theWorld, pos));

        float hitX = 0.0F;
        float hitY = 0.5F + (float) Math.random() * 0.44F;
        float hitZ = 0.0F;

        mc.thePlayer.motionY = -1.0;

        PacketUtil.sendPacket(
                new C08PacketPlayerBlockPlacement(
                        pos,
                        1,
                        stack,
                        hitX,
                        hitY,
                        hitZ
                )
        );

        event.onGround = false;

        mc.thePlayer.onGround = false;
    }

    @Override
    public void onSendPacket(SendPacketEvent event) {
    }

    @Override
    public void onReceivedPacket(ReceivedPacketEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        Packet<?> packet = event.packet;

        if (packet instanceof S08PacketPlayerPosLook) {
            LogUtil.printChat("Crym");
        }
    }
}
