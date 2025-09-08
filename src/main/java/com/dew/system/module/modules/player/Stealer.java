package com.dew.system.module.modules.player;

import com.dew.DewCommon;
import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.event.events.LoadWorldEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.module.modules.exploit.SafetySwitchv2000;
import com.dew.system.settingsvalue.NumberValue;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Stealer extends Module {

    private static final NumberValue startDelay = new NumberValue("Start Delay", 1.0, 0.0, 10.0, 1.0);
    private static final NumberValue stealDelay = new NumberValue("Steal Delay", 2.0, 0.0, 10.0, 1.0);
    private static final NumberValue closeDelay = new NumberValue("Close Delay", 2.0, 0.0, 10.0, 1.0);
    private int tickDelayCounter = 0;
    private int closeTickCounter = 0;
    private int startDelayCounter = 0;
    private boolean wasChestOpenLastTick = false;

    public Stealer() {
        super("Stealer", ModuleCategory.PLAYER, Keyboard.KEY_NONE, false, true, true);
    }

    @Override
    public void onDisable() {
        this.resetState();
    }

    @Override
    public void onLoadWorld(LoadWorldEvent event) {
        if (DewCommon.moduleManager.getModule(SafetySwitchv2000.class).isEnabled()) {
            this.setState(false);
        } else {
            this.resetState();
        }
    }

    private void resetState() {
        tickDelayCounter = 0;
        closeTickCounter = 0;
        startDelayCounter = 0;
        wasChestOpenLastTick = false;
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer == null || mc.thePlayer.inventory.getItemStack() != null || !(mc.currentScreen instanceof GuiChest)) {
            this.resetState();
            return;
        }

        if (!wasChestOpenLastTick) {
            startDelayCounter = Math.max(1, startDelay.get().intValue());
        }
        wasChestOpenLastTick = true;

        if (startDelayCounter > 0) {
            startDelayCounter--;
            return;
        }

        if (tickDelayCounter > 0) {
            tickDelayCounter--;
            return;
        }

        GuiChest chestGui = (GuiChest) mc.currentScreen;
        IInventory chestInventory = chestGui.lowerChestInventory;

        List<Integer> slotIndices = new ArrayList<>();
        for (int i = 0; i < chestInventory.getSizeInventory(); i++) {
            slotIndices.add(i);
        }
        Collections.shuffle(slotIndices);

        for (int i : slotIndices) {
            ItemStack stack = chestInventory.getStackInSlot(i);
            if (stack != null && !DewCommon.moduleManager.getModule(Manager.class).isTrash(stack)) {
                mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, i, 0, 1, mc.thePlayer);
                tickDelayCounter = Math.max(1, stealDelay.get().intValue());
                closeTickCounter = 0;
                return;
            }
        }

        boolean chestEmpty = true;
        for (int i = 0; i < chestInventory.getSizeInventory(); i++) {
            ItemStack stack = chestInventory.getStackInSlot(i);
            if (DewCommon.moduleManager.getModule(Manager.class).isTrash(stack)) continue;
            if (chestInventory.getStackInSlot(i) != null) {
                chestEmpty = false;
                break;
            }
        }

        if (chestEmpty) {
            closeTickCounter++;
            if (closeTickCounter >= closeDelay.get().intValue()) {
                mc.thePlayer.closeScreen();
                closeTickCounter = 0;
                wasChestOpenLastTick = false;
            }
        } else {
            closeTickCounter = 0;
        }
    }
}
