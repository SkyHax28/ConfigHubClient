package com.dew.system.module.modules.combat;

import com.dew.DewCommon;
import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.event.events.StrafeEvent;
import com.dew.system.event.events.LoadWorldEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.module.modules.movement.flight.FlightModule;
import com.dew.system.module.modules.movement.speed.SpeedModule;
import com.dew.system.settingsvalue.NumberValue;
import com.dew.system.settingsvalue.SelectionValue;
import com.dew.utils.MovementUtil;
import net.minecraft.entity.Entity;
import org.lwjgl.input.Keyboard;

public class TargetStrafe extends Module {

    private static final SelectionValue activationMode = new SelectionValue("Activation Mode", "Both Forward and Jump Key", "Both Forward and Jump Key", "Always", "Forward Key Only", "Jump Key");
    private static final NumberValue distance = new NumberValue("Distance", 1.2, 0.0, 5.0, 0.1);
    private int direction = 0;

    public TargetStrafe() {
        super("Target Strafe", ModuleCategory.COMBAT, Keyboard.KEY_NONE, false, true, true);
    }

    public int getDirection() {
        return this.direction;
    }

    public double getDistance() {
        return distance.get();
    }

    public boolean shouldActivate() {
        float forward = 0f;
        if (mc.gameSettings.keyBindForward.isKeyDown()) forward += 1f;
        if (mc.gameSettings.keyBindBack.isKeyDown()) forward -= 1f;

        float strafe = 0f;
        if (mc.gameSettings.keyBindRight.isKeyDown()) strafe += 1f;
        if (mc.gameSettings.keyBindLeft.isKeyDown()) strafe -= 1f;

        switch (activationMode.get().toLowerCase()) {
            case "both forward and jump key":
                return forward == 1f && strafe == 0 && mc.gameSettings.keyBindJump.isKeyDown();

            case "always":
                return true;

            case "forward key only":
                return forward == 1f && strafe == 0;

            case "jump key":
                return mc.gameSettings.keyBindJump.isKeyDown();

            default:
                return false;
        }
    }

    @Override
    public void onDisable() {
        direction = 0;
    }

    @Override
    public void onLoadWorld(LoadWorldEvent event) {
        direction = 0;
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer == null) return;

        if (mc.thePlayer.isCollidedHorizontally || !MovementUtil.isBlockUnderPlayer(mc.thePlayer, 2, 0.1, false) && !DewCommon.moduleManager.getModule(FlightModule.class).isEnabled()) {
            direction = direction == 0 ? 1 : 0;
        }
    }

    @Override
    public void onStrafe(StrafeEvent event) {
        if (mc.thePlayer == null || !DewCommon.moduleManager.getModule(Aura.class).isEnabled() || !DewCommon.moduleManager.getModule(FlightModule.class).isEnabled() && !DewCommon.moduleManager.getModule(SpeedModule.class).isEnabled())
            return;

        Entity target = DewCommon.moduleManager.getModule(Aura.class).target;
        if (target == null || !this.shouldActivate()) return;

        event.moveYaw = MovementUtil.getTargetStrafeYawDirection(target, this.getDistance());
    }
}
