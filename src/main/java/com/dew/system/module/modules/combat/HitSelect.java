package com.dew.system.module.modules.combat;

import com.dew.DewCommon;
import com.dew.system.event.events.AttackEvent;
import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.NumberValue;
import com.dew.system.settingsvalue.SelectionValue;
import net.minecraft.client.entity.EntityPlayerSP;
import org.lwjgl.input.Keyboard;


public class HitSelect extends Module {
    public static SelectionValue mode = new SelectionValue("Mode", "Active", "Active", "Pause");
    public static SelectionValue preference = new SelectionValue("Preference", "Reduce", "Movement", "Reduce", "Critical");
    private static final NumberValue chance = new NumberValue("Chance", 80, 10, 100, 1);
    private static final NumberValue threshold = new NumberValue("Threshold", 400, 300, 500, 1);
    private long lastAttackTime = -1;
    private boolean currentShouldAttack = false;

    public HitSelect() {
        super("Hit Select", ModuleCategory.COMBAT, Keyboard.KEY_NONE, false, true, true);
    }
    public boolean canAttack() {
        boolean canAttack = currentShouldAttack;

        if (!DewCommon.moduleManager.getModule(HitSelect.class).isEnabled() || mode.get().equals("Active")) {
            canAttack = true;
        }

        return canAttack;
    }
    @Override
    public void onAttack(AttackEvent event) {
        if (mode.get().equals("Active") && !currentShouldAttack) {
            event.cancel();
            return;
        }
        if (canAttack()) {
            lastAttackTime = System.currentTimeMillis();
        }
    }
    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        currentShouldAttack = false;
        EntityPlayerSP player = mc.thePlayer;
        if (player == null) return;
        if (Math.random() * 100 > chance.get()) {
            currentShouldAttack = true;
        } else {
            switch (preference.get().toLowerCase()) {
                case "movement":
                    double dx = player.posX - player.prevPosX;
                    double dz = player.posZ - player.prevPosZ;
                    double speed = Math.sqrt(dx * dx + dz * dz);
                    currentShouldAttack = speed > 0.1; // threshold, adjust as needed
                    break;

                case "reduce":
                    currentShouldAttack = player.hurtTime > 0 && !player.onGround;
                    break;

                case "critical":
                    currentShouldAttack = !player.onGround && player.motionY < 0;
                    break;
            }
            if (!currentShouldAttack) {
                currentShouldAttack = System.currentTimeMillis() - lastAttackTime >= threshold.get();
            }
        }
    }
}
