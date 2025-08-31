package com.dew.system.module.modules.combat;

import com.dew.DewCommon;
import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.event.events.ReceivedPacketEvent;
import com.dew.system.event.events.TickEvent;
import com.dew.system.event.events.WorldLoadEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.SelectionValue;
import com.dew.utils.BlinkUtil;
import com.dew.utils.PacketUtil;
import net.minecraft.item.ItemSword;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Keyboard;

public class AutoBlock extends Module {

    private static final SelectionValue mode = new SelectionValue("Mode", "Hypixel", "Vanilla", "Hold", "Legit", "Hypixel");
    private boolean blinkAB = true;
    private boolean block = false;
    private boolean blink = false;
    private boolean swapped = false;
    private int serverSlot = -1;
    private long legitBlockEndTime = 0L;

    public AutoBlock() {
        super("Auto Block", ModuleCategory.COMBAT, Keyboard.KEY_NONE, false, true, true);
    }

    public String getMode() {
        return mode.get();
    }

    @Override
    public String tag() {
        return mode.get();
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
        this.unblock();

        blinkAB = true;
        serverSlot = -1;
    }

    @Override
    public void onTick(TickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        Aura auraModule = DewCommon.moduleManager.getModule(Aura.class);

        if (auraModule.isInAutoBlockMode()) {
            switch (mode.get().toLowerCase()) {
                case "vanilla":
                    auraModule.doMainFunctions(true);
                    if (!block) {
                        PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
                        block = true;
                    }
                    break;

                case "hold":
                    if (!block || !mc.gameSettings.keyBindUseItem.isKeyDown()) {
                        mc.gameSettings.keyBindUseItem.setKeyDown(true);
                        block = true;
                        auraModule.doMainFunctions(false);
                    } else {
                        auraModule.doMainFunctions(true);
                    }
                    break;

                case "legit":
                    long now = System.currentTimeMillis();

                    auraModule.doMainFunctions(!mc.gameSettings.keyBindUseItem.isKeyDown() && !block);

                    if (!block && now > legitBlockEndTime && mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
                        mc.gameSettings.keyBindUseItem.setKeyDown(true);
                        legitBlockEndTime = now + 1;
                        block = true;
                    } else if (block && now <= legitBlockEndTime && mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
                        mc.gameSettings.keyBindUseItem.setKeyDown(true);
                    } else {
                        mc.gameSettings.keyBindUseItem.setKeyDown(false);
                        block = false;
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

                        auraModule.doMainFunctions(false);

                        blinkAB = false;
                    } else {
                        int currentSlot = mc.thePlayer.inventory.currentItem;
                        if (serverSlot != currentSlot) {
                            PacketUtil.sendPacket(new C09PacketHeldItemChange(serverSlot = currentSlot));
                            swapped = false;
                        }

                        auraModule.doMainFunctions(true);
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

    @Override
    public void onReceivedPacket(ReceivedPacketEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        Packet<?> packet = event.packet;

        if (packet instanceof S12PacketEntityVelocity && mc.theWorld.getEntityByID(((S12PacketEntityVelocity) packet).getEntityID()) == mc.thePlayer && mode.get().equals("Hypixel")) {
            if (DewCommon.moduleManager.getModule(Aura.class).isInAutoBlockMode()) {
                BlinkUtil.sync(true, true);
                BlinkUtil.stopBlink();
            }
        }
    }

    public boolean isHoldingSword() {
        return mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword;
    }

    public void unblock() {
        if (block) {
            if (mode.get().equals("Legit")) {
                mc.gameSettings.keyBindUseItem.setKeyDown(false);
            } else if (mode.get().equals("Hold")) {
                mc.gameSettings.keyBindUseItem.setKeyDown(false);
            } else {
                PacketUtil.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
            }
            block = false;
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
