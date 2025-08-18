package com.dew.system.module.modules.movement.flight.flies;

import com.dew.system.event.events.PreMotionEvent;
import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.event.events.ReceivedPacketEvent;
import com.dew.system.event.events.SendPacketEvent;
import com.dew.system.module.modules.movement.flight.FlightMode;
import com.dew.utils.MovementUtil;
import com.dew.utils.PacketUtil;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;

public class VerusFlight implements FlightMode {

    @Override
    public String getName() {
        return "Verus";
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
        if (mc.thePlayer != null) {
            MovementUtil.stopMovingSlowly();
            MovementUtil.stopYMotion();
        }
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        BlockPos downPos = mc.thePlayer.getPosition().add(0.0, -1.5, 0.0);
        PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(downPos, 1, new ItemStack(Blocks.stone.getItem(mc.theWorld, downPos)), 0.0F, 0.5F + ((float) Math.random()) * 0.44F, 0.0F));

        double yMotion = 0;
        if (mc.gameSettings.keyBindJump.isKeyDown())
            yMotion += 0.08;
        if (mc.gameSettings.keyBindSneak.isKeyDown())
            yMotion -= 0.08;

        mc.thePlayer.motionY = yMotion;

        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            MovementUtil.strafe(0.42f);
        } else {
            MovementUtil.strafe(0.32f);
        }
    }

    @Override
    public void onPreMotion(PreMotionEvent event) {
    }

    @Override
    public void onSendPacket(SendPacketEvent event) {
    }

    @Override
    public void onReceivedPacket(ReceivedPacketEvent event) {
    }
}
