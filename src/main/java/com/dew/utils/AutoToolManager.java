package com.dew.utils;

import com.dew.IMinecraft;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.BlockPos;

public class AutoToolManager {
    private final Minecraft mc = IMinecraft.mc;

    private int originalSlot = -1;
    private int switchBackTicks = -1;

    private static final int RETURN_DELAY_TICKS = 3;

    public void switchToBestSword() {
        if (mc.thePlayer == null) return;

        int bestSlot = -1;
        float bestDamage = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if (stack == null) continue;

            if (stack.getItem() instanceof ItemSword) {
                float damage = ((ItemSword) stack.getItem()).getDamageVsEntity();
                int sharpness = EnchantmentHelper.getEnchantmentLevel(16, stack);
                if (sharpness > 0) {
                    damage += 1.0F + 0.5F * sharpness;
                }

                if (damage > bestDamage) {
                    bestDamage = damage;
                    bestSlot = i;
                }
            }
        }

        if (bestSlot != -1 && bestSlot != mc.thePlayer.inventory.currentItem) {
            mc.thePlayer.inventory.currentItem = bestSlot;
            mc.playerController.updateController();
        }
    }

    public void start(BlockPos pos) {
        if (mc.thePlayer == null || mc.theWorld == null || pos == null) return;

        Block block = mc.theWorld.getBlockState(pos).getBlock();
        if (block.getMaterial().isReplaceable()) return;

        switchBackTicks = -1;

        float bestSpeed = getStrVsBlock(null, block, pos);
        int bestSlot = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if (stack == null) continue;

            float speed = getStrVsBlock(stack, block, pos);
            if (speed > bestSpeed + 0.1f) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }

        if (bestSlot != -1 && bestSlot != mc.thePlayer.inventory.currentItem) {
            if (originalSlot == -1) {
                originalSlot = mc.thePlayer.inventory.currentItem;
            }
            mc.thePlayer.inventory.currentItem = bestSlot;
            mc.playerController.updateController();
        }
    }

    public void stop() {
        if (originalSlot != -1 && switchBackTicks == -1) {
            switchBackTicks = RETURN_DELAY_TICKS;
        }
    }

    public void tick() {
        if (switchBackTicks > 0) {
            switchBackTicks--;
            if (switchBackTicks == 0 && mc.thePlayer != null) {
                mc.thePlayer.inventory.currentItem = originalSlot;
                mc.playerController.updateController();
                originalSlot = -1;
            }
        }
    }

    private float getStrVsBlock(ItemStack stack, Block block, BlockPos pos) {
        if (stack == null) return 1.0F;

        float base = stack.getStrVsBlock(block);
        if (base > 1.0F) {
            int efficiency = EnchantmentHelper.getEnchantmentLevel(32, stack);
            if (efficiency > 0) {
                base += efficiency * efficiency + 1;
            }
        }

        return base;
    }

    public void reset() {
        originalSlot = -1;
        switchBackTicks = -1;
    }
}