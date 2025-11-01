package com.dew.utils;

import com.dew.IMinecraft;
import net.minecraft.block.BlockAir;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.stats.StatFileWriter;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraft.util.Vec3;

import java.util.LinkedList;

public class PredictUtil {

    private static final Minecraft mc = IMinecraft.mc;
    public static boolean predicting = false;
    public static boolean isSp = false;

    private static void togglePredictingState(EntityPlayer player, boolean state) {
        predicting = state;
        if (player instanceof EntityPlayerSP) {
            isSp = state;
        }
    }

    public static LinkedList<Vec3> predict(EntityPlayer player, int ticks) {
        togglePredictingState(player, true);
        LinkedList<Vec3> positions = new LinkedList<>();

        EntityOtherPlayerMP clone = new EntityOtherPlayerMP(mc.theWorld, player.getGameProfile());
        clone.copyLocationAndAnglesFrom(player);
        clone.motionX = player.motionX;
        clone.motionY = player.motionY;
        clone.motionZ = player.motionZ;
        clone.setSprinting(player.isSprinting());
        clone.setSneaking(player.isSneaking());
        clone.onGround = player.onGround;

        float forward = player.moveForward;
        float strafe = player.moveStrafing;

        for (int i = 0; i < ticks; i++) {
            clone.moveEntityWithHeading(strafe, forward);

            if (!clone.onGround) {
                clone.motionY -= 0.08;
                clone.motionY *= 0.98;
            }

            positions.add(new Vec3(clone.posX, clone.posY, clone.posZ));
        }

        togglePredictingState(player, false);
        return positions;
    }

    public static EntityPlayer predictFinalState(EntityPlayer player, int ticks) {
        togglePredictingState(player, true);

        EntityOtherPlayerMP clone = new EntityOtherPlayerMP(mc.theWorld, player.getGameProfile());
        clone.copyLocationAndAnglesFrom(player);
        clone.motionX = player.motionX;
        clone.motionY = player.motionY;
        clone.motionZ = player.motionZ;
        clone.setSprinting(player.isSprinting());
        clone.setSneaking(player.isSneaking());
        clone.onGround = player.onGround;

        float forward = player.moveForward;
        float strafe = player.moveStrafing;

        for (int i = 0; i < ticks; i++) {
            clone.moveEntityWithHeading(strafe, forward);

            if (!clone.onGround) {
                clone.motionY -= 0.08;
                clone.motionY *= 0.98;
            }
        }

        EntityOtherPlayerMP finalPlayer = new EntityOtherPlayerMP(mc.theWorld, player.getGameProfile());
        finalPlayer.copyLocationAndAnglesFrom(clone);
        finalPlayer.motionX = clone.motionX;
        finalPlayer.motionY = clone.motionY;
        finalPlayer.motionZ = clone.motionZ;
        finalPlayer.onGround = clone.onGround;
        finalPlayer.setSprinting(clone.isSprinting());
        finalPlayer.setSneaking(clone.isSneaking());

        togglePredictingState(player, false);
        return finalPlayer;
    }

    public static EntityPlayer predictOthers(Entity target, boolean predict, float predictSize) {
        EntityPlayerSP player = mc.thePlayer;

        double posX = target.posX + (predict ? (target.posX - target.prevPosX) * predictSize : 0.0);
        double posY = target.posY + (predict ? (target.posY - target.prevPosY) * predictSize : 0.0);
        double posZ = target.posZ + (predict ? (target.posZ - target.prevPosZ) * predictSize : 0.0);

        double motionX = predict ? (target.posX - target.prevPosX) : target.motionX;
        double motionY = predict ? (target.posY - target.prevPosY) : target.motionY;
        double motionZ = predict ? (target.posZ - target.prevPosZ) : target.motionZ;

        EntityOtherPlayerMP predictedPlayer = new EntityOtherPlayerMP(mc.theWorld,
                target instanceof EntityPlayer ? ((EntityPlayer) target).getGameProfile() : player.getGameProfile());

        predictedPlayer.setPosition(posX, posY, posZ);
        predictedPlayer.motionX = motionX;
        predictedPlayer.motionY = motionY;
        predictedPlayer.motionZ = motionZ;

        if (target instanceof EntityPlayer) {
            EntityPlayer targetPlayer = (EntityPlayer) target;
            predictedPlayer.setSprinting(targetPlayer.isSprinting());
            predictedPlayer.setSneaking(targetPlayer.isSneaking());
            predictedPlayer.onGround = targetPlayer.onGround;
        }

        return predictedPlayer;
    }
}
