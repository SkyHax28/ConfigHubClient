package com.dew.system.module.modules.player;

import com.dew.DewCommon;
import com.dew.system.event.events.LivingUpdateEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.module.modules.movement.MoveFix;
import com.dew.system.module.modules.movement.NoSlow;
import com.dew.system.settingsvalue.BooleanValue;
import com.dew.utils.MovementUtil;
import net.minecraft.world.WorldSettings;
import org.lwjgl.input.Keyboard;

public class Sprint extends Module {

    public static final BooleanValue omni = new BooleanValue("Omni", true);
    public static final BooleanValue rotationsCheck = new BooleanValue("Rotations Check", false);

    public Sprint() {
        super("Sprint", ModuleCategory.PLAYER, Keyboard.KEY_NONE, false, true, true);
    }

    public boolean shouldNotSprint() {
        float yawDiff = Math.abs(MovementUtil.getAngleDifference((float) MovementUtil.getDirection(), DewCommon.rotationManager.getClientYaw()));
        return !MovementUtil.isMoving() || mc.thePlayer.isSneaking() || mc.thePlayer.getFoodStats().getFoodLevel() <= 6 && mc.playerController != null && (mc.playerController.getCurrentGameType() == WorldSettings.GameType.SURVIVAL || mc.playerController.getCurrentGameType() == WorldSettings.GameType.ADVENTURE) || mc.thePlayer.isCollidedHorizontally || this.isEnabled() && !omni.get() && !mc.gameSettings.keyBindForward.isKeyDown() || (!this.isEnabled() || !omni.get()) && DewCommon.rotationManager.isRotating() && DewCommon.moduleManager.getModule(MoveFix.class).isEnabled() && yawDiff > 30F || mc.thePlayer.isUsingItem() && !DewCommon.moduleManager.getModule(NoSlow.class).canNoSlow() || DewCommon.moduleManager.getModule(Scaffold.class).isEnabled() && Scaffold.noSprint.get() || DewCommon.rotationManager.isRotating() && rotationsCheck.get() && yawDiff > 30F;
    }

    @Override
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (mc.thePlayer == null) return;

        mc.thePlayer.setSprinting(!this.shouldNotSprint());
    }
}
