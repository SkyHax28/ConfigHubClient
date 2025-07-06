package com.dew.system.module.modules.movement.speed.speeds;

import com.dew.DewCommon;
import com.dew.system.event.events.PreMotionEvent;
import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.event.events.ReceivedPacketEvent;
import com.dew.system.module.modules.exploit.Disabler;
import com.dew.system.module.modules.movement.speed.SpeedMode;
import com.dew.system.module.modules.movement.speed.SpeedModule;
import com.dew.system.module.modules.player.Scaffold;
import com.dew.utils.LogUtil;
import com.dew.utils.MovementUtil;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.potion.Potion;
import org.lwjgl.input.Keyboard;

public class HypixelSpeed implements SpeedMode {
    @Override
    public String getName() {
        return "Hypixel";
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
        MovementUtil.mcJumpNoBoost = false;
        this.resetState();
    }

    private int preUpdateEventTicks = 0;
    private int preMotionEventTicks = 0;
    private boolean allowLow = false;

    private void resetState() {
        preUpdateEventTicks = 0;
        preMotionEventTicks = 0;
        allowLow = false;
    }

    private void strafeWithCorrectHypPotMath(float speed) {
        if (!MovementUtil.isMoving()) return;
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed))
            MovementUtil.strafe(speed + ((mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1f) * mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() == 0 ? 0.036f : 0.12f));
        else MovementUtil.strafe(speed);
    }

    @Override
    public void onPreMotion(PreMotionEvent event) {
        if (mc.thePlayer == null) return;

        preMotionEventTicks = mc.thePlayer.onGround ? 0 : preMotionEventTicks + 1;

        if (DewCommon.moduleManager.getModule(Disabler.class).canLowHop()) {
            if (SpeedModule.hypixelLowHopMode.get().equals("More Strafe")) {
                if (mc.thePlayer.onGround) {
                    if (MovementUtil.isMoving()) {
                        mc.thePlayer.jump();
                        allowLow = !mc.thePlayer.isPotionActive(Potion.jump) && !MovementUtil.isBlockAbovePlayer(mc.thePlayer, 2, 1) && (!DewCommon.moduleManager.getModule(Scaffold.class).isEnabled() || !GameSettings.isKeyDown(mc.gameSettings.keyBindJump) && DewCommon.moduleManager.getModule(Scaffold.class).jumped);
                    }

                    if ((!DewCommon.moduleManager.getModule(Scaffold.class).isEnabled() || !GameSettings.isKeyDown(mc.gameSettings.keyBindJump) && DewCommon.moduleManager.getModule(Scaffold.class).jumped)) {
                        this.strafeWithCorrectHypPotMath(0.39f);
                    }
                } else if (allowLow) {
                    switch (preMotionEventTicks) {
                        case 1:
                            mc.thePlayer.motionY = 0.39F;
                            this.strafeWithCorrectHypPotMath(0.325f);
                            break;

                        case 3:
                            mc.thePlayer.motionY -= 0.1309F;
                            break;

                        case 4:
                            mc.thePlayer.motionY -= 0.20F;
                            break;

                        case 6:
                            if (MovementUtil.isBlockUnderPlayer(mc.thePlayer, 1, true)) {
                                mc.thePlayer.motionY += 0.075;
                                MovementUtil.strafe(MovementUtil.getSpeed());
                            }
                            break;

                        case 7:
                            if (MovementUtil.isBlockUnderPlayer(mc.thePlayer, 1, true)) {
                                MovementUtil.strafe(MovementUtil.getSpeed());
                            }
                            break;
                    }
                }
            }
        }
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer == null) return;

        MovementUtil.mcJumpNoBoost = true;

        preUpdateEventTicks = mc.thePlayer.onGround ? 0 : preUpdateEventTicks + 1;

        if (DewCommon.moduleManager.getModule(Disabler.class).canLowHop()) {
            if (SpeedModule.hypixelLowHopMode.get().equals("7 Tick")) {
                if (mc.thePlayer.onGround) {
                    if (MovementUtil.isMoving()) {
                        mc.thePlayer.jump();
                        allowLow = !mc.thePlayer.isPotionActive(Potion.jump) && !mc.thePlayer.isCollidedHorizontally && !MovementUtil.isBlockAbovePlayer(mc.thePlayer, 2, 1) && (!DewCommon.moduleManager.getModule(Scaffold.class).isEnabled() || !GameSettings.isKeyDown(mc.gameSettings.keyBindJump) && DewCommon.moduleManager.getModule(Scaffold.class).jumped);
                    }

                    if ((!DewCommon.moduleManager.getModule(Scaffold.class).isEnabled() || !GameSettings.isKeyDown(mc.gameSettings.keyBindJump) && DewCommon.moduleManager.getModule(Scaffold.class).jumped)) {
                        this.strafeWithCorrectHypPotMath(0.41f);
                    }
                } else if (allowLow) {
                    switch (preUpdateEventTicks) {
                        case 1:
                            mc.thePlayer.motionY += 0.0568;
                            break;

                        case 3:
                            mc.thePlayer.motionY -= 0.13;
                            break;

                        case 4:
                            mc.thePlayer.motionY -= 0.2;
                            break;

                        case 7:
                            if (MovementUtil.isBlockUnderPlayer(mc.thePlayer, 1, true)) {
                                this.strafeWithCorrectHypPotMath(0.285f);
                            }
                            break;

                        case 8:
                            mc.thePlayer.motionY = -0.4;
                            break;
                    }

                    if (preUpdateEventTicks >= 9) {
                        allowLow = false;
                    }
                }
            }
        } else {
            if (mc.thePlayer.onGround) {
                if (MovementUtil.isMoving()) {
                    mc.thePlayer.jump();
                }

                this.strafeWithCorrectHypPotMath(0.46f);
            } else {
                switch (preUpdateEventTicks) {
                    case 1:
                        this.strafeWithCorrectHypPotMath(0.2873f);
                        break;

                    case 10:
                        if (mc.thePlayer.hurtTime == 0 && MovementUtil.isBlockUnderPlayer(mc.thePlayer, 1, 1, true)) {
                            mc.thePlayer.motionY = -0.28;
                            this.strafeWithCorrectHypPotMath(0.305f);
                        }
                        break;
                }
            }
        }
    }

    @Override
    public void onReceivedPacket(ReceivedPacketEvent event) {
        if (mc.thePlayer == null) return;

        Packet<?> packet = event.packet;

        if (packet instanceof S08PacketPlayerPosLook) {
            DewCommon.moduleManager.getModule(SpeedModule.class).setState(false);
        }
    }
}
