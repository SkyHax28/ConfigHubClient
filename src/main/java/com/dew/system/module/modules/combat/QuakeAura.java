package com.dew.system.module.modules.combat;

import com.dew.DewCommon;
import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.utils.*;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemHoe;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class QuakeAura extends Module {

    public QuakeAura() {
        super("Quake Aura", ModuleCategory.COMBAT, Keyboard.KEY_NONE, false, true, true);
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer == null || mc.thePlayer.getHeldItem() == null || !(mc.thePlayer.getHeldItem().getItem() instanceof ItemHoe)) return;

        List<EntityPlayer> targets = this.getSortedTargetList();

        if (targets.isEmpty()) return;
        EntityPlayer closestTarget = targets.get(0);
        EntityPlayer predictedEntityPlayer = PredictUtil.predictOthers(closestTarget, true, 2f);

        if (DewCommon.rotationManager.faceEntity(predictedEntityPlayer, mc.thePlayer.onGround ? 180f : 45f, true, false, 200)) {
            if (mc.thePlayer.canEntityBeSeen(predictedEntityPlayer)) {
                mc.rightClickMouse();
                LogUtil.printChat("shot");
            }
        };
    }

    private List<EntityPlayer> getSortedTargetList() {
        List<EntityPlayer> targets = new ArrayList<>();

        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (player instanceof EntityPlayerSP) continue;
            targets.add(player);
        }

        targets.sort(Comparator.comparingDouble(e -> mc.thePlayer.getDistanceToEntity(e)));
        return targets;
    }
}
