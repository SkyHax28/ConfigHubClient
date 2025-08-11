package com.dew.system.module.modules.combat;

import com.dew.DewCommon;
import com.dew.system.event.events.AttackEvent;
import com.dew.system.event.events.TickEvent;
import com.dew.system.event.events.WorldLoadEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.module.modules.exploit.SafetySwitchv2000;
import com.dew.system.module.modules.player.AutoTool;
import com.dew.system.module.modules.player.Breaker;
import com.dew.system.module.modules.player.Freecam;
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
import net.minecraft.item.*;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.WorldSettings;
import org.lwjgl.input.Keyboard;

import java.util.*;

public class Aura extends Module {

    private static final SelectionValue mode = new SelectionValue("Mode", "Single", "Single", "Multi");
    private static final NumberValue maxTargets = new NumberValue("Max Targets", 3.0, 2.0, 8.0, 1.0, () -> mode.get().equals("Multi"));
    private static final NumberValue targetRange = new NumberValue("Target Range", 6.0, 0.1, 10.0, 0.1);
    private static final NumberValue attackRange = new NumberValue("Attack Range", 3.0, 0.1, 6.0, 0.1);
    private static final NumberValue maxCps = new NumberValue("Max CPS", 17.0, 0.0, 20.0, 0.1);
    private static final NumberValue minCps = new NumberValue("Min CPS", 5.0, 0.0, 20.0, 0.1);
    private static final NumberValue rotationSpeed = new NumberValue("Rotation Speed", 60.0, 0.0, 180.0, 5.0);
    private static final MultiSelectionValue targets = new MultiSelectionValue("Targets", Arrays.asList("Player", "Mob", "Animal", "Living"), "Player", "Mob", "Animal", "Living", "Dead", "Teammate");
    private static final BooleanValue noRotationHitCheck = new BooleanValue("No Rotation Hit Check", false);
    private static final BooleanValue throughWalls = new BooleanValue("Through Walls", true);
    private static final BooleanValue visualAutoBlock = new BooleanValue("Visual Auto Block", true);
    private static final BooleanValue autoThrowRodOrBalls = new BooleanValue("Auto Throw Rod or Balls", false, () -> mode.get().equals("Single"));
    private static final BooleanValue autoBlockPlacer = new BooleanValue("Auto Block Placer", false, () -> mode.get().equals("Single"));
    private static final BooleanValue tpAura = new BooleanValue("TP Aura", false);
    private static final NumberValue tpExtendedRange = new NumberValue("TP Extended Range", 50.0, 0.0, 100.0, 1.0, tpAura::get);
    private final Random random = new Random();
    public Entity target = null;
    private long lastAttackTime = 0L;
    private long nextAttackDelay = 0L;
    private long lastThrowTime = 0L;
    private long lastPlaceTime = 0L;
    private boolean targeted = false;

    public Aura() {
        super("Aura", ModuleCategory.COMBAT, Keyboard.KEY_NONE, false, true, true);
    }

    public boolean isInAutoBlockMode() {
        AutoBlock autoBlockModule = DewCommon.moduleManager.getModule(AutoBlock.class);
        return this.isEnabled() && autoBlockModule.isEnabled() && target != null && autoBlockModule.isHoldingSword() && (!autoBlockModule.getMode().equals("Legit") || DewCommon.rotationManager.canHitEntityFromPlayer(target, this.getAttackRange(), throughWalls.get()));
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
    public void onLoadWorld(WorldLoadEvent event) {
        if (DewCommon.moduleManager.getModule(SafetySwitchv2000.class).isEnabled()) {
            this.setState(false);
        }
    }

    private void resetState() {
        lastAttackTime = 0L;
        nextAttackDelay = 0L;
        lastThrowTime = 0L;
        target = null;
        targeted = false;
        DewCommon.moduleManager.getModule(Animations.class).setVisualBlocking(false);
    }

    @Override
    public void onTick(TickEvent event) {
        if (this.isInAutoBlockMode()) return;

        this.doMainFunctions(true);
    }

    private boolean attack(Entity entity, boolean canHit, long currentTime) {
        if (DewCommon.rotationManager.canHitEntityFromPlayer(entity, this.getAttackRange(), throughWalls.get()) && (canHit || noRotationHitCheck.get())) {
            if (currentTime - lastAttackTime >= nextAttackDelay) {
                if (tpAura.get()) {
                    new Thread(() -> {
                        ArrayList<Vec3> paths = DewCommon.pathFinder.computePath(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ), new Vec3(entity.posX, entity.posY, entity.posZ));
                        boolean shouldTp = paths != null && !paths.isEmpty() && mc.thePlayer.getDistanceToEntity(entity) > attackRange.get();

                        if (shouldTp) {
                            for (Vec3 vec : paths) {
                                PacketUtil.sendPacketAsSilent(new C03PacketPlayer.C04PacketPlayerPosition(vec.getX(), vec.getY(), vec.getZ(), true));
                            }
                        }

                        AttackEvent event = new AttackEvent(entity);
                        DewCommon.eventManager.call(event);

                        AttackOrder.sendFixedPacketAttack(mc.thePlayer, entity);

                        if (shouldTp) {
                            Collections.reverse(paths);
                            for (Vec3 vec : paths) {
                                PacketUtil.sendPacketAsSilent(new C03PacketPlayer.C04PacketPlayerPosition(vec.getX(), vec.getY(), vec.getZ(), true));
                            }
                        }
                    }).start();
                } else {
                    AttackOrder.sendFixedAttack(mc.thePlayer, entity);
                }

                return true;
            }
        }

        return false;
    }

