package com.dew.system.viapatcher;

import com.dew.IMinecraft;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import net.minecraft.client.Minecraft;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

public class MovePatcher {

    private static final Minecraft mc = IMinecraft.mc;

    public static double handlePosition() {
        return !mc.isSingleplayer() && ViaLoadingBase.getInstance().getTargetVersion().newerThan(ProtocolVersion.v1_8) ? 0.003D : 0.005D;
    }

    public static float handleLadder() {
        return !mc.isSingleplayer() && ViaLoadingBase.getInstance().getTargetVersion().newerThan(ProtocolVersion.v1_8) ? 0.1875f : 0.125f;
    }

    public static AxisAlignedBB handleLilyPad(BlockPos pos) {
        if (!mc.isSingleplayer() && ViaLoadingBase.getInstance().getTargetVersion().newerThan(ProtocolVersion.v1_8))
            return new AxisAlignedBB((double) pos.getX() + 0.0625D, (double) pos.getY() + 0.0D, (double) pos.getZ() + 0.0625D, (double) pos.getX() + 0.9375D, (double) pos.getY() + 0.09375D, (double) pos.getZ() + 0.9375D);

        return null;
    }

    public static float handleHitBoxSize() {
        return !IMinecraft.mc.isSingleplayer() && ViaLoadingBase.getInstance().getTargetVersion().newerThan(ProtocolVersion.v1_8) ? 0f : 0.1f;
    }
}
