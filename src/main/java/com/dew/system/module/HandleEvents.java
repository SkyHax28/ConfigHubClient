package com.dew.system.module;

import com.dew.DewCommon;
import com.dew.IMinecraft;
import com.dew.system.event.EventListener;
import com.dew.system.event.events.*;
import com.dew.system.gui.ClickGuiScreen;
import com.dew.system.gui.NewClickGuiScreen;
import com.dew.system.module.modules.player.AutoTool;
import com.dew.system.module.modules.render.ClickGui;
import com.dew.system.viapatcher.PacketPatcher;
import com.dew.utils.BlinkUtil;
import com.dew.utils.LogUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;
import org.lwjgl.input.Keyboard;

public class HandleEvents implements EventListener {
    private final Minecraft mc = IMinecraft.mc;

    private boolean worldFullLoaded = true;
    private boolean loadingWorld = false;

    public HandleEvents() {
        DewCommon.eventManager.register(this);

        LogUtil.infoLog("init handleEvents");
    }

    public boolean isWorldFullLoaded() {
        return this.worldFullLoaded && !this.loadingWorld;
    }

    @Override
    public void onWorld(WorldEvent event) {
        DewCommon.clientConfigManager.save();
        worldFullLoaded = false;
        loadingWorld = true;

        BlinkUtil.sync(true, true);
        BlinkUtil.stopBlink();
    }

    @Override
    public void onItemRender(ItemRenderEvent event) {
        if (DewCommon.moduleManager.getModule(AutoTool.class).autoToolManager.originalSlot != -1) {
            event.itemToRender = mc.thePlayer.inventory.getStackInSlot(DewCommon.moduleManager.getModule(AutoTool.class).autoToolManager.originalSlot);
        }
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        DewCommon.moduleManager.getModule(AutoTool.class).autoToolManager.tick();

        if (loadingWorld && mc.thePlayer != null && mc.theWorld != null && mc.thePlayer.ticksExisted > 10f && mc.currentScreen == null) {
            worldFullLoaded = true;
            loadingWorld = false;
        }
    }

    @Override
    public void onKeyboard(KeyboardEvent event) {
        if (mc.thePlayer == null || mc.currentScreen != null) return;

        int key = event.key;
        boolean isPress = event.isPress;

        if (!isPress || key == Keyboard.KEY_NONE) return;

        ClickGui clickGuiModule = DewCommon.moduleManager.getModule(ClickGui.class);
        for (Module module : DewCommon.moduleManager.getModules()) {
            if (module.key == key) {
                if (module == clickGuiModule) {
                    switch (clickGuiModule.getMode().toLowerCase()) {
                        case "modern":
                            mc.displayGuiScreen(DewCommon.clickGuiScreen);
                            break;

                        case "nostalgia":
                            mc.displayGuiScreen(new ClickGuiScreen());
                            break;
                    }
                    return;
                }

                module.toggleState();
            }
        }
    }

    @Override
    public void onSendPacket(SendPacketEvent event) {
        if (mc.thePlayer == null) return;

        Packet<?> packet = event.packet;

        if (packet instanceof C0DPacketCloseWindow && (mc.currentScreen instanceof ClickGuiScreen || mc.currentScreen instanceof NewClickGuiScreen || mc.currentScreen instanceof GuiChat)) {
            event.cancel();
        }

        if (BlinkUtil.blinking && !BlinkUtil.limiter && !(packet instanceof C01PacketChatMessage) && !(packet instanceof C0FPacketConfirmTransaction)) {
            event.cancel();
            BlinkUtil.addPacket(packet);
            return;
        }

        PacketPatcher.handleFixedSendPackets(event);
    }

    @Override
    public void onReceivedPacket(ReceivedPacketEvent event) {
        if (mc.thePlayer == null) return;

        Packet<?> packet = event.packet;

        if (packet instanceof S08PacketPlayerPosLook) {
            DewCommon.rotationManager.setRotationsInstantly(((S08PacketPlayerPosLook) packet).getYaw(), ((S08PacketPlayerPosLook) packet).getPitch());
        }

        PacketPatcher.handleFixedReceivePackets(event);
    }
}
