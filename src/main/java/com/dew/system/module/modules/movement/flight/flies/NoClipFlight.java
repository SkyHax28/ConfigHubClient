package com.dew.system.module.modules.movement.flight.flies;

import com.dew.DewCommon;
import com.dew.system.event.events.*;
import com.dew.system.module.modules.movement.flight.FlightMode;
import com.dew.system.module.modules.movement.flight.FlightModule;
import com.dew.utils.MovementUtil;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;

public class NoClipFlight implements FlightMode {
    @Override
    public String getName() {
        return "No Clip";
    }

    @Override
    public void onEnable() {
        mc.renderGlobal.loadRenderers();
    }

    @Override
    public void onDisable() {
        mc.renderGlobal.loadRenderers();
        if (mc.thePlayer != null) {
            MovementUtil.stopMovingSlowly();
            MovementUtil.stopYMotion();
        }
    }

    @Override
    public void onAttack(AttackEvent event) {
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer == null) return;

        double yMotion = 0;
        if (mc.gameSettings.keyBindJump.isKeyDown())
            yMotion += DewCommon.moduleManager.getModule(FlightModule.class).getVerticalSpeed();
        if (mc.gameSettings.keyBindSneak.isKeyDown())
            yMotion -= DewCommon.moduleManager.getModule(FlightModule.class).getVerticalSpeed();

        MovementUtil.strafe(DewCommon.moduleManager.getModule(FlightModule.class).getHorizontalSpeed());
        mc.thePlayer.motionY = yMotion;
        mc.thePlayer.onGround = false;
        mc.thePlayer.noClip = true;
    }

    @Override
    public void onPreMotion(PreMotionEvent event) {
    }

    @Override
    public void onBlockBB(BlockBBEvent event) {
        if (mc.thePlayer == null) return;

        event.boundingBox = new AxisAlignedBB(0, 0, 0, 0, 0, 0);
    }

    @Override
    public void onSendPacket(SendPacketEvent event) {
    }

    @Override
    public void onReceivedPacket(ReceivedPacketEvent event) {
    }
}
