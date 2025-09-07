package com.dew.system.module.modules.combat;

import com.dew.DewCommon;
import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.event.events.ReceivedPacketEvent;
import com.dew.system.event.events.WorldLoadEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.utils.*;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemSword;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
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
        EntityPlayer predictedEntityPlayer = PredictUtil.predictFinalState(closestTarget, 6);

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
