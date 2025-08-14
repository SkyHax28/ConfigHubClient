package com.dew.system.module.modules.ghost;

import com.dew.DewCommon;
import com.dew.system.event.events.TickEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.module.modules.combat.Teams;
import com.dew.system.module.modules.player.Freecam;
import com.dew.system.settingsvalue.BooleanValue;
import com.dew.system.settingsvalue.NumberValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class SilentAimAssist extends Module {

    private static final NumberValue angleDifference = new NumberValue("Angle Difference", 70.0, 20.0, 90.0, 0.5);
    private static final BooleanValue onlyWhenClick = new BooleanValue("Only When Click", true);

    public SilentAimAssist() {
        super("Silent Aim Assist", ModuleCategory.GHOST, Keyboard.KEY_NONE, false, true, true);
    }

    @Override
    public void onTick(TickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (!onlyWhenClick.get() && (mc.objectMouseOver == null || mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY))
            return;
        if (onlyWhenClick.get() && !Mouse.isButtonDown(0)) return;

        Entity closest = null;
        float minAngleDiff = angleDifference.get().floatValue();

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity == null || entity == mc.thePlayer || entity.isDead || !(entity instanceof EntityPlayer))
                continue;
            if (!entity.canBeCollidedWith() || !entity.isEntityAlive()) continue;
            if (!mc.thePlayer.canEntityBeSeen(entity) || mc.thePlayer.getDistanceToEntity(entity) > 5f) continue;
            if (DewCommon.moduleManager.getModule(Teams.class).isInYourTeam((EntityLivingBase) entity)) continue;
            if (DewCommon.moduleManager.getModule(Freecam.class).isEnabled() && mc.thePlayer == entity) continue;

            float[] diffs = DewCommon.rotationManager.getAngleDifferenceTo(entity);
            float yawDiff = diffs[0];
            float pitchDiff = diffs[1];

            if (yawDiff <= angleDifference.get() && pitchDiff <= angleDifference.get() * 2f) {
                float totalDiff = yawDiff + pitchDiff;
                if (totalDiff < minAngleDiff) {
                    minAngleDiff = totalDiff;
                    closest = entity;
                }
            }
        }

        if (closest != null) {
            DewCommon.rotationManager.faceEntity(closest, 22f, false, mc.playerController.getBlockReachDistance());
        }
    }
}
