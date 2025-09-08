package com.dew.system.module.modules.movement;

import com.dew.DewCommon;
import com.dew.system.event.events.AttackEvent;
import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.event.events.LoadWorldEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import net.minecraft.entity.Entity;
import org.lwjgl.input.Keyboard;

public class AutoWalkAI extends Module {

    public Entity target = null;
    private boolean targeted = false;

    public AutoWalkAI() {
        super("Auto Walk AI", ModuleCategory.MOVEMENT, Keyboard.KEY_NONE, false, true, true);
    }

    @Override
    public void onDisable() {
        this.resetState();
    }

    @Override
    public void onLoadWorld(LoadWorldEvent event) {
        this.setState(false);
    }

    private void resetState() {
        target = null;
        targeted = false;
        DewCommon.smartPlayerNavigator.stop();
    }

    @Override
    public void onAttack(AttackEvent event) {
        target = event.target;
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (target != null && mc.thePlayer.getDistanceToEntityIgnoringY(target) < 30f) {
            DewCommon.smartPlayerNavigator.startPathTo(target);
            DewCommon.smartPlayerNavigator.onUpdate();
            targeted = true;
        } else if (targeted) {
            DewCommon.smartPlayerNavigator.stop();
            targeted = false;
        }
    }
}
