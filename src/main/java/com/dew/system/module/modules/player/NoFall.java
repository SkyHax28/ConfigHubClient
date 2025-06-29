package com.dew.system.module.modules.player;

import com.dew.system.event.events.*;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.SelectionValue;
import com.dew.utils.MovementUtil;
import com.dew.utils.PacketUtil;
import com.dew.utils.TimerUtil;
import net.minecraft.network.play.client.C03PacketPlayer;
import org.lwjgl.input.Keyboard;

public class NoFall extends Module {

    public NoFall() {
        super("No Fall", ModuleCategory.PLAYER, Keyboard.KEY_NONE, false, true, true);
    }

    private static final SelectionValue mode = new SelectionValue("Mode", "Packet", "Packet", "Spoof", "No Ground", "Hypixel", "Add Packet");

    private int hypTick = 0;

    @Override
    public String tag() {
        return mode.get();
    }

    @Override
    public void onDisable() {
        this.resetState();
    }

    @Override
    public void onWorld(WorldEvent event) {
        this.resetState();
    }

    private void resetState() {
        if (hypTick != 0) {
            TimerUtil.resetTimerSpeed();
            hypTick = 0;
        }
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        switch (mode.get().toLowerCase()) {
            case "hypixel":
                if (mc.thePlayer == null || mc.thePlayer.isSpectator() || mc.thePlayer.capabilities.isFlying || !MovementUtil.isBlockUnderPlayer(mc.thePlayer, 100, false)) {
                    this.resetState();
                } else if (mc.thePlayer.fallDistance > 0 && mc.thePlayer.motionY <= -0.55) {
                    switch (hypTick) {
                        case 0:
                            TimerUtil.setTimerSpeed(0.45f);
                            PacketUtil.sendPacket(new C03PacketPlayer(true));
                            hypTick++;
                            break;

                        case 1:
                            TimerUtil.resetTimerSpeed();
                            hypTick = 0;
                            break;
                    }
                } else {
                    this.resetState();
                }
                break;

            case "add packet":
                if (mc.thePlayer.fallDistance > 0 && mc.thePlayer.ticksExisted % 2 == 0 && mc.thePlayer.motionY <= -0.55) {
                    PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true));
                }
                break;
        }
    }

    @Override
    public void onPreMotion(PreMotionEvent event) {
        switch (mode.get().toLowerCase()) {
            case "packet":
                if (mc.thePlayer.fallDistance > 0 && mc.thePlayer.ticksExisted % 2 == 0 && mc.thePlayer.motionY <= -0.55) {
                    event.onGround = true;
                }
                break;

            case "spoof":
                event.onGround = true;
                break;

            case "no ground":
                event.onGround = false;
                break;
        }
    }
}
