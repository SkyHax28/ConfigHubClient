package com.dew.system.module.modules.combat;

import com.dew.DewCommon;
import com.dew.system.event.events.*;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.module.modules.exploit.SafetySwitchv2000;
import com.dew.system.module.modules.movement.flight.FlightModule;
import com.dew.system.module.modules.movement.speed.SpeedModule;
import com.dew.system.module.modules.movement.speed.speeds.VerusSpeed;
import com.dew.system.module.modules.player.AutoTool;
import com.dew.system.module.modules.player.Breaker;
import com.dew.system.module.modules.player.Freecam;
import com.dew.system.module.modules.player.Scaffold;
import com.dew.system.module.modules.visual.Animations;
import com.dew.system.settingsvalue.BooleanValue;
import com.dew.system.settingsvalue.MultiSelectionValue;
import com.dew.system.settingsvalue.NumberValue;
import com.dew.system.settingsvalue.SelectionValue;
import com.dew.utils.*;
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
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.WorldSettings;
import org.lwjgl.input.Keyboard;

import java.util.*;

public class Aura extends Module {

    private static final SelectionValue mode = new SelectionValue("Mode", "Single", "Single", "Multi");
    private static final SelectionValue swingMode = new SelectionValue("Swing Mode", "Normal", "Normal", "Packet");
    private static final SelectionValue attackTiming = new SelectionValue("Attack Timing", "Legit", "Legit", "Pre Update", "Post Update", "Pre Motion", "Post Motion");
    private static final NumberValue maxTargets = new NumberValue("Max Targets", 3.0, 2.0, 8.0, 1.0, () -> mode.get().equals("Multi"));
    private static final NumberValue targetRange = new NumberValue("Target Range", 6.0, 0.1, 10.0, 0.1);
    private static final NumberValue attackRange = new NumberValue("Attack Range", 3.0, 0.1, 6.0, 0.1);
    private static final NumberValue maxCps = new NumberValue("Max CPS", 17.0, 0.0, 20.0, 0.1);
    private static final NumberValue minCps = new NumberValue("Min CPS", 5.0, 0.0, 20.0, 0.1);
    private static final NumberValue rotationSpeed = new NumberValue("Rotation Speed", 60.0, 0.0, 180.0, 5.0);
    private static final MultiSelectionValue targets = new MultiSelectionValue("Targets", Arrays.asList("Player", "Mob", "Animal", "Living"), "Player", "Mob", "Animal", "Living", "Dead", "Teammate");
    private static final BooleanValue reducedPitchRotation = new BooleanValue("Reduced Pitch Rotation", true);
    private static final BooleanValue noRotationHitCheck = new BooleanValue("No Rotation Hit Check", false);
    private static final BooleanValue throughWalls = new BooleanValue("Through Walls", true);
    private static final BooleanValue visualAutoBlock = new BooleanValue("Visual Auto Block", true);
    private static final BooleanValue tickBase = new BooleanValue("Tick Base", false);
    private static final NumberValue tickDelay = new NumberValue("Tick Delay", 80.0, 0.0, 200.0, 1.0, tickBase::get);
    private static final BooleanValue autoThrowRodOrBalls = new BooleanValue("Auto Throw Rod or Balls", false, () -> mode.get().equals("Single"));
    private static final BooleanValue autoBlockPlacer = new BooleanValue("Auto Block Placer", false, () -> mode.get().equals("Single"));
    private static final BooleanValue tpAura = new BooleanValue("TP Aura", false);
    private static final NumberValue tpExtendedRange = new NumberValue("TP Extended Range", 50.0, 0.0, 100.0, 1.0, tpAura::get);
    private static final BooleanValue objectCheck = new BooleanValue("Object Check", false, () -> !tpAura.get());
    private final Random random = new Random();
    public Entity target = null;
    private long lastAttackTime = 0L;
    private long nextAttackDelay = 0L;
    private long lastThrowTime = 0L;
    private long lastPlaceTime = 0L;
    private int placeableTick = 0;
    private int tickableTick = 0;
    private float currentTimerSpeed = 1.0f;
    private boolean burstNextTick = false;
    private boolean slowNextTick = false;
    private boolean targeted = false;
    private int inventorySwapBackTicks = -1;
    private int inventorySwapBackSlot = -1;
    private boolean restoringInventory = false;

    public Aura() {
        super("Aura", ModuleCategory.COMBAT, Keyboard.KEY_NONE, false, true, true);
    }

