package com.dew.system.module.modules.movement;

import com.dew.DewCommon;
import com.dew.system.event.events.PreMotionEvent;
import com.dew.system.event.events.ReceivedPacketEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.NumberValue;
import com.dew.utils.LogUtil;
import com.dew.utils.MovementUtil;
import com.dew.utils.PacketUtil;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import org.lwjgl.input.Keyboard;

public class LongJump extends Module {
    private static final NumberValue boostMotion = new NumberValue("Boost", 1.6, 1.5, 1.75, 0.05);
    private int tick, lastSlot, motionTick;
    private double startMotion;
    private boolean boost, sent;

    public LongJump() {
        super("Long Jump", ModuleCategory.MOVEMENT,  Keyboard.KEY_NONE, false, true, true);
    }

    @Override
    public void onDisable() {
        tick = 0;
        motionTick = 0;
        boost = false;
        sent = false;
    }

    @Override
    public void onEnable() {
        lastSlot = mc.thePlayer.inventory.currentItem;
        if (getBall() == -1 && this.isEnabled()) {
            LogUtil.printChat("No fireball are found in the hotbar.");
            this.toggleState();
            this.onDisable();
        }
    }

    @Override
    public void onPreMotion(PreMotionEvent event) {
        EntityPlayerSP player = mc.thePlayer;

        if (tick == 0) {
            int ballSlot = getBall();
            if (ballSlot != -1) {
                player.inventory.currentItem = ballSlot;
                PacketUtil.sendPacket(new C09PacketHeldItemChange(ballSlot));
            }
            DewCommon.rotationManager.rotateToward(mc.thePlayer.rotationYaw, 90f, 180f);
            tick++;
        } else if (tick == 1) {
            DewCommon.rotationManager.rotateToward(mc.thePlayer.rotationYaw, 90f, 180f);
            PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(player.inventory.getCurrentItem()));
            sent = true;
            tick++;
        } else if (tick == 2) {
            player.inventory.currentItem = lastSlot;
            PacketUtil.sendPacket(new C09PacketHeldItemChange(lastSlot));
            tick++;
        }

        if (boost) {
            if (MovementUtil.isMoving() && MovementUtil.getSpeed() > 0.25) {
                MovementUtil.setSpeed(boostMotion.get());
            }
            startMotion = player.motionY;
            boost = false;
            motionTick = 1;
        }

        if (motionTick >= 1) {
            motionTick++;
            if (motionTick < 30) {
                player.motionY += 0.0275D;
            }
            if ((motionTick == 30 || motionTick == 31) && startMotion > 0.9) {
                player.motionY = 0.027D;
                if (motionTick == 31) {
                    this.toggleState();
                }
            }
            MovementUtil.setSpeed(MovementUtil.getSpeed());
        }
    }

    @Override
    public void onReceivedPacket(ReceivedPacketEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        Packet<?> packet = event.packet;

        if (packet instanceof S12PacketEntityVelocity && mc.theWorld.getEntityByID(((S12PacketEntityVelocity) packet).getEntityID()) == mc.thePlayer && sent) {
            LogUtil.printChat("Boosting");
            boost = true;
            sent = false;
        }
    }

    private int getBall() {
        int ballSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() == Items.fire_charge) {
                ballSlot = i;
                break;
            }
        }
        return ballSlot;
    }
}
