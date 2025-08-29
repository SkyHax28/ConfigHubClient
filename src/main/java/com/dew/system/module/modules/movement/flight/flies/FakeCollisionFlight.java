package com.dew.system.module.modules.movement.flight.flies;

import com.dew.system.event.events.*;
import com.dew.system.module.modules.movement.flight.FlightMode;
import net.minecraft.util.AxisAlignedBB;

public class FakeCollisionFlight implements FlightMode {
    @Override
    public String getName() {
        return "Fake Collision";
    }

    private Double yCoord = null;

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
        yCoord = null;
    }

    @Override
    public void onAttack(AttackEvent event) {
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer != null) {
            if (yCoord == null) {
                yCoord = (double) mc.thePlayer.getPosition().down().getY();
            }
        }
    }

    @Override
    public void onPreMotion(PreMotionEvent event) {
    }

    @Override
    public void onBlockBB(BlockBBEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null || yCoord == null) return;

        if (event.blockPos.getY() == yCoord) {
            int x = event.blockPos.getX();
            int y = event.blockPos.getY();
            int z = event.blockPos.getZ();
            event.boundingBox = new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1);
        }
    }

    @Override
    public void onSendPacket(SendPacketEvent event) {
    }

    @Override
    public void onReceivedPacket(ReceivedPacketEvent event) {
    }
}