    public boolean isInAutoBlockMode() {
        AutoBlock autoBlockModule = DewCommon.moduleManager.getModule(AutoBlock.class);
        return this.isEnabled() && autoBlockModule.isEnabled() && target != null && autoBlockModule.isHoldingSword() && (!autoBlockModule.getMode().equals("Legit") || AaBbUtil.distanceTo(mc.thePlayer.getPositionEyes(1f), target.getEntityBoundingBox()) <= this.getAttackRange());
    }

    @Override
    public String tag() {
        return mode.get() + " " + attackTiming.get();
    }

    @Override
    public void onDisable() {
        this.resetState();
        if (inventorySwapBackSlot >= 0 && mc.thePlayer.inventory.currentItem != inventorySwapBackSlot) {
            mc.thePlayer.inventory.currentItem = inventorySwapBackSlot;
            mc.playerController.updateController();
        }
        DewCommon.moduleManager.getModule(AutoTool.class).doNotUpdateSwordNow(false);
        restoringInventory = false;
        inventorySwapBackTicks = -1;
        inventorySwapBackSlot = -1;
        if (currentTimerSpeed != 1f) {
            TimerUtil.resetTimerSpeed();
            currentTimerSpeed = 1f;
        }
        burstNextTick = false;
        slowNextTick = false;
        placeableTick = 0;
        tickableTick = 0;
    }

