package com.dew.system.module.modules.combat;

import com.dew.DewCommon;
import com.dew.system.event.events.PreMotionEvent;
import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.event.events.StrafeEvent;
import com.dew.system.event.events.WorldEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.module.modules.movement.flight.FlightModule;
import com.dew.system.module.modules.movement.speed.SpeedModule;
import com.dew.system.settingsvalue.BooleanValue;
import com.dew.system.settingsvalue.NumberValue;
import com.dew.utils.MovementUtil;
import net.minecraft.block.BlockAir;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;

public class TargetStrafe extends Module {

    public TargetStrafe() {
        super("Target Strafe", ModuleCategory.COMBAT, Keyboard.KEY_NONE, false, true, true);
    }

    private static final NumberValue distance = new NumberValue("Distance", 1.2, 0.0, 5.0, 0.1);

    private int direction = 0;

    public int getDirection() {
        return this.direction;
    }

    public double getDistance() {
        return distance.get();
    }

    @Override
    public void onDisable() {
        direction = 0;
    }

    @Override
    public void onWorld(WorldEvent event) {
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
        if (mc.thePlayer == null || !DewCommon.moduleManager.getModule(KillAura.class).isEnabled() || !DewCommon.moduleManager.getModule(FlightModule.class).isEnabled() && !DewCommon.moduleManager.getModule(SpeedModule.class).isEnabled()) return;

        float forward = 0f;
        if (mc.gameSettings.keyBindForward.isKeyDown()) forward += 1f;
        if (mc.gameSettings.keyBindBack.isKeyDown()) forward -= 1f;

        float strafe = 0f;
        if (mc.gameSettings.keyBindRight.isKeyDown()) strafe += 1f;
        if (mc.gameSettings.keyBindLeft.isKeyDown()) strafe -= 1f;

        Entity target = DewCommon.moduleManager.getModule(KillAura.class).target;
        if (target == null || forward != 1f || strafe != 0 || !mc.gameSettings.keyBindJump.isKeyDown()) return;

        event.moveYaw = MovementUtil.getTargetStrafeYawDirection(target, this.getDistance());
    }
}
