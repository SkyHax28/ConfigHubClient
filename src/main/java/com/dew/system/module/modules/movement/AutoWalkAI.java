package com.dew.system.module.modules.movement;

import com.dew.DewCommon;
import com.dew.system.event.events.*;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.module.modules.combat.AutoBlock;
import com.dew.system.module.modules.combat.AutoPot;
import com.dew.system.module.modules.combat.Teams;
import com.dew.system.module.modules.player.Breaker;
import com.dew.system.module.modules.player.Scaffold;
import com.dew.system.module.modules.render.Animations;
import com.dew.system.settingsvalue.BooleanValue;
import com.dew.system.settingsvalue.MultiSelectionValue;
import com.dew.system.settingsvalue.NumberValue;
import com.dew.system.settingsvalue.SelectionValue;
import com.dew.utils.PacketUtil;
import com.dew.utils.pathfinder.Vec3;
import de.florianmichael.viamcp.fixes.AttackOrder;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C03PacketPlayer;
import org.lwjgl.input.Keyboard;

import java.util.*;

public class AutoWalkAI extends Module {

    public AutoWalkAI() {
        super("Auto Walk AI", ModuleCategory.MOVEMENT, Keyboard.KEY_NONE, false, true, true);
    }

    public Entity target = null;
    private boolean targeted = false;

    @Override
    public void onDisable() {
        this.resetState();
    }

    @Override
    public void onLoadWorld(WorldLoadEvent event) {
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
