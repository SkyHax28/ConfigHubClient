package com.dew.system.module.modules.movement.speed.speeds;

import com.dew.DewCommon;
import com.dew.system.event.events.*;
import com.dew.system.module.modules.exploit.Disabler;
import com.dew.system.module.modules.movement.speed.SpeedMode;
import com.dew.system.module.modules.movement.speed.SpeedModule;
import com.dew.system.module.modules.player.Scaffold;
import com.dew.utils.MovementUtil;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.potion.Potion;

public class LatestNCPSpeed implements SpeedMode {
    private int ticks = 0;
    private boolean allowLow = false;

    @Override
    public String getName() {
        return "Latest NCP";
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
        MovementUtil.mcJumpNoBoost = false;
        this.resetState();
        if (mc.thePlayer != null) {
            MovementUtil.stopMovingSlowly();
        }
    }

    @Override
    public void onAttack(AttackEvent event) {
    }

    private void resetState() {
        ticks = 0;
        allowLow = false;
    }

    @Override
    public void onPreMotion(PreMotionEvent event) {
    }

    @Override
    public void onMove(MoveEvent event) {
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer == null) return;

        MovementUtil.mcJumpNoBoost = true;

        ticks = mc.thePlayer.onGround ? 0 : ticks + 1;

        if (mc.thePlayer.onGround) {
            if (MovementUtil.isMoving()) {
                mc.thePlayer.jump();
                MovementUtil.strafe((float) Math.max(0.48f + MovementUtil.getSpeedEffect() * 0.1, MovementUtil.getBaseMoveSpeed(0.2773)));
                allowLow = true;
            }
        } else {
            MovementUtil.strafe(MovementUtil.getSpeed());
            if (allowLow) {
                switch (ticks) {
                    case 1:
                        mc.thePlayer.motionY += 0.0568;
                        break;

                    case 3:
                        mc.thePlayer.motionY -= 0.13;
                        break;

                    case 4:
                        mc.thePlayer.motionY -= 0.2;
                        break;
                }
            }
        }
    }

    @Override
    public void onBlockBB(BlockBBEvent event) {
    }

    @Override
    public void onReceivedPacket(ReceivedPacketEvent event) {
        if (mc.thePlayer == null) return;

        Packet<?> packet = event.packet;

        if (packet instanceof S08PacketPlayerPosLook) {
            DewCommon.moduleManager.getModule(SpeedModule.class).setState(false);
        }
    }
}
