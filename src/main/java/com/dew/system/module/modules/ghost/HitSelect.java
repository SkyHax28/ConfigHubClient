package com.dew.system.module.modules.ghost;

import com.dew.system.event.events.AttackEvent;
import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.event.events.WorldLoadEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.NumberValue;
import com.dew.system.settingsvalue.SelectionValue;
import org.lwjgl.input.Keyboard;


public class HitSelect extends Module {

    public static SelectionValue mode = new SelectionValue("Mode", "Active", "Active", "Pause");
    public static SelectionValue preference = new SelectionValue("Preference", "Reduce", "Movement", "Reduce", "Critical");
    private static final NumberValue chance = new NumberValue("Chance", 80.0, 10.0, 100.0, 1.0);
    private static final NumberValue threshold = new NumberValue("Threshold", 400.0, 300.0, 500.0, 1.0);
    private long lastAttackTime = -1;
    private boolean currentShouldAttack = false;
    public HitSelect() {
        super("Hit Select", ModuleCategory.GHOST, Keyboard.KEY_NONE, false, true, true);
    }

    @Override
    public String tag() {
        return preference.get();
    }

    @Override
    public void onDisable() {
        this.resetState();
    }

    @Override
    public void onLoadWorld(WorldLoadEvent event) {
        this.resetState();
    }

    private void resetState() {
        lastAttackTime = -1;
        currentShouldAttack = false;
    }

    @Override
    public void onAttack(AttackEvent event) {
        if (mode.get().equals("Active") && !currentShouldAttack) {
            event.cancel();
            return;
        }

        if (mode.get().equals("Active") || currentShouldAttack) {
            lastAttackTime = System.currentTimeMillis();
        }
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer == null) return;

        currentShouldAttack = false;

        if (Math.random() * 100 > chance.get()) {
            currentShouldAttack = true;
        } else {
            switch (preference.get().toLowerCase()) {
                case "movement":
                    double dx = mc.thePlayer.posX - mc.thePlayer.prevPosX;
                    double dz = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
                    double speed = Math.sqrt(dx * dx + dz * dz);
                    currentShouldAttack = speed > 0.1;
                    break;

                case "reduce":
                    currentShouldAttack = mc.thePlayer.hurtTime > 0 && !mc.thePlayer.onGround;
                    break;

                case "critical":
                    currentShouldAttack = !mc.thePlayer.onGround && mc.thePlayer.motionY < 0;
                    break;
            }

            if (!currentShouldAttack) {
                currentShouldAttack = System.currentTimeMillis() - lastAttackTime >= threshold.get();
            }
        }
    }
}
