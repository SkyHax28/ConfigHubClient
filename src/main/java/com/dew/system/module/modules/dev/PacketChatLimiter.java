package com.dew.system.module.modules.dev;

import com.dew.system.event.events.SendPacketEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C01PacketChatMessage;
import org.lwjgl.input.Keyboard;

public class PacketChatLimiter extends Module {

    public PacketChatLimiter() {
        super("Packet Chat Limiter", ModuleCategory.EXPLOIT, Keyboard.KEY_NONE, false, true, true);
    }

    @Override
    public void onSendPacket(SendPacketEvent event) {
        Packet<?> packet = event.packet;

        if (!(packet instanceof C01PacketChatMessage)) {
            event.cancel();
        }
    }
}
