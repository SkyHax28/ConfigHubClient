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

    private final BooleanValue manual           = new BooleanValue("Manual", false);
    private final BooleanValue onlyVelocity     = new BooleanValue("Only while velocity", false);
    private final NumberValue boost             = new NumberValue("Horizontal boost", 2.5, 0.0, 10.0, 0.05);
    private final BooleanValue allowStrafe      = new BooleanValue("Allow strafe", false);
    private final BooleanValue invertYaw        = new BooleanValue("Invert yaw", true);
    private final BooleanValue stopMovement     = new BooleanValue("Stop movement", false);
    private final BooleanValue jumpAfter        = new BooleanValue("Jump after throw", false);
    private final BooleanValue silentSwing      = new BooleanValue("Silent swing", false);

    // Runtime
    private boolean active = false;
    private int ticks = 0;
    private int rotateTicks = 0;
    private int oldSlot = -1;
    private long throwTime = 0;

    public MMCLongJump() {
        super("MMC LongJump", ModuleCategory.MOVEMENT, Keyboard.KEY_NONE, false, true, true);
    }

    @Override
    public void onEnable() {
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

        boolean holdingFireball = mc.thePlayer.getHeldItem() != null
                && mc.thePlayer.getHeldItem().getItem() == Items.fire_charge;

        if (!active && !manual.get() && Mouse.isButtonDown(1) && holdingFireball) {
            startLongJump();
            return;
        }

        if (!active && manual.get() && Mouse.isButtonDown(1) && holdingFireball) {
            startLongJump();
            return;
        }

        if (!active) return;

        if (throwTime > 0 && (System.currentTimeMillis() - throwTime > 800 || mc.thePlayer.motionY < -0.0784)) {
            sendMessage("Fireball timeout!");
            setEnabled(false);
            return;
        }

        if (mc.thePlayer.onGround && ticks > 2) {
            setEnabled(false);
            return;
        }

        if (ticks >= 0) {
            double speed = boost.getValue();
            if (speed > 0) {
                if (allowStrafe.get()) {
                    MovementUtil.strafe((float) MovementUtil.getSpeed());
                }
                MovementUtil.strafe((float) (speed - Math.random() * 0.0001));
            }
            ticks++;
        }
    }

    public void onPreMotion() {
        if (!active || rotateTicks <= 0) return;

        float yaw = mc.thePlayer.rotationYaw;
        if (invertYaw.get() || stopMovement.get()) {
            yaw -= 180f;
        }
        float pitch = stopMovement.get() ? 66.3f : 90f;

        mc.thePlayer.rotationYaw = yaw;
        mc.thePlayer.rotationPitch = pitch;

        if (++rotateTicks == 3) {
            PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(
                    new BlockPos(-1, -1, -1), 255, mc.thePlayer.getHeldItem(), 0, 0, 0));

            if (silentSwing.get()) {
                PacketUtil.sendPacket(new C0APacketAnimation());
            } else {
                mc.thePlayer.swingItem();
            }

            throwTime = System.currentTimeMillis();
            ticks = 0;
        }
    }

    public void onReceivePacket(Object packet) {
        if (!active || !(packet instanceof S27PacketExplosion)) return;

        S27PacketExplosion explosion = (S27PacketExplosion) packet;

        double distSq = mc.thePlayer.getDistanceSq(explosion.getX(), explosion.getY(), explosion.getZ());
        if (throwTime == 0 || distSq > 9) {
            sendMessage("Explosion too far!");
            setEnabled(false);
            return;
        }

        // Boost start :fire:
        ticks = 0;
        throwTime = 0;
        resetSlot();
    }

    private void startLongJump() {
        int fireballSlot = getFireballSlot();
        if (fireballSlot == -1) {
            sendMessage("No fireball in hotbar!");
            return;
        }

        oldSlot = mc.thePlayer.inventory.currentItem;

        if (oldSlot != fireballSlot) {
            PacketUtil.sendPacket(new C09PacketHeldItemChange(fireballSlot));
            mc.thePlayer.inventory.currentItem = fireballSlot;
        }

        active = true;
        ticks = -1;
        rotateTicks = 1;
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

    private void sendMessage(String msg) {
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText("§8[§cMMC§8] §7" + msg));
        }
    }
}

// im hoping that it works now :praye: