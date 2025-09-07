package com.dew.system.module.modules.movement.flight.flies;

import com.dew.system.event.events.*;
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
    }

    @Override
    public void onAttack(AttackEvent event) {
        event.cancel();
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        boolean aboveMoment = !MovementUtil.isBlockAbovePlayer(mc.thePlayer, 1, 0.6);

        if (mc.gameSettings.keyBindJump.isKeyDown() && aboveMoment) {
            if (mc.thePlayer.ticksExisted % 2 == 0) {
                mc.thePlayer.motionY = 0.41999998688698;
            }
            return;
        }

        PacketUtil.sendVerusMagicPacket();

        double yMotion = 0;
        if (mc.gameSettings.keyBindSneak.isKeyDown())
            yMotion -= 0.08;

        mc.thePlayer.motionY = yMotion;
        if (yMotion == 0) {
            mc.thePlayer.onGround = aboveMoment;
        }
    }

    @Override
    public void onPreMotion(PreMotionEvent event) {
    }

    @Override
    public void onBlockBB(BlockBBEvent event) {
    }

    @Override
    public void onSendPacket(SendPacketEvent event) {
    }

    @Override
    public void onReceivedPacket(ReceivedPacketEvent event) {
    }
}