    @Override
    public void onLoadWorld(LoadWorldEvent event) {
        if (DewCommon.moduleManager.getModule(SafetySwitchv2000.class).isEnabled()) {
            this.setState(false);
        } else {
            this.resetState();
            if (inventorySwapBackSlot >= 0 && mc.thePlayer.inventory.currentItem != inventorySwapBackSlot) {
                mc.thePlayer.inventory.currentItem = inventorySwapBackSlot;
                mc.playerController.updateController();
            }
            DewCommon.moduleManager.getModule(AutoTool.class).doNotUpdateSwordNow(false);
            restoringInventory = false;
            inventorySwapBackTicks = -1;
            inventorySwapBackSlot = -1;
            if (currentTimerSpeed != 1f) {
                TimerUtil.resetTimerSpeed();
                currentTimerSpeed = 1f;
            }
            burstNextTick = false;
            slowNextTick = false;
            placeableTick = 0;
            tickableTick = 0;
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

    public String getAttackTiming() {
        return attackTiming.get();
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (attackTiming.get().equals("Pre Update")) {
            this.localFunc();
        }
    }

    @Override
    public void onPostUpdate(PostUpdateEvent event) {
        if (attackTiming.get().equals("Post Update")) {
            this.localFunc();
        }
    }

    @Override
    public void onPreMotion(PreMotionEvent event) {
        if (attackTiming.get().equals("Pre Motion")) {
            this.localFunc();
        }
    }

    @Override
    public void onPostMotion(PostMotionEvent event) {
        if (attackTiming.get().equals("Post Motion")) {
            this.localFunc();
        }
    }

    @Override
    public void onTick(TickEvent event) {
        if (attackTiming.get().equals("Legit")) {
            this.localFunc();
        }
    }

    private void localFunc() {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        this.updateSlotSwapper();

        if (this.isInAutoBlockMode()) return;

        this.doMainFunctions(true);
    }

    private boolean attack(Entity entity, boolean canHit, long currentTime) {
        if ((AaBbUtil.distanceTo(mc.thePlayer.getPositionEyes(1f), DewCommon.moduleManager.getModule(PingReach.class).getBestBacktrackEntity(entity).getEntityBoundingBox()) <= this.getAttackRange() || AaBbUtil.distanceTo(mc.thePlayer.getPositionEyes(1f), entity.getEntityBoundingBox()) <= this.getAttackRange()) && (canHit || noRotationHitCheck.get())) {
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

                        AttackOrder.sendFixedPacketAttack(mc.thePlayer, entity, !swingMode.get().equals("Normal"));

                        if (shouldTp) {
                            Collections.reverse(paths);
                            for (Vec3 vec : paths) {
                                PacketUtil.sendPacketAsSilent(new C03PacketPlayer.C04PacketPlayerPosition(vec.getX(), vec.getY(), vec.getZ(), true));
                            }
                        }
                    }).start();
                } else {
                    if (objectCheck.get()) {
                        if (mc.objectMouseOver != null && mc.objectMouseOver.entityHit != null && mc.objectMouseOver.entityHit == entity && !mc.thePlayer.isUsingItem()) {
                            AttackOrder.sendFixedAttack(mc.thePlayer, mc.objectMouseOver.entityHit, !swingMode.get().equals("Normal"));
                            mc.thePlayer.onEnchantmentCritical(mc.objectMouseOver.entityHit);
                        }
                    } else {
                        AttackOrder.sendFixedAttack(mc.thePlayer, entity, !swingMode.get().equals("Normal"));
                        mc.thePlayer.onEnchantmentCritical(entity);
                    }
                }

                return true;
            }
        }

        return false;
    }

    public void updateSlotSwapper() {
        if (inventorySwapBackTicks >= 0) {
            inventorySwapBackTicks--;
            if (inventorySwapBackTicks == 0) {
                if (inventorySwapBackSlot >= 0 && mc.thePlayer.inventory.currentItem != inventorySwapBackSlot) {
                    mc.thePlayer.inventory.currentItem = inventorySwapBackSlot;
                    mc.playerController.updateController();
                }
                DewCommon.moduleManager.getModule(AutoTool.class).doNotUpdateSwordNow(false);
                restoringInventory = false;
                inventorySwapBackTicks = -1;
                inventorySwapBackSlot = -1;
            }
        }
    }

    private void scheduleInventoryRestore(int originalSlot, int delayMs) {
        DewCommon.moduleManager.getModule(AutoTool.class).doNotUpdateSwordNow(true);
        restoringInventory = true;
        inventorySwapBackSlot = originalSlot;
        inventorySwapBackTicks = Math.max(1, delayMs / 50);
    }

    public void doMainFunctions(boolean doAttack) {
        placeableTick = target != null ? placeableTick + 1 : 0;

        if (tickBase.get() && target != null && target instanceof EntityLivingBase && mc.thePlayer.canEntityBeSeen(target) && tickableTick == 0) {
            adjustTimerRange((EntityLivingBase) target);
        } else if (currentTimerSpeed != 1f) {
            slowlyReturnToNormal();
            if (tickableTick > 0) {
                tickableTick--;
            }
        } else {
            if (tickableTick > 0) {
                tickableTick--;
            }
        }

        if (mc.thePlayer == null || mc.playerController == null || mc.playerController.getCurrentGameType() == WorldSettings.GameType.SPECTATOR || DewCommon.moduleManager.getModule(Scaffold.class).isEnabled() || DewCommon.moduleManager.getModule(FlightModule.class).isEnabled() && DewCommon.moduleManager.getModule(FlightModule.class).getMode().equals("Verus") || DewCommon.moduleManager.getModule(AutoPot.class).isEnabled() && DewCommon.moduleManager.getModule(AutoPot.class).isThrowing() || DewCommon.moduleManager.getModule(Breaker.class).isEnabled() && DewCommon.moduleManager.getModule(Breaker.class).isBreaking) {
            this.resetState();
            return;
        }

        boolean setBlocker = mc.thePlayer.isSwingInProgress || swingMode.get().equals("Packet");

        switch (mode.get().toLowerCase()) {
            case "single":
                target = getHighestThreatTarget(this.getTargetRange());
                if (target != null) {
                    targeted = true;
                    if (autoBlockPlacer.get() && placeableTick <= 30 && placeDefensiveBlock(target) || DewCommon.moduleManager.getModule(SpeedModule.class).isEnabled() && DewCommon.moduleManager.getModule(SpeedModule.class).getMode().equals("Verus") && VerusSpeed.dontAttack() && MovementUtil.hasMotionHorizontal()) {
                        return;
                    }
                    if (visualAutoBlock.get()) {
                        DewCommon.moduleManager.getModule(Animations.class).setVisualBlocking(setBlocker);
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
                        DewCommon.moduleManager.getModule(Animations.class).setVisualBlocking(setBlocker);
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
        return DewCommon.rotationManager.faceEntity(entity, rotationSpeed.get().floatValue(), false, reducedPitchRotation.get(), mc.playerController.getBlockReachDistance());
    }

    private double getTargetRange() {
        return targetRange.get() + (tpAura.get() ? tpExtendedRange.get() : 0.0);
    }

    private double getAttackRange() {
        return attackRange.get() + (tpAura.get() ? tpExtendedRange.get() : 0.0);
    }

    private void adjustTimerRange(EntityLivingBase target) {
        double dist = mc.thePlayer.getDistanceToEntity(target);

        if (!burstNextTick && !slowNextTick && dist <= getAttackRange() + 0.15 && dist > getAttackRange() - 0.55) {
            burstNextTick = true;
        }

        if (burstNextTick) {
            currentTimerSpeed = 8f;
            TimerUtil.setTimerSpeed(currentTimerSpeed);
            burstNextTick = false;
            slowNextTick = true;
            return;
        }

        if (slowNextTick) {
            currentTimerSpeed = 0.1f;
            TimerUtil.setTimerSpeed(currentTimerSpeed);
            tickableTick = tickDelay.get().intValue();
            slowNextTick = false;
            return;
        }

        slowlyReturnToNormal();
    }

    private void slowlyReturnToNormal() {
        if (Math.abs(currentTimerSpeed - 1.0f) > 0.01f) {
            currentTimerSpeed += (1.0f - currentTimerSpeed) * 0.5f;
            TimerUtil.setTimerSpeed(currentTimerSpeed);
        } else {
            TimerUtil.resetTimerSpeed();
        }
    }

    private boolean throwItems(Entity entity) {
        if (restoringInventory) return false;

        long now = System.currentTimeMillis();

        if (mc.thePlayer.getDistanceToEntity(entity) <= targetRange.get() && mc.thePlayer.canEntityBeSeen(target) && now - lastThrowTime >= 1400) {
            int originalSlot = mc.thePlayer.inventory.currentItem;
            for (int i = 0; i < 9; i++) {
                if (mc.thePlayer.inventory.getStackInSlot(i) != null) {
                    Item item = mc.thePlayer.inventory.getStackInSlot(i).getItem();
                    if (item instanceof ItemSnowball || item instanceof ItemEgg || item instanceof ItemFishingRod) {
                        if (mc.thePlayer.inventory.currentItem != i) {
                            mc.thePlayer.inventory.currentItem = i;
                            mc.playerController.updateController();
                        }
                        if (mc.thePlayer.inventory.getCurrentItem() != null && mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getCurrentItem())) {
                            mc.entityRenderer.itemRenderer.resetEquippedProgress2();
                        }
                        lastThrowTime = now;
                        int storeDelay = item instanceof ItemFishingRod ? 200 : 60;
                        scheduleInventoryRestore(originalSlot, storeDelay);
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
        EntityLivingBase closest = null;
        double closestDistance = Double.MAX_VALUE;

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (shouldNotAttack(entity) || entity instanceof EntityPlayerSP || !throughWalls.get() && !mc.thePlayer.canEntityBeSeen(entity)) continue;
            if (entity instanceof EntityLivingBase) {
                double distance = mc.thePlayer.getDistanceToEntity(entity);
                if (distance <= range && distance < closestDistance) {
                    closestDistance = distance;
                    closest = (EntityLivingBase) entity;
                }
            }
        }

        return closest;

        /*EntityLivingBase best = null;
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
        return best;*/
    }

    private boolean placeDefensiveBlock(Entity entity) {
        if (restoringInventory || mc.timer.timerSpeed != 1) return false;
        if (System.currentTimeMillis() - lastPlaceTime < 1000 || !mc.thePlayer.canEntityBeSeen(target)) return false;
        double dist = mc.thePlayer.getDistanceToEntity(entity);
        if (dist < 2.7 || dist > 3.0) return false;

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

        AxisAlignedBB bb = new AxisAlignedBB(
                pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1
        );

        if (target.getEntityBoundingBox().intersectsWith(bb)) return false;

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
        double hitY = neighbor.getY() + 0.5 + ((float) Math.random()) * 0.44F;
        double hitZ = neighbor.getZ() + 0.5 + 0.5 * opposite.getFrontOffsetZ();
        net.minecraft.util.Vec3 hitVec = new net.minecraft.util.Vec3(hitX, hitY, hitZ);

        if (DewCommon.rotationManager.faceBlockWithFacing(neighbor, placeFacing, rotationSpeed.get().floatValue(), false, true, false)) {
            if (mc.thePlayer.inventory.currentItem != blockSlot) {
                mc.thePlayer.inventory.currentItem = blockSlot;
                mc.playerController.updateController();
            }
            ItemStack itemstack = mc.thePlayer.inventory.getCurrentItem();
            if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, itemstack, neighbor, placeFacing, hitVec)) {
                if (swingMode.get().equals("Normal")) {
                    mc.thePlayer.swingItem();
                } else {
                    PacketUtil.sendPacket(new C0APacketAnimation());
                }
                if (itemstack != null) {
                    if (itemstack.stackSize == 0) {
                        mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.currentItem] = null;
                    } else if (mc.playerController.isInCreativeMode()) {
                        mc.entityRenderer.itemRenderer.resetEquippedProgress();
                    }
                }
            }

            lastPlaceTime = System.currentTimeMillis();

            scheduleInventoryRestore(originalSlot, 60);
        }

        return true;
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
            if (entity instanceof EntityPlayerSP || this.shouldNotAttack(entity) || !throughWalls.get() && !mc.thePlayer.canEntityBeSeen(entity)) continue;
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
