package com.dew.system.module.modules.combat;

import com.dew.DewCommon;
import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.event.events.ReceivedPacketEvent;
import com.dew.system.event.events.TickEvent;
import com.dew.system.event.events.WorldEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.module.modules.movement.speed.SpeedModule;
import com.dew.system.settingsvalue.SelectionValue;
import com.dew.utils.BlinkUtil;
import com.dew.utils.LogUtil;
import com.dew.utils.PacketUtil;
import net.minecraft.item.ItemSword;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.lwjgl.input.Keyboard;

public class AutoBlock extends Module {

    public AutoBlock() {
        super("Auto Block", ModuleCategory.COMBAT, Keyboard.KEY_NONE, false, true, true);
    }

    private static final SelectionValue mode = new SelectionValue("Mode", "Hypixel", "Vanilla", "Legit", "Hypixel");

    private boolean blinkAB = true;
    private boolean block = false;
    private boolean blink = false;
    private boolean swapped = false;
    private int serverSlot = -1;

    private long legitBlockEndTime = 0L;

    @Override
    public void onDisable() {
        this.resetState();
    }

    @Override
    public void onWorld(WorldEvent event) {
        this.resetState();
    }

    private void resetState() {
        this.unblock();

        blinkAB = true;
        serverSlot = -1;
    }

    @Override
    public void onTick(TickEvent event) {
        if (mc.thePlayer == null) return;

        KillAura killAuraModule = DewCommon.moduleManager.getModule(KillAura.class);

        if (killAuraModule.isInAutoBlockMode()) {
            switch (mode.get().toLowerCase()) {
                case "vanilla":
                    killAuraModule.doMainFunctions(true);
                    if (!block) {
                        PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
                        block = true;
                    }
                    break;

                case "legit":
                    long now = System.currentTimeMillis();

                    if (now > legitBlockEndTime && !block) {
                        mc.gameSettings.keyBindUseItem.setKeyDown(true);
                        legitBlockEndTime = now + 400;
                        block = true;
                    } else {
                        mc.gameSettings.keyBindUseItem.setKeyDown(false);
                        block = false;
                    }

                    if (!mc.gameSettings.keyBindUseItem.isKeyDown()) {
                        killAuraModule.doMainFunctions(!block);
                    } else {
                        killAuraModule.doMainFunctions(false);
                    }
                    break;

                case "hypixel":
                    if (blinkAB) {
                        BlinkUtil.doBlink();
                        blink = true;

                        int newSlot = (mc.thePlayer.inventory.currentItem % 8 + 1);
                        if (serverSlot != newSlot) {
                            PacketUtil.sendPacket(new C09PacketHeldItemChange(serverSlot = newSlot));
                            swapped = true;
                            block = false;
                        }

                        killAuraModule.doMainFunctions(false);

                        blinkAB = false;
                    } else {
                        int currentSlot = mc.thePlayer.inventory.currentItem;
                        if (serverSlot != currentSlot) {
                            PacketUtil.sendPacket(new C09PacketHeldItemChange(serverSlot = currentSlot));
                            swapped = false;
                        }

                        killAuraModule.doMainFunctions(true);
                        PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
                        block = true;

                        BlinkUtil.sync(true, true);
                        BlinkUtil.stopBlink();

                        blink = false;
                        blinkAB = true;
                    }
                    break;
            }
        } else {
            this.unblock();
        }
    }

    public boolean isHoldingSword() {
        return mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword;
    }

    public void unblock() {
        if (block) {
            block = false;
            if (mode.get().equals("Legit")) {
                mc.gameSettings.keyBindUseItem.setKeyDown(false);
            } else {
                PacketUtil.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
            }
        }

        if (blink) {
            BlinkUtil.sync(true, true);
            BlinkUtil.stopBlink();
            blink = false;
        }

        if (swapped) {
            int currentSlot = mc.thePlayer.inventory.currentItem;
            if (serverSlot != currentSlot) {
                PacketUtil.sendPacket(new C09PacketHeldItemChange(serverSlot = currentSlot));
            }
            swapped = false;
        }

        legitBlockEndTime = 0L;
    }
}
