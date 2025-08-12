package com.dew.system.module.modules.combat;

import com.dew.DewCommon;
import com.dew.system.event.events.ItemRenderEvent;
import com.dew.system.event.events.TickEvent;
import com.dew.system.event.events.WorldLoadEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.module.modules.player.Scaffold;
import com.dew.system.settingsvalue.NumberValue;
import com.dew.utils.LogUtil;
import com.dew.utils.MovementUtil;
import net.minecraft.block.*;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Items;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Keyboard;

public class AutoPot extends Module {

    private static final NumberValue throwDelay = new NumberValue("Throw Delay", 15.0, 0.0, 20.0, 1.0);
    private int tickDelayCounter = 0;
    private int stage = 0;
    private int prevSlot = -1;
    private int targetSlot = -1;

    public AutoPot() {
        super("Auto Pot", ModuleCategory.COMBAT, Keyboard.KEY_NONE, false, true, true);
    }

    public boolean isThrowing() {
        return stage != 0;
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
        tickDelayCounter = 0;
        stage = 0;
        prevSlot = -1;
        targetSlot = -1;
    }

    @Override
    public void onItemRender(ItemRenderEvent event) {
        if (prevSlot != -1) {
            event.itemToRender = mc.thePlayer.inventory.getStackInSlot(prevSlot);
        }
    }

    @Override
    public void onTick(TickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null || mc.currentScreen instanceof GuiChest) {
            tickDelayCounter = 0;
            return;
        }

        MovingObjectPosition mop = mc.objectMouseOver;
        if (mop != null) {
            if (mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                BlockPos pos = mop.getBlockPos();
                Block block = mc.theWorld.getBlockState(pos).getBlock();

                if (block instanceof BlockChest || block instanceof BlockEnderChest || block instanceof BlockWorkbench || block instanceof BlockFurnace || block instanceof BlockAnvil || block instanceof BlockFenceGate || block instanceof BlockTrapDoor || block instanceof BlockDoor) {
                    tickDelayCounter = 0;
                    return;
                }
            } else if (mop.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                tickDelayCounter = 0;
                return;
            }
        }

        if (mc.thePlayer.isUsingItem() || stage == 0 && !MovementUtil.isBlockUnderPlayer(mc.thePlayer, 2, 0.2, false) || DewCommon.moduleManager.getModule(Scaffold.class).isEnabled()) {
            tickDelayCounter = 0;
            return;
        }

        if (tickDelayCounter > 0) {
            tickDelayCounter--;
            return;
        }

        switch (stage) {
            case 0:
                for (int i = 0; i < 9; i++) {
                    ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
                    if (stack != null && stack.getItem() == Items.potionitem && ItemPotion.isSplash(stack.getMetadata())) {
                        ItemPotion potion = (ItemPotion) stack.getItem();
                        for (PotionEffect o : potion.getEffects(stack)) {
                            Potion potionType = Potion.potionTypes[o.getPotionID()];
                            if (potionType != null && !potionType.isBadEffect() && !mc.thePlayer.isPotionActive(potionType) && (!potionType.isInstant() || mc.thePlayer.getHealth() < 10f) && (potionType.getId() != Potion.regeneration.getId() || mc.thePlayer.getHealth() < 15f)) {
                                targetSlot = i;
                                break;
                            }
                        }
                    }
                    if (targetSlot != -1) break;
                }

                if (targetSlot != -1) {
                    prevSlot = mc.thePlayer.inventory.currentItem;
                    mc.thePlayer.inventory.currentItem = targetSlot;
                    mc.playerController.updateController();

                    DewCommon.rotationManager.rotateToward(mc.thePlayer.rotationYaw, 90f, 180f, true);
                    stage = 1;
                    tickDelayCounter = 1;
                }

                break;

            case 1:
                DewCommon.rotationManager.rotateToward(mc.thePlayer.rotationYaw, 90f, 180f, true);
                LogUtil.printChat("Throw pot");
                mc.rightClickMouse();
                stage = 2;
                break;

            case 2:
                if (prevSlot != -1) {
                    mc.thePlayer.inventory.currentItem = prevSlot;
                    mc.playerController.updateController();
                }

                this.resetState();
                tickDelayCounter = Math.max(1, throwDelay.get().intValue());
                break;
        }
    }
}
