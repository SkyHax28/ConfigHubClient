package com.dew.system.event.events;

import com.dew.IMinecraft;
import com.dew.system.event.EventArgument;
import com.dew.system.event.EventListener;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

import java.util.Objects;

public class AttackEvent extends EventArgument {

    public Entity target;

    public AttackEvent(Entity target) {
        this.target = target;

        Minecraft mc = IMinecraft.mc;
        World world = mc.theWorld;

        if (mc.effectRenderer != null && world != null) {
            int blockId = Block.getIdFromBlock(Blocks.redstone_block);

            for (int i = 0; i < 25; i++) {
                double dx = target.posX + (Math.random() - 0.5);
                double dy = target.posY + target.getEyeHeight() / 2 + (Math.random() - 0.5);
                double dz = target.posZ + (Math.random() - 0.5);

                mc.effectRenderer.spawnEffectParticle(
                        EnumParticleTypes.WATER_SPLASH.getParticleID(),
                        dx, dy, dz,
                        0.0, 0.0, 0.0,
                        blockId);
            }
        }
    }

    @Override
    public void call(EventListener listener) {
        Objects.requireNonNull(listener).onAttack(this);
    }
}