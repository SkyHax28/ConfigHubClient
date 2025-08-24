package com.dew.system.module.modules.movement.speed.speeds;

import com.dew.DewCommon;
import com.dew.system.event.events.*;
import com.dew.system.module.modules.combat.Aura;
import com.dew.system.module.modules.movement.flight.FlightModule;
import com.dew.system.module.modules.movement.speed.SpeedMode;
import com.dew.system.module.modules.movement.speed.SpeedModule;
import com.dew.utils.MovementUtil;
import com.dew.utils.PacketUtil;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockIce;
import net.minecraft.block.BlockPackedIce;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

public class VerusSpeed implements SpeedMode {
    @Override
    public String getName() {
        return "Verus";
    }

    private int allowTick = 0;

    @Override
    public void onEnable() {
        MovementUtil.mcJumpNoBoost = true;
    }

    @Override
    public void onDisable() {
        MovementUtil.mcJumpNoBoost = false;
        if (mc.thePlayer != null) {
            MovementUtil.stopMovingSlowly();
        }
    }

    @Override
    public void onAttack(AttackEvent event) {
        if (mc.thePlayer.onGround && MovementUtil.hasMotionHorizontal()) {
            event.cancel();
        }
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
    }

    @Override
    public void onPreMotion(PreMotionEvent event) {
        if (mc.thePlayer == null || mc.thePlayer.isInWater() || DewCommon.moduleManager.getModule(FlightModule.class).isEnabled()) return;

        if (!mc.thePlayer.onGround && allowTick <= 0) {
            MovementUtil.strafe(0.3f);
        }
    }

    @Override
    public void onMove(MoveEvent event) {
        if (mc.thePlayer == null || mc.thePlayer.isInWater() || DewCommon.moduleManager.getModule(FlightModule.class).isEnabled()) return;

        MovementUtil.mcJumpNoBoost = true;

        if (!mc.thePlayer.onGround) {
            if (allowTick > 0) {
                allowTick--;
                if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                    if (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() == 0) {
                        MovementUtil.strafe(0.6f);
                    } else {
                        MovementUtil.strafe(0.67f);
                    }
                } else {
                    MovementUtil.strafe(0.52f);
                }
            }
        } else if (MovementUtil.isMoving()) {
            BlockPos downPos = mc.thePlayer.getPosition().add(0.0, -1.5, 0.0);
            PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(downPos, 1, new ItemStack(Blocks.stone.getItem(mc.theWorld, downPos)), 0.0F, 0.5F + ((float) Math.random()) * 0.44F, 0.0F));

            allowTick = 1;
            mc.thePlayer.jump();
            mc.thePlayer.motionY = -0.9;
            if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                if (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() == 0) {
                    MovementUtil.strafe(0.92f);
                } else {
                    MovementUtil.strafe(1.19f);
                }
            } else {
                MovementUtil.strafe(0.69F);
            }
            event.y = 0.0;
        }
    }

    @Override
    public void onBlockBB(BlockBBEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (event.blockPos.getY() == mc.thePlayer.getPosition().down().getY() && (mc.theWorld.getBlockState(mc.thePlayer.getPosition().down()).getBlock() instanceof BlockIce || mc.theWorld.getBlockState(mc.thePlayer.getPosition().down()).getBlock() instanceof BlockPackedIce)) {
            int x = event.blockPos.getX();
            int y = event.blockPos.getY();
            int z = event.blockPos.getZ();
            event.boundingBox = new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1);
        }
    }

    @Override
    public void onReceivedPacket(ReceivedPacketEvent event) {
    }
}