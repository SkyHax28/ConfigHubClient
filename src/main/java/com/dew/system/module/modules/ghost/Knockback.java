package com.dew.system.module.modules.ghost;

import com.dew.system.event.events.*;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.NumberValue;
import com.dew.system.settingsvalue.SelectionValue;
import com.dew.utils.Clock;
import com.dew.utils.PacketUtil;
import com.dew.utils.RandomUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import org.lwjgl.input.Keyboard;

public class Knockback extends Module {

    private static final SelectionValue mode = new SelectionValue("Mode", "WTap", "WTap", "Legit", "Rapid", "Backtap", "Packet", "Init Packet", "Legit Packet", "Dual Packet");
    private static final NumberValue minDelayValue = new NumberValue("Min Delay", 50.0, 50.0, 500.0, 1.0);
    private static final NumberValue maxDelayValue = new NumberValue("Max Delay", 50.0, 50.0, 500.0, 1.0);
    private final Clock attackDelay = new Clock();
    private boolean isHit = false;
    private float delay = 0f;

    public Knockback() {
        super("Knockback", ModuleCategory.GHOST, Keyboard.KEY_NONE, false, true, true);
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
        attackDelay.reset();
        isHit = false;
        delay = 0f;
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mode.get().equals("Rapid") && isHit) {
            if (mc.thePlayer.serverSprintState) {
                mc.thePlayer.sprintingTicksLeft = 0;
            }

            isHit = false;
        }
    }

    @Override
    public void onMoveForward(MoveForwardEvent event) {
        if (mode.get().equals("WTap") && isHit) {
            if (mc.thePlayer.serverSprintState) {
                event.reset = true;
            }

            isHit = false;
        }
    }

    @Override
    public void onPostSprint(PostSprintEvent event) {
        if (mode.get().equalsIgnoreCase("Legit") && attackDelay.hasTimePassed(delay)) {
            if (isHit) {
                if (mc.thePlayer.serverSprintState) {
                    mc.thePlayer.setSprinting(false);
                }

                isHit = false;
            }

            delay = RandomUtil.nextFloat(minDelayValue.get().floatValue(), maxDelayValue.get().floatValue());
            attackDelay.reset();
        }
    }

    @Override
    public void onAttack(AttackEvent event) {
        if (!mc.gameSettings.keyBindForward.isKeyDown() || mc.thePlayer.isSneaking()) return;

        Entity target = event.target;

        if (target instanceof EntityLivingBase) {
            switch (mode.get().toLowerCase()) {
                case "legit packet":
                    if (mc.thePlayer.isSprinting()) {
                        if (!mc.thePlayer.serverSprintState) {
                            PacketUtil.sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                            mc.thePlayer.serverSprintState = true;
                        }
                        mc.thePlayer.setSprinting(false);
                    }
                    break;

                case "init packet":
                    mc.thePlayer.sendStartSprintingPacket();
                    mc.thePlayer.serverSprintState = true;
                    break;

                case "packet":
                    mc.thePlayer.sendStopSprintingPacket();
                    mc.thePlayer.sendStartSprintingPacket();
                    mc.thePlayer.serverSprintState = true;
                    break;

                case "dual packet":
                    for (int i = 0; i < 2; i++) {
                        mc.thePlayer.sendStopSprintingPacket();
                        mc.thePlayer.sendStartSprintingPacket();
                    }
                    mc.thePlayer.serverSprintState = true;
                    break;
            }
        }
    }

    @Override
    public void onMoveInput(MoveInputEvent event) {
        if (mc.objectMouseOver != null && mc.objectMouseOver.entityHit instanceof EntityLivingBase) {
            EntityLivingBase entity = (EntityLivingBase) mc.objectMouseOver.entityHit;
            if (mode.get().equalsIgnoreCase("Backtap")) {
                switch (entity.hurtTime) {
                    case 10:
                        event.forward = -1f;
                        break;
                    case 9:
                        event.forward = 1f;
                        break;
                }
            }
        }
    }
}