    public void doMainFunctions(boolean doAttack) {
        if (mc.thePlayer == null || mc.playerController == null || mc.playerController.getCurrentGameType() == WorldSettings.GameType.SPECTATOR  || DewCommon.moduleManager.getModule(Scaffold.class).isEnabled() || DewCommon.moduleManager.getModule(AutoPot.class).isEnabled() && DewCommon.moduleManager.getModule(AutoPot.class).isThrowing() || DewCommon.moduleManager.getModule(Breaker.class).isEnabled() && DewCommon.moduleManager.getModule(Breaker.class).isBreaking) {
            this.resetState();
            return;
        }

        switch (mode.get().toLowerCase()) {
            case "single":
                target = getHighestThreatTarget(this.getTargetRange());
                if (target != null) {
                    targeted = true;
                    if (autoBlockPlacer.get() && placeDefensiveBlock(target)) {
                        return;
                    }
                    if (visualAutoBlock.get()) {
                        DewCommon.moduleManager.getModule(Animations.class).setVisualBlocking(true);
                    }
                    long currentTime = System.currentTimeMillis();
                    boolean canHit = this.rotateToTargetAndIsCanHit(target);
                    if (doAttack) {
                        boolean threw = false;
                        if (autoThrowRodOrBalls.get()) {
                            threw = this.throwItems(target);
                        }
                        boolean success = this.attack(target, canHit && !threw, currentTime);
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

    private boolean rotateToTargetAndIsCanHit(Entity entity) {
        return DewCommon.rotationManager.faceEntity(entity, rotationSpeed.get().floatValue());
    }

    private double getTargetRange() {
        return targetRange.get() + (tpAura.get() ? tpExtendedRange.get() : 0.0);
    }

    private double getAttackRange() {
        return attackRange.get() + (tpAura.get() ? tpExtendedRange.get() : 0.0);
    }

    private boolean throwItems(Entity entity) {
        long now = System.currentTimeMillis();
        boolean swapBack = mc.thePlayer.fishEntity != null;
        long delay = swapBack && mc.thePlayer.fishEntity == target ? 200L : swapBack ? 400L : 700L;

        if (mc.thePlayer.getDistanceToEntity(entity) <= targetRange.get() && mc.thePlayer.canEntityBeSeen(target) && now - lastThrowTime >= delay) {
            int originalSlot = mc.thePlayer.inventory.currentItem;
            for (int i = 0; i < 9; i++) {
                if (mc.thePlayer.inventory.getStackInSlot(i) != null) {
                    Item item = mc.thePlayer.inventory.getStackInSlot(i).getItem();
                    if (item instanceof ItemSnowball || item instanceof ItemEgg || item instanceof ItemFishingRod) {
                        DewCommon.moduleManager.getModule(AutoTool.class).doNotUpdateSwordNow(true);
                        if (mc.thePlayer.inventory.currentItem != i) {
                            mc.thePlayer.inventory.currentItem = i;
                            mc.playerController.updateController();
                        }
                        if (mc.thePlayer.inventory.getCurrentItem() != null && mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getCurrentItem())) {
                            mc.entityRenderer.itemRenderer.resetEquippedProgress2();
                        }
                        lastThrowTime = now;
                        new Thread(() -> {
                            try {
                                Thread.sleep(250);
                            } catch (InterruptedException ignored) {}
                            if (mc.thePlayer.inventory.currentItem != originalSlot) {
                                mc.thePlayer.inventory.currentItem = originalSlot;
                                mc.playerController.updateController();
                            }
                            DewCommon.moduleManager.getModule(AutoTool.class).doNotUpdateSwordNow(false);
                        }).start();
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private double getThreatScore(EntityLivingBase entity) {
        double score = 0;
        double dist = mc.thePlayer.getDistanceToEntity(entity);
        score -= dist;

        ItemStack held = entity.getHeldItem();
        if (held != null && held.getItem() instanceof ItemSword) score += 10;
        if (entity.getTotalArmorValue() > 10) score += 5;
        if (entity.getHealth() > 15) score += 3;
        return score;
    }

    private Entity getHighestThreatTarget(double range) {
        EntityLivingBase best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (shouldNotAttack(entity) || entity instanceof EntityPlayerSP) continue;
            if (entity instanceof EntityLivingBase && mc.thePlayer.getDistanceToEntity(entity) <= range) {
                double score = getThreatScore((EntityLivingBase) entity);
                if (score > bestScore) {
                    bestScore = score;
                    best = (EntityLivingBase) entity;
                }
            }
        }
        return best;
    }

    private boolean placeDefensiveBlock(Entity entity) {
        if (System.currentTimeMillis() - lastPlaceTime < 400) return false;
        double dist = mc.thePlayer.getDistanceToEntity(entity);
        if (dist < 2.5 || dist > 3.0) return false;

        int blockSlot = getHotbarBlockSlot();
        if (blockSlot == -1) return false;

        double dx = entity.posX - mc.thePlayer.posX;
        double dz = entity.posZ - mc.thePlayer.posZ;

        double len = Math.sqrt(dx * dx + dz * dz);
        if (len == 0) return false;
        dx /= len;
        dz /= len;

        double placeX = entity.posX - dx;
        double placeZ = entity.posZ - dz;
        double placeY = Math.floor(entity.posY);

        BlockPos pos = new BlockPos(placeX, placeY, placeZ);

        EnumFacing placeFacing = null;
        BlockPos neighbor = null;
        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos adj = pos.offset(facing);
            if (!mc.theWorld.isAirBlock(adj)) {
                placeFacing = facing.getOpposite();
                neighbor = adj;
                break;
            }
        }

        if (placeFacing == null || neighbor == null) return false;

        int originalSlot = mc.thePlayer.inventory.currentItem;
        EnumFacing opposite = placeFacing.getOpposite();

        double hitX = neighbor.getX() + 0.5 + 0.5 * opposite.getFrontOffsetX();
        double hitY = neighbor.getY() + 0.5 + 0.5 * opposite.getFrontOffsetY();
        double hitZ = neighbor.getZ() + 0.5 + 0.5 * opposite.getFrontOffsetZ();
        net.minecraft.util.Vec3 hitVec = new net.minecraft.util.Vec3(hitX, hitY, hitZ);

        if (DewCommon.rotationManager.faceBlockWithFacing(neighbor, placeFacing, rotationSpeed.get().floatValue(), true)) {
            DewCommon.moduleManager.getModule(AutoTool.class).doNotUpdateSwordNow(true);
            if (mc.thePlayer.inventory.currentItem != blockSlot) {
                mc.thePlayer.inventory.currentItem = blockSlot;
                mc.playerController.updateController();
            }
            ItemStack itemstack = mc.thePlayer.inventory.getCurrentItem();
            if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, itemstack, neighbor, placeFacing, hitVec)) {
                mc.thePlayer.swingItem();
                if (itemstack != null) {
                    if (itemstack.stackSize == 0) {
                        mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.currentItem] = null;
                    } else if (mc.playerController.isInCreativeMode()) {
                        mc.entityRenderer.itemRenderer.resetEquippedProgress();
                    }
                }
            }

            lastPlaceTime = System.currentTimeMillis();

            new Thread(() -> {
                try {
                    Thread.sleep(80);
                } catch (InterruptedException ignored) {}
                if (mc.thePlayer.inventory.currentItem != originalSlot) {
                    mc.thePlayer.inventory.currentItem = originalSlot;
                    mc.playerController.updateController();
                }
                DewCommon.moduleManager.getModule(AutoTool.class).doNotUpdateSwordNow(false);
            }).start();

            return true;
        }

        return false;
    }

    private int getHotbarBlockSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() instanceof ItemBlock) {
                return i;
            }
        }
        return -1;
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
            if (entity instanceof EntityPlayerSP || this.shouldNotAttack(entity)) continue;
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

    private boolean shouldNotAttack(Entity entity) {
        if (entity == null || DewCommon.moduleManager.getModule(Freecam.class).isEnabled() && mc.thePlayer == entity)
            return true;
        if (!targets.isSelected("Teammate") && entity instanceof EntityLivingBase && DewCommon.moduleManager.getModule(Teams.class).isInYourTeam((EntityLivingBase) entity))
            return true;
        if ((entity.isDead || entity instanceof EntityLiving && !entity.isEntityAlive()) && !entity.isDead && !targets.isSelected("Dead"))
            return true;
        return !(entity instanceof EntityPlayer && targets.isSelected("Player") || entity instanceof EntityMob && targets.isSelected("Mob") || entity instanceof EntityAnimal && targets.isSelected("Animal") || entity instanceof EntityLiving && targets.isSelected("Living"));
    }
}
