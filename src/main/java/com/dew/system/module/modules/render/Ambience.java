package com.dew.system.module.modules.render;

import com.dew.system.event.events.ReceivedPacketEvent;
import com.dew.system.event.events.Render3DEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.BooleanValue;
import com.dew.system.settingsvalue.SelectionValue;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S03PacketTimeUpdate;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import org.lwjgl.input.Keyboard;

public class Ambience extends Module {

    private static final SelectionValue mode = new SelectionValue("Mode", "Midnight", "Day", "Noon", "Dusk", "Night", "Midnight");
    private static final BooleanValue clearWeather = new BooleanValue("Clear Weather", true);

    public Ambience() {
        super("Ambience", ModuleCategory.RENDER, Keyboard.KEY_NONE, false, false, true);
    }

    @Override
    public String tag() {
        return mode.get();
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (mc.theWorld == null) return;

        this.update();
    }

    @Override
    public void onReceivedPacket(ReceivedPacketEvent event) {
        if (mc.theWorld == null) return;

        Packet<?> packet = event.packet;

        if (packet instanceof S03PacketTimeUpdate || clearWeather.get() && packet instanceof S2BPacketChangeGameState && (((S2BPacketChangeGameState) packet).getGameState() == 7 || ((S2BPacketChangeGameState) packet).getGameState() == 8)) {
            this.update();
            event.cancel();
        }
    }

    private void update() {
        int time = 0;

        switch (mode.get().toLowerCase()) {
            case "day":
                time = 1000;
                break;

            case "noon":
                time = 6000;
                break;

            case "dusk":
                time = 12000;
                break;

            case "night":
                time = 13000;
                break;

            case "midnight":
                time = 18000;
                break;
        }

        mc.theWorld.setWorldTime(time);

        if (clearWeather.get()) {
            mc.theWorld.setRainStrength(0f);
            mc.theWorld.setThunderStrength(0f);
        }
    }
}