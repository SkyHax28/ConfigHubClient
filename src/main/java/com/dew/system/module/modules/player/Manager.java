package com.dew.system.module.modules.player;

import com.dew.DewCommon;
import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.event.events.WorldEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.module.modules.exploit.Disabler;
import com.dew.system.module.modules.exploit.SafetySwitchv2000;
import com.dew.system.settingsvalue.BooleanValue;
import com.dew.system.settingsvalue.NumberValue;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import org.lwjgl.input.Keyboard;

import java.util.*;

public class Manager extends Module {

    public Manager() {
        super("Manager", ModuleCategory.PLAYER, Keyboard.KEY_NONE, false, true, true);
    }

    private static final NumberValue delay = new NumberValue("Delay", 4.0, 0.0, 10.0, 0.1);
    private static final BooleanValue inventoryOnly = new BooleanValue("Inventory Only", false);

    private int tickDelayCounter = 0;
    private boolean cleaning = false;

    public boolean isCleaning() {
        return this.cleaning;
    }

    @Override
    public void onDisable() {
        this.resetState();
    }

    @Override
    public void onWorld(WorldEvent event) {
        if (DewCommon.moduleManager.getModule(SafetySwitchv2000.class).isEnabled()) {
            this.setState(false);
        }
    }

    private void resetState() {
        tickDelayCounter = 0;
        cleaning = false;
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer == null || mc.currentScreen instanceof GuiChest || inventoryOnly.get() && !(mc.currentScreen instanceof GuiInventory) || DewCommon.moduleManager.getModule(Disabler.class).isEnabled() && DewCommon.moduleManager.getModule(Disabler.class).isInventoryDisablerEnabled() && mc.thePlayer.isUsingItem()) {
            this.resetState();
            return;
        }

        if (tickDelayCounter > 0) {
            cleaning = true;
            tickDelayCounter--;
            return;
        }

        if (manageArmor()) return;

        int bestWeapon = getBestSwordSlot();
        int bestPickaxe = getBestToolSlot(ItemPickaxe.class);
        int bestAxe = getBestToolSlot(ItemAxe.class);
        int bestShovel = getBestToolSlot(ItemSpade.class);

        if (moveToHotbar(bestWeapon, 0)) return;
        if (moveToHotbar(bestPickaxe, 1)) return;
        if (moveToHotbar(bestAxe, 2)) return;
        if (moveToHotbar(bestShovel, 3)) return;
        if (searchAndMoveSelectedItemToHotbar(Items.golden_apple, 4)) return;
        if (searchAndMoveBlocksToHotbar(6)) return;
        if (moveToHotbar(getBestPotionSlot(true), 7)) return;
        if (moveToHotbar(getBestPotionSlot(false), 8)) return;

        for (int i = 0; i < 36; i++) {
            if (i == bestWeapon) continue;
            ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            if (stack == null) continue;
            if (stack.getItem() instanceof ItemSword) {
                dropSlot(i);
                return;
            }
        }

        for (int i = 0; i < 36; i++) {
            if (i == bestPickaxe) continue;
            ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            if (stack == null) continue;
            if (stack.getItem() instanceof ItemPickaxe) {
                dropSlot(i);
                return;
            }
        }

        for (int i = 0; i < 36; i++) {
            if (i == bestAxe) continue;
            ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            if (stack == null) continue;
            if (stack.getItem() instanceof ItemAxe) {
                dropSlot(i);
                return;
            }
        }

        for (int i = 0; i < 36; i++) {
            if (i == bestShovel) continue;
            ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            if (stack == null) continue;
            if (stack.getItem() instanceof ItemSpade) {
                dropSlot(i);
                return;
            }
        }

        for (int i = 0; i < 36; i++) {
            if (isTrash(mc.thePlayer.inventory.mainInventory[i])) {
                dropSlot(i);
                return;
            }
        }

