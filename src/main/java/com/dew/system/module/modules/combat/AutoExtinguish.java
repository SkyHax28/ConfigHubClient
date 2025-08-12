package com.dew.system.module.modules.combat;

import com.dew.DewCommon;
import com.dew.system.event.events.TickEvent;
import com.dew.system.event.events.WorldLoadEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.module.modules.player.Scaffold;
import com.dew.utils.LogUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;

public class AutoExtinguish extends Module {

    private int prevSlot = -1;
    private int targetSlot = -1;
    private int stage = 0;
    private int delayTicks = 0;
    public AutoExtinguish() {
        super("Auto Extinguish", ModuleCategory.COMBAT, Keyboard.KEY_NONE, false, true, true);
    }

    @Override
    public void onDisable() {
        this.resetState();
    }

    @Override
    public void onLoadWorld(WorldLoadEvent event) {
        this.resetState();
    }

    private void resetState() {
        if (prevSlot != -1) {
            if (mc.thePlayer != null) {
                mc.thePlayer.inventory.currentItem = prevSlot;
                mc.playerController.updateController();
            }
            prevSlot = -1;
        }
        targetSlot = -1;
        stage = 0;
        delayTicks = 0;
    }

    @Override
    public void onTick(TickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (!mc.thePlayer.isBurning() || mc.thePlayer.isUsingItem() || DewCommon.moduleManager.getModule(Scaffold.class).isEnabled() || DewCommon.moduleManager.getModule(Aura.class).isEnabled() && DewCommon.moduleManager.getModule(Aura.class).target != null) {
            this.resetState();
            return;
        }

        if (delayTicks > 0) {
            DewCommon.rotationManager.rotateToward(mc.thePlayer.rotationYaw, 90f, 180f, true);
            delayTicks--;
            return;
        }

        switch (stage) {
            case 0:
                BlockPos feetPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
                if (mc.theWorld.getBlockState(feetPos).getBlock() instanceof BlockLiquid) return;

                for (int i = 0; i < 9; i++) {
                    ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
                    if (stack != null && stack.getItem() == Items.water_bucket) {
                        targetSlot = i;
                        break;
                    }
                }
                if (targetSlot == -1) return;

                prevSlot = mc.thePlayer.inventory.currentItem;
                mc.thePlayer.inventory.currentItem = targetSlot;
                mc.playerController.updateController();
                DewCommon.rotationManager.rotateToward(mc.thePlayer.rotationYaw, 90f, 180f, true);
                stage = 1;
                delayTicks = 5;
                break;

            case 1:
                DewCommon.rotationManager.rotateToward(mc.thePlayer.rotationYaw, 90f, 180f, true);
                mc.rightClickMouse();
                stage = 2;
                delayTicks = 2;
                break;

            case 2:
                if (prevSlot != -1) {
                    mc.thePlayer.inventory.currentItem = prevSlot;
                    mc.playerController.updateController();
                }
                this.resetState();
                break;
        }
    }
}
