package com.dew.system.module.modules.test;

import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.utils.Clock;
import com.dew.utils.PacketUtil;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.network.play.client.C01PacketChatMessage;
import org.lwjgl.input.Keyboard;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class CapeStealer extends Module {

    public CapeStealer() {
        super("Cape Stealer", ModuleCategory.EXPLOIT, Keyboard.KEY_NONE, false, true, true);
    }

    private final Clock clock = new Clock();

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        List<EntityPlayer> havingCapePlayerList = mc.theWorld.playerEntities.stream().filter(entity -> entity.isWearing(EnumPlayerModelParts.CAPE) && !(entity instanceof EntityPlayerSP)).sorted(Comparator.comparingDouble((EntityPlayer entity) -> mc.thePlayer.getDistanceToEntity(entity))).collect(Collectors.toList());

        if (clock.hasTimePassed(1000)) {
            PacketUtil.sendPacket(new C01PacketChatMessage("/tell AH0 give me your cape " + new Random().nextFloat()));
            clock.reset();
        }
    }
}