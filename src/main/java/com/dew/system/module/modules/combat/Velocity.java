package com.dew.system.module.modules.combat;

import com.dew.DewCommon;
import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.event.events.ReceivedPacketEvent;
import com.dew.system.event.events.WorldEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.module.modules.movement.speed.SpeedModule;
import com.dew.system.settingsvalue.SelectionValue;
import com.dew.utils.BlinkUtil;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import org.lwjgl.input.Keyboard;

public class Velocity extends Module {

    public Velocity() {
        super("Velocity", ModuleCategory.COMBAT, Keyboard.KEY_NONE, false, true, true);
    }

    private static final SelectionValue mode = new SelectionValue("Mode", "Normal", "Normal", "Hypixel", "Jump");

    private int hypTick = 0;

    @Override
    public void onDisable() {
        hypTick = 0;
    }

    @Override
    public void onWorld(WorldEvent event) {
        hypTick = 0;
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        hypTick = mc.thePlayer.onGround ? 0 : hypTick + 1;
    }

    @Override
    public void onReceivedPacket(ReceivedPacketEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        Packet<?> packet = event.packet;

        if (packet instanceof S12PacketEntityVelocity && mc.theWorld.getEntityByID(((S12PacketEntityVelocity) packet).getEntityID()) == mc.thePlayer) {
            switch (mode.get().toLowerCase()) {
                case "normal":
                    event.cancel();
                    break;

                case "hypixel":
                    if (!DewCommon.moduleManager.getModule(SpeedModule.class).isEnabled() || hypTick >= 10 || mc.thePlayer.onGround) {
                        if (DewCommon.moduleManager.getModule(KillAura.class).isInAutoBlockMode()) {
                            BlinkUtil.sync(true, true);
                            BlinkUtil.stopBlink();
                        }
                        mc.thePlayer.motionY = ((S12PacketEntityVelocity) packet).getMotionY() / 8000.0;
                    }
                    event.cancel();
                    break;
                case "jump":
                    EntityPlayerSP player = mc.thePlayer;
                    if (player == null) return;

                    if (event.packet instanceof S12PacketEntityVelocity) {
                        S12PacketEntityVelocity packet1 = (S12PacketEntityVelocity) packet;
                        if (packet1.getEntityID() == player.getEntityId()) {
                            if (player.onGround && player.isSprinting()) {
                                int motionX = packet1.getMotionX();
                                int motionZ = packet1.getMotionZ();

                                double horizontal = motionX * motionX + motionZ * motionZ;
                                double horizontalStrength = Math.sqrt(horizontal);

                                if (horizontalStrength <= 1000) return;

                                mc.thePlayer.jump();
                            }
                        }
                    }
            }
        }
    }
}
