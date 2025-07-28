package com.dew.system.module.modules.player;

import com.dew.DewCommon;
import com.dew.system.event.events.LivingUpdateEvent;
import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.module.modules.movement.MoveFix;
import com.dew.system.module.modules.movement.NoSlow;
import com.dew.system.settingsvalue.BooleanValue;
import com.dew.utils.MovementUtil;
import org.lwjgl.input.Keyboard;

public class Sprint extends Module {

    public Sprint() {
        super("Sprint", ModuleCategory.PLAYER, Keyboard.KEY_NONE, false, true, true);
    }

    public static final BooleanValue omni = new BooleanValue("Omni", true);

    public boolean shouldNotSprint() {
        float yawDiff = Math.abs(MovementUtil.getAngleDifference((float) MovementUtil.getDirection(), DewCommon.rotationManager.getClientYaw()));
        return !MovementUtil.isMoving() || mc.thePlayer.isSneaking() || mc.thePlayer.isCollidedHorizontally || this.isEnabled() && !omni.get() && !mc.gameSettings.keyBindForward.isKeyDown() || (!this.isEnabled() || !omni.get()) && DewCommon.rotationManager.isRotating() && DewCommon.moduleManager.getModule(MoveFix.class).isEnabled() && yawDiff > 45F || mc.thePlayer.isUsingItem() && !DewCommon.moduleManager.getModule(NoSlow.class).canNoSlow() || DewCommon.moduleManager.getModule(Scaffold.class).isEnabled() && Scaffold.noSprint.get();
    }

    @Override
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (mc.thePlayer == null) return;

        mc.thePlayer.setSprinting(!this.shouldNotSprint());
    }
}
