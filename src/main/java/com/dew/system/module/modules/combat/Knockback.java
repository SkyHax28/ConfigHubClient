package com.dew.system.module.modules.combat;
import com.dew.system.event.events.*;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.BooleanValue;
import com.dew.system.settingsvalue.NumberValue;
import com.dew.system.settingsvalue.SelectionValue;
import com.dew.utils.Clock;
import com.dew.utils.LogUtil;
import com.dew.utils.RandomUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.optifine.Log;
import org.lwjgl.input.Keyboard;

public class Knockback extends Module {
    private static SelectionValue mode = new SelectionValue("Mode", "WTap", "WTap", "Legit", "Rapid", "Backtap", "Packet", "InitPacket", "LegitPacket", "DualPacket");
    private static NumberValue minDelayValue = new NumberValue("MinDelayValue", 50, 50, 500, 1);
    private static NumberValue maxDelayValue = new NumberValue("MaxDelayValue", 50, 50, 500, 1);
    private static BooleanValue hurtTime = new BooleanValue("HurtTime Check", true);
    private static NumberValue hurtTimeThreshold = new NumberValue("HurtTime Threshold", 10, 0, 10, 1);
    private static BooleanValue debug = new BooleanValue("Debug", false);
    private boolean isHit = false;
    private final Clock attackDelay = new Clock();
    private float delay = 0f;
    public Knockback() {
        super("Knockback", ModuleCategory.COMBAT, Keyboard.KEY_NONE, false, true, true);
    }
    @Override
    public void onEnable() {
        attackDelay.reset();
        isHit = false;
    }

    @Override
    public void onDisable() {
        attackDelay.reset();
        isHit = false;
    }
    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mode.get().equalsIgnoreCase("Rapid") && isHit) {
            if (mc.thePlayer.serverSprintState) {
                mc.thePlayer.sprintingTicksLeft = 0;
                debug("Rapid: Knockback");
            }
            isHit = false;
        }
    }
    @Override
    public void onMoveForward(MoveForwardEvent event) {
        if (mode.get().equalsIgnoreCase("WTap") && isHit) {
            if (mc.thePlayer.serverSprintState) {
                event.reset = true;
                debug("WTap: Knockback");
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
                    debug("Legit: Knockback");
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

        if (event.target instanceof EntityLivingBase) {
            EntityLivingBase target = (EntityLivingBase) event.target;
            if (target.hurtTime == hurtTimeThreshold.get() || !hurtTime.get()) {
                switch (mode.get().toLowerCase()) {
                    case "legit": debug("Legit: Knockback");
                    case "rapid": debug("Rapid: Knockback");
                    case "wtap":
                        isHit = true;
                        debug("WTap: Knockback");
                        break;
                    case "legitpacket":
                        if (mc.thePlayer.isSprinting()) {
                            if (!mc.thePlayer.serverSprintState) {
                                mc.getNetHandler().addToSendQueue(
                                        new C0BPacketEntityAction(mc.thePlayer,
                                                C0BPacketEntityAction.Action.START_SPRINTING));
                                mc.thePlayer.serverSprintState = true;
                            }
                            mc.thePlayer.setSprinting(false);
                            debug("LegitPacket: Knockback");
                        }
                        break;
                    case "initpacket":
                        mc.thePlayer.sendStartSprintingPacket();
                        mc.thePlayer.serverSprintState = true;
                        debug("InitPacket: Knockback");
                        break;
                    case "packet":
                        mc.thePlayer.sendStopSprintingPacket();
                        mc.thePlayer.sendStartSprintingPacket();
                        mc.thePlayer.serverSprintState = true;
                        debug("Packet: Knockback");
                        break;
                    case "dualpacket":
                        for (int i = 0; i < 2; i++) {
                            mc.thePlayer.sendStopSprintingPacket();
                            mc.thePlayer.sendStartSprintingPacket();
                        }
                        mc.thePlayer.serverSprintState = true;
                        debug("Dual Packet: Knockback");
                        break;
                }
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
                        debug("Backtap: Knockback");
                        break;
                    case 9:
                        event.forward = 1f;
                        debug("Backtap: Knockback");
                        break;
                }
            }
        }
    }
    public void debug(String text1) {
        if (debug.get()){
            LogUtil.printChat(text1);
        }
    }
}
