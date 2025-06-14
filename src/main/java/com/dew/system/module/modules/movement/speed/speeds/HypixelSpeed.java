package com.dew.system.module.modules.movement.speed.speeds;

import com.dew.DewCommon;
import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.module.modules.exploit.Disabler;
import com.dew.system.module.modules.movement.speed.SpeedMode;
import com.dew.system.module.modules.player.Scaffold;
import com.dew.utils.LogUtil;
import com.dew.utils.MovementUtil;
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

    private int ticks = 0;
    private boolean allowLow = false;

    private void resetState() {
        ticks = 0;
        allowLow = false;
    }

    private void strafeWithCorrectHypPotMath(float speed) {
        if (!MovementUtil.isMoving()) return;
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed))
            MovementUtil.strafe(speed + ((mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1f) * mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() == 0 ? 0.036f : 0.12f));
        else MovementUtil.strafe(speed);
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer == null) return;

        MovementUtil.mcJumpNoBoost = true;

        ticks = mc.thePlayer.onGround ? 0 : ticks + 1;

        if (DewCommon.moduleManager.getModule(Disabler.class).canLowHop()) {
            if (mc.thePlayer.onGround) {
                if (MovementUtil.isMoving()) {
                    mc.thePlayer.jump();
                    allowLow = !mc.thePlayer.isPotionActive(Potion.jump) && !mc.thePlayer.isCollidedHorizontally && !MovementUtil.isBlockAbovePlayer(mc.thePlayer, 2, 1) && !DewCommon.moduleManager.getModule(Scaffold.class).isEnabled();
                }

                this.strafeWithCorrectHypPotMath(0.481f);
            } else if (allowLow) {
                switch (ticks) {
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

                if (ticks >= 9) {
                    allowLow = false;
                }
            }
        } else {
            if (mc.thePlayer.onGround) {
                if (MovementUtil.isMoving()) {
                    mc.thePlayer.jump();
                }

                this.strafeWithCorrectHypPotMath(0.481f);
            } else {
                switch (ticks) {
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
}
