package com.dew.system.module.modules.combat;

import com.dew.DewCommon;
import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.event.events.TickEvent;
import com.dew.system.event.events.WorldEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.module.modules.player.Breaker;
import com.dew.system.module.modules.player.Scaffold;
import com.dew.system.module.modules.render.Animations;
import com.dew.system.rotation.RotationManager;
import com.dew.system.settingsvalue.BooleanValue;
import com.dew.system.settingsvalue.MultiSelectionValue;
import com.dew.system.settingsvalue.NumberValue;
import com.dew.system.settingsvalue.SelectionValue;
import com.dew.utils.BlinkUtil;
import com.dew.utils.LogUtil;
import com.dew.utils.PacketUtil;
import de.florianmichael.viamcp.fixes.AttackOrder;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import org.lwjgl.input.Keyboard;

import java.util.Arrays;
import java.util.Random;

public class KillAura extends Module {

    public KillAura() {
        super("Kill Aura", ModuleCategory.COMBAT, Keyboard.KEY_NONE, false, true, true);
    }

    private static final NumberValue targetRange = new NumberValue("Target Range", 6.0, 0.1, 10.0, 0.1);
    private static final NumberValue attackRange = new NumberValue("Attack Range", 3.0, 0.1, 6.0, 0.1);
    private static final NumberValue maxCps = new NumberValue("Max CPS", 17.0, 0.0, 20.0, 0.1);
    private static final NumberValue minCps = new NumberValue("Min CPS", 5.0, 0.0, 20.0, 0.1);
    private static final NumberValue rotationSpeed = new NumberValue("Rotation Speed", 60.0, 0.0, 180.0, 10.0);
    private static final MultiSelectionValue targets = new MultiSelectionValue("Targets", Arrays.asList("Player", "Mob", "Animal", "Living"), "Player", "Mob", "Animal", "Living", "Dead", "Teammate");
    private static final BooleanValue noHitCheck = new BooleanValue("No Hit Check", false);
    private static final BooleanValue visualAutoBlock = new BooleanValue("Visual Auto Block", true);

    private final Random random = new Random();
    private long lastAttackTime = 0L;
    private long nextAttackDelay = 0L;

    public Entity target = null;
    private boolean targeted = false;

    public boolean isInAutoBlockMode() {
        AutoBlock autoBlockModule = DewCommon.moduleManager.getModule(AutoBlock.class);
        return this.isEnabled() && autoBlockModule.isEnabled() && target != null && autoBlockModule.isHoldingSword();
    }

    @Override
    public void onDisable() {
        this.resetState();
    }

    @Override
    public void onWorld(WorldEvent event) {
        this.setState(false);
    }

    private void resetState() {
        lastAttackTime = 0L;
        nextAttackDelay = 0L;
        target = null;
        targeted = false;
        DewCommon.moduleManager.getModule(Animations.class).setVisualBlocking(false);
    }

    @Override
    public void onTick(TickEvent event) {
        if (this.isInAutoBlockMode()) return;

        this.doMainFunctions(true);
    }

    private boolean rotateToTargetAndIsCanHit() {
        return DewCommon.rotationManager.faceEntity(target, rotationSpeed.get().floatValue());
    }

    private void attack(boolean canHit) {
        if (mc.thePlayer.getDistanceToEntity(target) <= attackRange.get() && (canHit && mc.thePlayer.canEntityBeSeen(target) || noHitCheck.get())) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastAttackTime >= nextAttackDelay) {
                AttackOrder.sendFixedAttack(mc.thePlayer, target);
                mc.thePlayer.onEnchantmentCritical(target);
                lastAttackTime = currentTime;
                nextAttackDelay = getNextAttackDelay();
            }
        }
    }

    public void doMainFunctions(boolean doAttack) {
        if (mc.thePlayer == null || DewCommon.moduleManager.getModule(Scaffold.class).isEnabled() && DewCommon.moduleManager.getModule(Scaffold.class).holdingBlock || DewCommon.moduleManager.getModule(AutoPot.class).isEnabled() && DewCommon.moduleManager.getModule(AutoPot.class).isThrowing() || DewCommon.moduleManager.getModule(Breaker.class).isEnabled() && DewCommon.moduleManager.getModule(Breaker.class).isBreaking) {
            this.resetState();
            return;
        }

        target = getClosestTarget(targetRange.get());
        if (target != null) {
            targeted = true;
            if (visualAutoBlock.get()) {
                DewCommon.moduleManager.getModule(Animations.class).setVisualBlocking(true);
            }
            boolean canHit = this.rotateToTargetAndIsCanHit();
            if (doAttack) {
                this.attack(canHit);
            }
        } else if (targeted) {
            this.resetState();
        }
    }

    private long getNextAttackDelay() {
        double min = minCps.get();
        double max = maxCps.get();
        if (min > max) {
            double temp = min;
            min = max;
            max = temp;
        }
        double cps = min + (max - min) * random.nextDouble();
        return (long) (1000.0 / cps);
    }

    private Entity getClosestTarget(double range) {
        Entity closest = null;
        double closestDist = range;

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityPlayerSP || !this.shouldAttack(entity)) continue;
            double dist = mc.thePlayer.getDistanceToEntity(entity);
            if (dist < closestDist) {
                closestDist = dist;
                closest = entity;
            }
        }

        return closest;
    }

    private boolean shouldAttack(Entity entity) {
        if (entity == null) return false;
        if (!targets.isSelected("Teammate") && entity instanceof EntityLiving && DewCommon.moduleManager.getModule(Teams.class).isInYourTeam((EntityLivingBase) entity)) return false;
        if ((entity.isDead || entity instanceof EntityLiving && ((EntityLiving) entity).deathTime > 0) && !targets.isSelected("Dead")) return false;
        return entity instanceof EntityPlayer && targets.isSelected("Player") || entity instanceof EntityMob && targets.isSelected("Mob") || entity instanceof EntityAnimal && targets.isSelected("Animal") || entity instanceof EntityLiving && targets.isSelected("Living");
    }
}
