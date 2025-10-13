package com.dew.system.module.modules.other;

import com.dew.system.event.events.ReceivedPacketEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.utils.LogUtil;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import org.lwjgl.input.Keyboard;

public class LightningDetector extends Module {

    public LightningDetector() {
        super("Lightning Detector", ModuleCategory.OTHER, Keyboard.KEY_NONE, true, false, true);
    }

    @Override
    public void onReceivedPacket(ReceivedPacketEvent event) {
        Packet<?> packet = event.packet;

        if (packet instanceof S29PacketSoundEffect && ((S29PacketSoundEffect) packet).getSoundName().equals("ambient.weather.thunder")) {
            int x = (int) ((S29PacketSoundEffect) packet).getX();
            int y = (int) ((S29PacketSoundEffect) packet).getY();
            int z = (int) ((S29PacketSoundEffect) packet).getZ();
            int distance = (int) mc.thePlayer.getDistance(x, y, z);
            LogUtil.printChat("Detected thunder at " + x + " " + y + " " + z + " (" + distance + " blocks away)");
        }
    }
}
