package com.dew.system.module.modules.combat;

import com.dew.DewCommon;
import com.dew.system.event.events.PreMotionEvent;
import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.event.events.ReceivedPacketEvent;
import com.dew.system.event.events.WorldLoadEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.module.modules.movement.speed.SpeedModule;
import com.dew.system.settingsvalue.NumberValue;
import com.dew.system.settingsvalue.SelectionValue;
import com.dew.utils.LogUtil;
import com.dew.utils.PacketUtil;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import org.lwjgl.input.Keyboard;

public class Velocity extends Module {

    private static final SelectionValue mode = new SelectionValue("Mode", "Normal", "Normal", "Hypixel", "9.0E-4D Exploit", "Prediction", "Jump");
    private static final NumberValue horizontal = new NumberValue("Horizontal", 0.0, -1.0, 1.0, 0.05, () -> mode.get().equals("Normal"));
    private static final NumberValue vertical = new NumberValue("Vertical", 0.0, -1.0, 1.0, 0.05, () -> mode.get().equals("Normal"));
    private int hypTick = 0;
    private boolean sent = false;

    public Velocity() {
        super("Velocity", ModuleCategory.COMBAT, Keyboard.KEY_NONE, false, true, true);
    }

    @Override
    public String tag() {
        return mode.get();
    }

    @Override
    public void onDisable() {
        hypTick = 0;
        sent = false;
    }

    @Override
    public void onLoadWorld(WorldLoadEvent event) {
        hypTick = 0;
        sent = false;
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        hypTick = mc.thePlayer.onGround ? 0 : hypTick + 1;
    }

    @Override
    public void onPreMotion(PreMotionEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (sent) {
            event.cancel();
            sent = false;
        }
    }

    @Override
    public void onReceivedPacket(ReceivedPacketEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        Packet<?> packet = event.packet;

        if (packet instanceof S12PacketEntityVelocity && mc.theWorld.getEntityByID(((S12PacketEntityVelocity) packet).getEntityID()) == mc.thePlayer) {
            switch (mode.get().toLowerCase()) {
                case "normal":
                    if (horizontal.get() != 0f) {
                        mc.thePlayer.motionX = ((S12PacketEntityVelocity) packet).getMotionX() / (8000.0 / horizontal.get());
                        mc.thePlayer.motionZ = ((S12PacketEntityVelocity) packet).getMotionZ() / (8000.0 / horizontal.get());
                    }

                    if (vertical.get() != 0f) {
                        mc.thePlayer.motionY = ((S12PacketEntityVelocity) packet).getMotionY() / (8000.0 / vertical.get());
                    }

                    event.cancel();
                    break;

                case "hypixel":
                    if (!DewCommon.moduleManager.getModule(SpeedModule.class).isEnabled() || hypTick >= 8 || mc.thePlayer.onGround) {
                        mc.thePlayer.motionY = ((S12PacketEntityVelocity) packet).getMotionY() / 8000.0;
                    }
                    event.cancel();
                    break;

                case "9.0e-4d exploit":
                    if (mc.thePlayer.onGround && !sent) {
                        PacketUtil.sendPacketAsSilent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 9.0E-4D, mc.thePlayer.posZ, false));
                        sent = true;
                    }
                    break;

                case "jump":
                case "prediction":
                    if (mc.thePlayer.onGround && mc.thePlayer.isSprinting() && mc.thePlayer.posY > 0.0D) {
                        int motionX = ((S12PacketEntityVelocity) packet).getMotionX();
                        int motionZ = ((S12PacketEntityVelocity) packet).getMotionZ();

                        double horizontal = motionX * motionX + motionZ * motionZ;
                        double horizontalStrength = Math.sqrt(horizontal);

                        if (horizontalStrength <= 1000) return;

                        mc.thePlayer.jump();
                    }
                    break;
            }
        }
    }
}
