package com.dew.system.module.modules.combat;

import com.dew.DewCommon;
import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.utils.LogUtil;
import com.dew.utils.PredictUtil;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemHoe;
import net.minecraft.util.BlockPos;
import org.lwjgl.input.Keyboard;

import java.util.*;

public class AutoAimPractice extends Module {

    public AutoAimPractice() {
        super("Auto Aim Practice", ModuleCategory.COMBAT, Keyboard.KEY_NONE, false, true, true);
    }

    private final Map<BlockPos, Integer> recentShots = new HashMap<>();

    @Override
    public void onDisable() {
        recentShots.clear();
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        Iterator<Map.Entry<BlockPos, Integer>> it = recentShots.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<BlockPos, Integer> entry = it.next();
            int remaining = entry.getValue() - 1;
            if (remaining <= 0) {
                it.remove();
            } else {
                entry.setValue(remaining);
            }
        }

        BlockPos targetPos = findNearestWhiteWool(20);
        if (targetPos != null && DewCommon.rotationManager.faceBlock(targetPos, 180f, true, 30)) {
            mc.clickMouse();
            recentShots.put(targetPos, 20);
        };
    }

    private BlockPos findNearestWhiteWool(int searchRadius) {
        BlockPos playerPos = mc.thePlayer.getPosition();
        BlockPos nearestPos = null;
        double nearestDistanceSq = Double.MAX_VALUE;

        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int y = -searchRadius; y <= searchRadius; y++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    BlockPos currentPos = playerPos.add(x, y, z);
                    if (recentShots.containsKey(currentPos)) continue;
                    if (mc.theWorld.getBlockState(currentPos).getBlock() == Blocks.wool && mc.theWorld.getBlockState(currentPos).getBlock().getMetaFromState(mc.theWorld.getBlockState(currentPos)) == 0) {
                        double distSq = mc.thePlayer.getDistanceSq(currentPos);
                        if (distSq < nearestDistanceSq) {
                            nearestDistanceSq = distSq;
                            nearestPos = currentPos;
                        }
                    }
                }
            }
        }

        return nearestPos;
    }
}
