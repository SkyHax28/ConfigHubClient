package com.dew.system.module;

import com.dew.DewCommon;
import com.dew.IMinecraft;
import com.dew.system.event.EventListener;
import com.dew.system.event.events.*;
import com.dew.system.gui.ClickGuiScreen;
import com.dew.system.module.modules.combat.KillAura;
import com.dew.system.module.modules.player.AutoTool;
import com.dew.system.module.modules.render.ClickGui;
import com.dew.system.viapatcher.PacketPatcher;
import com.dew.utils.BlinkUtil;
import com.dew.utils.LogUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S05PacketSpawnPosition;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S0CPacketSpawnPlayer;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;
import org.lwjgl.input.Keyboard;

public class HandleEvents implements EventListener {
    private final Minecraft mc = IMinecraft.mc;

    private boolean worldFullLoaded = true;
    private boolean loadingWorld = false;

    private int rotLockTick = 0;

    public HandleEvents() {
        DewCommon.eventManager.register(this);

        LogUtil.infoLog("init handleEvents");
    }

    public boolean isWorldFullLoaded() {
        return this.worldFullLoaded && !this.loadingWorld;
    }

    public boolean canRotation() {
        return rotLockTick == 0;
    }

    @Override
    public void onWorld(WorldEvent event) {
        DewCommon.clientConfigManager.save();
        worldFullLoaded = false;
        loadingWorld = true;
        rotLockTick = 0;

        BlinkUtil.sync(true, true);
        BlinkUtil.stopBlink();
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        DewCommon.moduleManager.getModule(AutoTool.class).autoToolManager.tick();

        if (rotLockTick > 0) {
            rotLockTick--;
        }

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

        for (Module module : DewCommon.moduleManager.getModules()) {
            if (module.key == key) {
                if (module == DewCommon.moduleManager.getModule(ClickGui.class)) {
                    mc.displayGuiScreen(new ClickGuiScreen());
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

        if (BlinkUtil.blinking && !BlinkUtil.limiter && !(packet instanceof C00Handshake || packet instanceof C00PacketServerQuery || packet instanceof C01PacketPing || packet instanceof C01PacketChatMessage)) {
            event.cancel();
            BlinkUtil.addPacket(packet);
        }

        PacketPatcher.handleFixedSendPackets(event);
    }

    @Override
    public void onReceivedPacket(ReceivedPacketEvent event) {
        if (mc.thePlayer == null) return;

        Packet<?> packet = event.packet;

        if (packet instanceof S08PacketPlayerPosLook) {
            rotLockTick = 3;
        }

        PacketPatcher.handleFixedReceivePackets(event);
    }
}
