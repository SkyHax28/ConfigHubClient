package com.dew.system.module.modules.movement;

import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.NumberValue;
import com.dew.system.settingsvalue.BooleanValue;
import com.dew.utils.MovementUtil;
import com.dew.utils.PacketUtil;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.util.BlockPos;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class MMCLongJump extends Module {

    private final BooleanValue manual = new BooleanValue("Manual", false);
    private final BooleanValue onlyVelocity = new BooleanValue("Only while velocity", false);
    private final NumberValue boost = new NumberValue("Horizontal boost", 2.5, 0.0, 10.0, 0.05);
    private final BooleanValue allowStrafe = new BooleanValue("Allow strafe", false);
    private final BooleanValue invertYaw = new BooleanValue("Invert yaw", true);
    private final BooleanValue stopMovement = new BooleanValue("Stop movement", false);
    private final BooleanValue jumpAfter = new BooleanValue("Jump after throw", false);
    private final BooleanValue silentSwing = new BooleanValue("Silent swing", false);

    private boolean active = false;
    private int ticks = 0;
    private int rotateTicks = 0;
    private int oldSlot = -1;
    private long throwTime = 0;

    public LongJump() {
        super("LongJump", ModuleCategory.MOVEMENT, Keyboard.KEY_NONE, false, true, true);
        register(manual, onlyVelocity, boost, allowStrafe, invertYaw, stopMovement, jumpAfter, silentSwing);
    }

    @Override
    public void onEnable() {
        if (mc.thePlayer == null) return;

        active = false;
        ticks = rotateTicks = 0;
        oldSlot = -1;
        throwTime = 0;
    }

    @Override
    public void onDisable() {
        active = false;
        resetSlot();
    }

    @Override
    public String tag() {
        return "MMC";
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        boolean holdingFireball = mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() == Items.fire_charge;

        if (!active && Mouse.isButtonDown(1) && holdingFireball) {
            if (!manual.get() || (onlyVelocity.get() && isVelocityActive())) {
                startJump();
                return;
            }
        }

        if (!active) return;

        if (throwTime > 0 && (System.currentTimeMillis() - throwTime > 800 || mc.thePlayer.motionY < -0.0784)) {
            send("&cFireball timeout!");
            disable();
            return;
        }

        if (mc.thePlayer.onGround && ticks > 2) {
            disable();
            return;
        }

        // Boosts you big bypass
        if (ticks >= 0) {
            if (allowStrafe.get() && ticks < 32) {
                MovementUtil.strafe((float) MovementUtil.getSpeed());
            }

            if (boost.get() > 0) {
                double speed = boost.get() - Math.random() * 0.0001;
                MovementUtil.strafe((float) speed);
            }
            ticks++;
        }
    }

    private void startJump() {
        int fireballSlot = getFireballSlot();
        if (fireballSlot == -1) {
            send("&cNo fireball found!");
            return;
        }

        if (MovementUtil.getDistanceToGround() > 3) {
            send("&cCan't throw in air!");
            return;
        }

        oldSlot = mc.thePlayer.inventory.currentItem;

        // switches slot ig
        if (oldSlot != fireballSlot) {
            PacketUtil.sendPacket(new C09PacketHeldItemChange(fireballSlot));
            mc.thePlayer.inventory.currentItem = fireballSlot;
        }

        active = true;
        ticks = -1;
        rotateTicks = 1;
        throwTime = 0;
    }


    public void onPreMotion() { 
        if (!active || rotateTicks == 0) return;

        float yaw = mc.thePlayer.rotationYaw;
        if (invertYaw.get() || stopMovement.get()) {
            yaw -= 180f;
        }

        float pitch = stopMovement.get() ? 66.3f : 90f;

        mc.thePlayer.rotationYaw = yaw;
        mc.thePlayer.rotationPitch = pitch;

        if (++rotateTicks == 3) {
            PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255, mc.thePlayer.getHeldItem(), 0, 0, 0));

            if (silentSwing.get()) {
                PacketUtil.sendPacket(new C0APacketAnimation());
            } else {
                mc.thePlayer.swingItem();
            }

            throwTime = System.currentTimeMillis();
            ticks = 0;

            resetSlot();
        }
    }

    // Explosion abfangen für Boost
    public void onPacket(Object packet) {
        if (!active || !(packet instanceof S27PacketExplosion)) return;

        S27PacketExplosion explosion = (S27PacketExplosion) packet;

        if (throwTime == 0 || mc.thePlayer.getDistanceSq(explosion.getX(), explosion.getY(), explosion.getZ()) > 9) {
            send("&cExplosion too far!");
            disable();
            return;
        }

        ticks = 0;
        throwTime = 0;
    }

    private void resetSlot() {
        if (oldSlot != -1) {
            PacketUtil.sendPacket(new C09PacketHeldItemChange(oldSlot));
            mc.thePlayer.inventory.currentItem = oldSlot;
            oldSlot = -1;
        }
    }

    private int getFireballSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() == Items.fire_charge) {
                return i;
            }
        }
        return -1;
    }

    private boolean isVelocityActive() {
        return false;
    }

    private void send(String msg) {
    }
}