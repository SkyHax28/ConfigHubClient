package com.dew.system.module.modules.ghost;

import com.dew.DewCommon;
import com.dew.system.event.events.TickEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.module.modules.combat.Teams;
import com.dew.system.module.modules.player.Freecam;
import com.dew.system.settingsvalue.NumberValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Keyboard;

public class SilentAimAssist extends Module {

    public SilentAimAssist() {
        super("Silent Aim Assist", ModuleCategory.GHOST, Keyboard.KEY_NONE, false, true, true);
    }

    private static final NumberValue activateAngleDifference = new NumberValue("Activate Angle Difference", 25.0, 20.0, 90.0, 0.5);

    @Override
    public void onTick(TickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null || mc.objectMouseOver == null || mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) return;

        Entity closest = null;
        float minAngleDiff = activateAngleDifference.get().floatValue();

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity == null || entity == mc.thePlayer || entity.isDead || !(entity instanceof EntityPlayer)) continue;
            if (!entity.canBeCollidedWith() || !entity.isEntityAlive()) continue;
            if (!mc.thePlayer.canEntityBeSeen(entity) || mc.thePlayer.getDistanceToEntity(entity) > 5f) continue;
            if (DewCommon.moduleManager.getModule(Teams.class).isInYourTeam((EntityLivingBase) entity)) continue;
            if (DewCommon.moduleManager.getModule(Freecam.class).isEnabled() && mc.thePlayer == entity) continue;

            float[] diffs = DewCommon.rotationManager.getAngleDifferenceTo(entity);
            float yawDiff = diffs[0];
            float pitchDiff = diffs[1];

            if (yawDiff <= activateAngleDifference.get() && pitchDiff <= activateAngleDifference.get()) {
                float totalDiff = yawDiff + pitchDiff;
                if (totalDiff < minAngleDiff) {
                    minAngleDiff = totalDiff;
                    closest = entity;
                }
            }
        }

        if (closest != null) {
            DewCommon.rotationManager.faceEntity(closest, 22f);
        }
    }
}
