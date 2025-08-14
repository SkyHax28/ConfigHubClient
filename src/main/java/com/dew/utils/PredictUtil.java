package com.dew.utils;

import com.dew.IMinecraft;
import net.minecraft.block.BlockAir;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
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
}