        cleaning = false;
    }

    private void addManageDelay() {
        tickDelayCounter = (int) Math.max(1, delay.get());
    }

    private boolean moveToHotbar(int fromInvSlot, int toHotbarIndex) {
        if (fromInvSlot == -1 || toHotbarIndex < 0 || toHotbarIndex > 8) return false;
        if (fromInvSlot == toHotbarIndex) return false;

        cleaning = true;
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, getWindowSlotId(fromInvSlot), toHotbarIndex, 2, mc.thePlayer);
        addManageDelay();
        return true;
    }

    private static final List<Integer> POSITIVE_PRIORITY = Arrays.asList(
            Potion.moveSpeed.id,
            Potion.regeneration.id,
            Potion.heal.id
    );

    private int getBestPotionSlot(boolean splashOnly) {
        int bestSlot = -1;
        int bestPriority = Integer.MAX_VALUE;
        int bestAmplifier = -1;
        int bestDuration = -1;

        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            if (stack == null || !(stack.getItem() instanceof ItemPotion)) continue;

            if (ItemPotion.isSplash(stack.getItemDamage()) != splashOnly) continue;

            List<PotionEffect> effects = ((ItemPotion) stack.getItem()).getEffects(stack);
            if (effects == null || effects.isEmpty()) continue;
            if (hasNegativeEffect(effects)) continue;

            for (PotionEffect effect : effects) {
                int priority = POSITIVE_PRIORITY.indexOf(effect.getPotionID());
                if (priority == -1) priority = POSITIVE_PRIORITY.size();

                int amplifier = effect.getAmplifier();
                int duration = effect.getDuration();

                if (
                        priority < bestPriority ||
                                (priority == bestPriority && amplifier > bestAmplifier) ||
                                (priority == bestPriority && amplifier == bestAmplifier && duration > bestDuration)
                ) {
                    bestPriority = priority;
                    bestAmplifier = amplifier;
                    bestDuration = duration;
                    bestSlot = i;
                }
            }
        }

        return bestSlot;
    }

    private boolean hasNegativeEffect(List<PotionEffect> effects) {
        for (PotionEffect effect : effects) {
            Potion potion = Potion.potionTypes[effect.getPotionID()];
            if (potion != null && potion.isBadEffect()) return true;
        }
        return false;
    }

    private boolean searchAndMoveSelectedItemToHotbar(Item targetItem, int toHotbarIndex) {
        if (targetItem == null || toHotbarIndex < 0 || toHotbarIndex > 8) return false;

        ItemStack hotbarStack = mc.thePlayer.inventory.mainInventory[toHotbarIndex];
        if (hotbarStack != null && hotbarStack.getItem() == targetItem) {
            return false;
        }

        int bestSlot = -1;
        int maxStackSize = 0;

        for (int i = 0; i < 36; i++) {
            if (i == toHotbarIndex) continue;

            ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            if (stack == null || stack.getItem() != targetItem) continue;

            if (stack.stackSize > maxStackSize) {
                maxStackSize = stack.stackSize;
                bestSlot = i;
            }
        }

        if (bestSlot != -1) {
            return moveToHotbar(bestSlot, toHotbarIndex);
        }

        return false;
    }

    private boolean searchAndMoveBlocksToHotbar(int toHotbarIndex) {
        if (toHotbarIndex < 0 || toHotbarIndex > 8) return false;

        ItemStack hotbarStack = mc.thePlayer.inventory.mainInventory[toHotbarIndex];
        if (hotbarStack != null && hotbarStack.getItem() instanceof ItemBlock) {
            return false;
        }

        int bestSlot = -1;
        int maxStackSize = 0;

        for (int i = 0; i < 36; i++) {
            if (i == toHotbarIndex) continue;

            ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            if (stack == null || !(stack.getItem() instanceof ItemBlock)) continue;

            ItemBlock itemBlock = (ItemBlock) stack.getItem();
            Block block = itemBlock.getBlock();

            if (block instanceof BlockFalling) continue;
            if (block instanceof BlockSlab) continue;
            if (block instanceof BlockStairs) continue;
            if (!block.isFullBlock()) continue;

            if (stack.stackSize > maxStackSize) {
                maxStackSize = stack.stackSize;
                bestSlot = i;
            }
        }

        if (bestSlot != -1) {
            return moveToHotbar(bestSlot, toHotbarIndex);
        }

        return false;
    }

    private void dropSlot(int invSlot) {
        cleaning = true;
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, getWindowSlotId(invSlot), 1, 4, mc.thePlayer);
        addManageDelay();
    }

    private int getBestSwordSlot() {
        float bestDamage = 0.0f;
        int bestSlot = -1;

        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            if (stack == null) continue;
            if (stack.getItem() instanceof ItemSword) {
                float damage = getItemDamage(stack);
                if (damage > bestDamage) {
                    bestDamage = damage;
                    bestSlot = i;
                }
            }
        }

        return bestSlot;
    }

    private int getBestToolSlot(Class<?> toolClass) {
        float bestStrength = 0.0f;
        int bestSlot = -1;

        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            if (stack == null || !toolClass.isInstance(stack.getItem())) continue;

            float strength = getToolStrength(stack);
            if (strength > bestStrength) {
                bestStrength = strength;
                bestSlot = i;
            }
        }

        return bestSlot;
    }

    private int getWindowSlotId(int invSlot) {
        if (invSlot >= 0 && invSlot <= 8) {
            return 36 + invSlot;
        }
        return invSlot;
    }

    public boolean isTrash(ItemStack stack) {
        if (stack == null) return false;

        Item item = stack.getItem();

        return item == Items.spider_eye ||
                item == Items.bone ||
                item == Items.stick ||
                item == Items.string ||
                item == Items.glass_bottle ||
                item == Items.egg ||
                item == Items.snowball ||
                item == Items.flower_pot ||
                item == Items.bucket ||
                item == Item.getItemFromBlock(Blocks.chest) ||
                item == Item.getItemFromBlock(Blocks.ender_chest) ||
                item == Item.getItemFromBlock(Blocks.trapped_chest) ||
                item instanceof ItemSeeds;
    }

    private float getItemDamage(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemSword)) return 0;
        return ((ItemSword) stack.getItem()).getDamageVsEntity() + getSharpnessLevel(stack) * 1.25f;
    }

    private float getToolStrength(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemTool) {
            return ((ItemTool) item).getToolMaterial().getEfficiencyOnProperMaterial();
        }
        return 0;
    }

    private int getSharpnessLevel(ItemStack stack) {
        if (stack == null || stack.getEnchantmentTagList() == null) return 0;
        return net.minecraft.enchantment.EnchantmentHelper.getEnchantmentLevel(net.minecraft.enchantment.Enchantment.sharpness.effectId, stack);
    }

    private boolean manageArmor() {
        boolean actionPerformed = false;

        for (int armorType = 0; armorType < 4; armorType++) {
            int bestSlot = -1;
            float bestScore = -1;

            for (int i = 0; i < 36; i++) {
                ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
                if (stack == null || !(stack.getItem() instanceof ItemArmor)) continue;

                ItemArmor armor = (ItemArmor) stack.getItem();
                if (armor.armorType != armorType) continue;

                float score = armor.damageReduceAmount + getProtectionLevel(stack);
                if (score > bestScore) {
                    bestScore = score;
                    bestSlot = i;
                }
            }

            ItemStack currentArmor = mc.thePlayer.inventory.armorItemInSlot(3 - armorType);
            float currentScore = -1;
            if (currentArmor != null && currentArmor.getItem() instanceof ItemArmor) {
                ItemArmor current = (ItemArmor) currentArmor.getItem();
                if (current.armorType == armorType) {
                    currentScore = current.damageReduceAmount + getProtectionLevel(currentArmor);
                }
            }

            if (bestScore > currentScore) {
                if (currentArmor != null) {
                    dropArmorSlot(armorType);
                } else {
                    shiftClick(bestSlot);
                }
                return true;
            }
        }

        for (int armorType = 0; armorType < 4; armorType++) {
            for (int i = 0; i < 36; i++) {
                ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
                if (stack == null || !(stack.getItem() instanceof ItemArmor)) continue;

                ItemArmor armor = (ItemArmor) stack.getItem();
                if (armor.armorType != armorType) continue;

                dropSlot(i);
                return true;
            }
        }

        return actionPerformed;
    }

    private int getProtectionLevel(ItemStack stack) {
        if (stack == null) return 0;
        return net.minecraft.enchantment.EnchantmentHelper.getEnchantmentLevel(net.minecraft.enchantment.Enchantment.protection.effectId, stack);
    }

    private void dropArmorSlot(int armorType) {
        int slotId = 5 + armorType;
        cleaning = true;
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slotId, 1, 4, mc.thePlayer);
        addManageDelay();
    }

    private void shiftClick(int invSlot) {
        cleaning = true;
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, getWindowSlotId(invSlot), 0, 1, mc.thePlayer);
        addManageDelay();
    }
}
