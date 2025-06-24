package com.dew.system.module.modules.combat;

import com.dew.DewCommon;
import com.dew.system.event.events.TickEvent;
import com.dew.system.event.events.WorldEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.module.modules.player.Breaker;
import com.dew.system.module.modules.player.Scaffold;
import com.dew.system.module.modules.render.Animations;
import com.dew.system.settingsvalue.BooleanValue;
import com.dew.system.settingsvalue.MultiSelectionValue;
import com.dew.system.settingsvalue.NumberValue;
import com.dew.system.settingsvalue.SelectionValue;
import com.dew.utils.LogUtil;
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

public class KillAura extends Module {

    public KillAura() {
        super("Kill Aura", ModuleCategory.COMBAT, Keyboard.KEY_NONE, false, true, true);
    }

    private static final SelectionValue mode = new SelectionValue("Mode", "Single", "Single", "Multi");
    private static final NumberValue maxTargets = new NumberValue("Max Targets", 3.0, 2.0, 8.0, 1.0, () -> mode.get().equals("Multi"));
    private static final NumberValue targetRange = new NumberValue("Target Range", 6.0, 0.1, 10.0, 0.1);
    private static final NumberValue attackRange = new NumberValue("Attack Range", 3.0, 0.1, 6.0, 0.1);
    private static final NumberValue maxCps = new NumberValue("Max CPS", 17.0, 0.0, 20.0, 0.1);
    private static final NumberValue minCps = new NumberValue("Min CPS", 5.0, 0.0, 20.0, 0.1);
    private static final NumberValue rotationSpeed = new NumberValue("Rotation Speed", 60.0, 0.0, 180.0, 10.0);
    private static final MultiSelectionValue targets = new MultiSelectionValue("Targets", Arrays.asList("Player", "Mob", "Animal", "Living"), "Player", "Mob", "Animal", "Living", "Dead", "Teammate");
    private static final BooleanValue noHitCheck = new BooleanValue("No Hit Check", false);
    private static final BooleanValue visualAutoBlock = new BooleanValue("Visual Auto Block", true);
    private static final BooleanValue tpAura = new BooleanValue("TP Aura", false);
    private static final NumberValue tpExtendedRange = new NumberValue("TP Extended Range", 50.0, 0.0, 100.0, 1.0, tpAura::get);

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
    public String tag() {
        return mode.get();
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

    private boolean rotateToTargetAndIsCanHit(Entity entity) {
        return DewCommon.rotationManager.faceEntity(entity, rotationSpeed.get().floatValue());
    }

    private double getTargetRange() {
        return targetRange.get() + (tpAura.get() ? tpExtendedRange.get() : 0.0);
    }

    private double getAttackRange() {
        return attackRange.get() + (tpAura.get() ? tpExtendedRange.get() : 0.0);
    }

    private boolean attack(Entity entity, boolean canHit, long currentTime) {
        if (mc.thePlayer.getDistanceToEntity(entity) <= this.getAttackRange() && (canHit && mc.thePlayer.canEntityBeSeen(entity) || noHitCheck.get())) {
            if (currentTime - lastAttackTime >= nextAttackDelay) {
                if (tpAura.get()) {
                    new Thread(() -> {
                        ArrayList<Vec3> paths = DewCommon.pathFinder.computePath(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ), new Vec3(entity.posX, entity.posY, entity.posZ));
                        boolean shouldTp = paths != null && !paths.isEmpty() && mc.thePlayer.getDistanceToEntity(entity) > attackRange.get();

                        if (shouldTp) {
                            for (Vec3 vec : paths) {
                                PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(vec.getX(), vec.getY(), vec.getZ(), true));
                            }
                        }

                        AttackOrder.sendFixedPacketAttack(mc.thePlayer, entity);

                        if (shouldTp) {
                            Collections.reverse(paths);
                            for (Vec3 vec : paths) {
                                PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(vec.getX(), vec.getY(), vec.getZ(), true));
                            }
                        }
                    }).start();
                } else {
                    AttackOrder.sendFixedAttack(mc.thePlayer, entity);
                    mc.thePlayer.onEnchantmentCritical(entity);
                }

                return true;
            }
        }

        return false;
    }

    public void doMainFunctions(boolean doAttack) {
        if (mc.thePlayer == null || DewCommon.moduleManager.getModule(Scaffold.class).isEnabled() && DewCommon.moduleManager.getModule(Scaffold.class).holdingBlock || DewCommon.moduleManager.getModule(AutoPot.class).isEnabled() && DewCommon.moduleManager.getModule(AutoPot.class).isThrowing() || DewCommon.moduleManager.getModule(Breaker.class).isEnabled() && DewCommon.moduleManager.getModule(Breaker.class).isBreaking) {
            this.resetState();
            return;
        }

        switch (mode.get().toLowerCase()) {
            case "single":
                target = getClosestTarget(this.getTargetRange());
                if (target != null) {
                    targeted = true;
                    if (visualAutoBlock.get()) {
                        DewCommon.moduleManager.getModule(Animations.class).setVisualBlocking(true);
                    }
                    long currentTime = System.currentTimeMillis();
                    boolean canHit = this.rotateToTargetAndIsCanHit(target);
                    if (doAttack) {
                        boolean success = this.attack(target, canHit, currentTime);
                        if (success) {
                            lastAttackTime = currentTime;
                            nextAttackDelay = getNextAttackDelay();
                        }
                    }
                } else if (targeted) {
                    this.resetState();
                }
                break;

            case "multi":
                List<Entity> targetsList = getTargetsInRange(this.getTargetRange(), maxTargets.get().intValue());

                if (!targetsList.isEmpty()) {
                    targeted = true;
                    if (visualAutoBlock.get()) {
                        DewCommon.moduleManager.getModule(Animations.class).setVisualBlocking(true);
                    }

                    target = targetsList.get(0);
                    long currentTime = System.currentTimeMillis();
                    this.rotateToTargetAndIsCanHit(target);

                    boolean attacked = false;

                    for (Entity entity : targetsList) {
                        if (doAttack) {
                            if (this.attack(entity, true, currentTime)) {
                                attacked = true;
                            }
                        }
                    }

                    if (attacked) {
                        lastAttackTime = currentTime;
                        nextAttackDelay = getNextAttackDelay();
                    }
                } else if (targeted) {
                    this.resetState();
                }
                break;
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

    private List<Entity> getTargetsInRange(double range, int maxTargets) {
        List<Entity> targets = new ArrayList<>();

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityPlayerSP || !this.shouldNotAttack(entity)) continue;
            double dist = mc.thePlayer.getDistanceToEntity(entity);
            if (dist <= range) {
                targets.add(entity);
            }
        }

        targets.sort(Comparator.comparingDouble(e -> mc.thePlayer.getDistanceToEntity(e)));

        if (targets.size() > maxTargets) {
            return targets.subList(0, maxTargets);
        }

        return targets;
    }

    private Entity getClosestTarget(double range) {
        Entity closest = null;
        double closestDist = range;

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityPlayerSP || !this.shouldNotAttack(entity)) continue;
            double dist = mc.thePlayer.getDistanceToEntity(entity);
            if (dist < closestDist) {
                closestDist = dist;
                closest = entity;
            }
        }

        return closest;
    }

    private boolean shouldNotAttack(Entity entity) {
        if (entity == null) return false;
        if (!targets.isSelected("Teammate") && entity instanceof EntityLiving && DewCommon.moduleManager.getModule(Teams.class).isInYourTeam((EntityLivingBase) entity)) return false;
        if ((entity.isDead || entity instanceof EntityLiving && ((EntityLiving) entity).deathTime > 0) && !targets.isSelected("Dead")) return false;
        return entity instanceof EntityPlayer && targets.isSelected("Player") || entity instanceof EntityMob && targets.isSelected("Mob") || entity instanceof EntityAnimal && targets.isSelected("Animal") || entity instanceof EntityLiving && targets.isSelected("Living");
    }
}
