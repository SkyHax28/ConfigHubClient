package com.dew.system.module.modules.movement;

import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.event.events.PreMotionEvent;
import com.dew.system.event.events.Render2DEvent;
import com.dew.system.event.events.ReceivedPacketEvent;

import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;

import com.dew.system.settingsvalue.BooleanValue;
import com.dew.system.settingsvalue.NumberValue;
import com.dew.system.settingsvalue.SelectionValue;

import com.dew.utils.MovementUtil;
import com.dew.utils.PacketUtil;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C09PacketHeldItemChange;

import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.util.BlockPos;

import org.lwjgl.input.Mouse;
import org.lwjgl.input.Keyboard;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class MMCLongJump extends Module {

    // Modes: Float, Boost, Delay
    private final SelectionValue mode = new SelectionValue(
            "Mode",
            "Float",
            "Float", "Boost", "Delay"
    );

    private final BooleanValue manual = new BooleanValue("Manual", false);
    private final BooleanValue onlyVelocity = new BooleanValue("Only while velocity", false);

    private final NumberValue horizontalBoost = new NumberValue("Horizontal boost", 1.7, 0.0, 3.0, 0.05);
    private final NumberValue verticalMotion = new NumberValue("Vertical motion", 0.2, 0.0, 1.0, 0.01);
    private final NumberValue motionDecay = new NumberValue("Motion decay", 40.0, 1.0, 100.0, 1.0);

    private final BooleanValue allowStrafe = new BooleanValue("Allow strafe", false);
    private final BooleanValue invertYaw = new BooleanValue("Invert yaw", true);
    private final BooleanValue stopMovement = new BooleanValue("Stop movement", false);
    private final BooleanValue jumpAfterThrow = new BooleanValue("Jump", false);
    private final BooleanValue hideExplosion = new BooleanValue("Hide explosion", false);
    private final BooleanValue spoofItem = new BooleanValue("Spoof item", false);
    private final BooleanValue silentSwing = new BooleanValue("Silent swing", false);
    private final BooleanValue renderProgress = new BooleanValue("Render bar", true);

    // Runtime
    private boolean function = false;
    private boolean swappedSlot = false;
    private boolean delaying = false;
    private boolean notMoving = false;

    private int lastSlot = -1;
    private int spoofSlot = -1;
    private int fireballRotateTicks = 0;
    private int boostTicks = -1;
    private int delayTicks = -1;

    private long fireballTime = 0L;
    private long MAX_EXPLOSION_DIST_SQ = 9;

    // Progress bar fields
    private float barWidth = 60f;
    private float barHeight = 4f;
    private float filledWidth = 0f;
    private float barX;
    private float barY;

    // Packet queue for Delay mode
    private final List<Packet<?>> delayedPackets = new ArrayList<>();


    public MMCLongJump() {
        super("MMC LongJump", ModuleCategory.MOVEMENT, Keyboard.KEY_NONE, false, true, true);
    }

    @Override
    public void onEnable() {
        function = false;
        swappedSlot = false;
        delaying = false;
        spoofSlot = -1;
        lastSlot = -1;
        boostTicks = -1;
        delayTicks = -1;
        fireballTime = 0;
        fireballRotateTicks = 0;
        filledWidth = 0;

        // Init progress bar position
        int w = mc.displayWidth / 2;
        int h = mc.displayHeight / 2;
        barX = w - barWidth / 2f;
        barY = h + 12;
    }

    @Override
    public void onDisable() {
        disableLogic();
    }

    private void disableLogic() {
        function = false;
        delaying = false;
        boostTicks = -1;
        delayTicks = -1;
        fireballTime = 0;
        filledWidth = 0;

        resetSlot();
    }

    //
    // Core logic
    //

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer == null) return;

        boolean holdingFireball = mc.thePlayer.getHeldItem() != null &&
                mc.thePlayer.getHeldItem().getItem() == Items.fire_charge;

        //
        // Trigger logic
        //
        if (!function) {
            if (manual.get()) {
                if (Mouse.isButtonDown(1) && holdingFireball && (!onlyVelocity.get() || MovementUtil.hasMotionHorizontal())) {
                    startLongJump();
                    return;
                }
            } else {
                if (Mouse.isButtonDown(1) && holdingFireball) {
                    startLongJump();
                    return;
                }
            }
            return;
        }

        //
        // Fireball timeout failsafe
        //
        long timeout = 750L;
        if (jumpAfterThrow.get() && !mode.get().equals("Delay")) timeout = 350L;

        if (fireballTime > 0 && (System.currentTimeMillis() - fireballTime > timeout ||
                mc.thePlayer.motionY < -0.0784)) {
            sendMessage("Fireball timeout");
            setState(false);
            return;
        }

        //
        // Ground terminate
        //
        if (mc.thePlayer.onGround && boostTicks > 2) {
            setState(false);
            return;
        }

        //
        // Movement logic
        //

        boolean isFloat = mode.get().equals("Float");
        boolean isBoost = mode.get().equals("Boost");
        boolean isDelay = mode.get().equals("Delay");

        if (isFloat) applyFloatModeMovement();
        if (isBoost) applyBoostModeMovement();
        if (isDelay) applyDelayModeMovement();
    }

    private void applyFloatModeMovement() {
        if (boostTicks <= 0) return;

        double decay = motionDecay.get() / 1000.0;
        double vertical = verticalMotion.get();

        if (notMoving) {
            vertical = 1.16;
            decay = 20.0 / 1000.0;
        }

        if (boostTicks >= 1 && boostTicks <= 32) {
            mc.thePlayer.motionY = vertical - boostTicks * decay;
        } else if (boostTicks > 32) {
            mc.thePlayer.motionY += 0.028;
        }

        if (allowStrafe.get()) {
            MovementUtil.strafe(MovementUtil.getSpeed());
        }
    }

    private void applyBoostModeMovement() {
        if (boostTicks < 1) return;

        if (horizontalBoost.get() > 0) {
            double speed = horizontalBoost.get() - (Math.random() * 0.0001);

            if (MovementUtil.isMoving()) {
                MovementUtil.setSpeed(speed);
            }
        }
    }

    private void applyDelayModeMovement() {
        if (!delaying) return;

        delayTicks++;
        filledWidth = barWidth * delayTicks / 20f;

        if (delayTicks > 20) {
            flushDelayedPackets();
            setState(false);
        }
    }

    //
    // Fireball launch and rotation spoof
    //

    @Override
    public void onPreMotion(PreMotionEvent event) {
        if (!function) return;

        // Rotation sequence
        if (fireballRotateTicks > 0) {
            if (fireballRotateTicks == 1) {
                float yaw = mc.thePlayer.rotationYaw;
                float pitch = stopMovement.get() ? 66.3f : 90f;

                if (invertYaw.get()) yaw -= 180f;

                event.yaw = yaw;
                event.pitch = pitch;
            }

            fireballRotateTicks++;

            if (fireballRotateTicks >= 3) {
                throwFireball();
                fireballRotateTicks = 0;
            }
        }
    }

    private void throwFireball() {
        fireballTime = System.currentTimeMillis();

        // Actual fireball throw
        PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(
                new BlockPos(-1, -1, -1),
                255,
                mc.thePlayer.getHeldItem(),
                0, 0, 0
        ));

        if (silentSwing.get()) {
            PacketUtil.sendPacket(new C0APacketAnimation());
        } else {
            mc.thePlayer.swingItem();
        }

        boostTicks = -1;
    }

    //
    // Packet logic (explosion + delay mode)
    //

    @Override
    public void onReceivedPacket(ReceivedPacketEvent event) {
        if (!function) return;

        Packet<?> p = event.getPacket();

        // Explosion detection
        if (p instanceof S27PacketExplosion) {
            S27PacketExplosion ex = (S27PacketExplosion) p;

            double distSq = mc.thePlayer.getDistanceSq(ex.getX(), ex.getY(), ex.getZ());
            if (fireballTime == 0 || distSq > MAX_EXPLOSION_DIST_SQ) {
                sendMessage("Explosion too far");
                setState(false);
                return;
            }

            fireballTime = 0;
            boostTicks = 0;

            if (mode.get().equals("Delay")) {
                delaying = true;
            }

            resetSlot();
        }

        // Delay mode packet queue
        if (delaying) {
            delayedPackets.add(p);
            event.setCancelled(true);
        }
    }

    private void flushDelayedPackets() {
        for (Packet<?> p : delayedPackets) {
            PacketUtil.processPacketClientSide(p);
        }
        delayedPackets.clear();
        delaying = false;
    }

    //
    // Rendering (progress bar)
    //

    @Override
    public void onRender2D(Render2DEvent event) {
        if (!renderProgress.get()) return;
        if (!function) return;

        int color = new Color(0, 180, 255).getRGB();

        // Background
        drawRect(barX, barY, barX + barWidth, barY + barHeight, new Color(30, 30, 30, 180).getRGB());

        // Fill
        drawRect(barX, barY, barX + filledWidth, barY + barHeight, color);
    }

    private void drawRect(float x1, float y1, float x2, float y2, int color) {
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = (color) & 0xFF;

        net.minecraft.client.renderer.GlStateManager.enableBlend();
        net.minecraft.client.renderer.GlStateManager.disableTexture2D();
        net.minecraft.client.renderer.GlStateManager.color(r / 255f, g / 255f, b / 255f, a / 255f);
        net.minecraft.client.renderer.Tessellator tess = net.minecraft.client.renderer.Tessellator.getInstance();
        net.minecraft.client.renderer.WorldRenderer wr = tess.getWorldRenderer();
        wr.begin(7, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION);
        wr.pos(x1, y2, 0).endVertex();
        wr.pos(x2, y2, 0).endVertex();
        wr.pos(x2, y1, 0).endVertex();
        wr.pos(x1, y1, 0).endVertex();
        tess.draw();
        net.minecraft.client.renderer.GlStateManager.enableTexture2D();
        net.minecraft.client.renderer.GlStateManager.disableBlend();
    }

    //
    // Utility
    //

    private void startLongJump() {
        int fireballSlot = findFireballSlot();
        if (fireballSlot == -1) {
            sendMessage("No fireball in hotbar");
            return;
        }

        function = true;
        boostTicks = -1;
        delayTicks = 0;
        fireballRotateTicks = 1;

        lastSlot = mc.thePlayer.inventory.currentItem;

        if (!manual.get()) {
            spoofSlot = fireballSlot;
            PacketUtil.sendPacket(new C09PacketHeldItemChange(fireballSlot));
            mc.thePlayer.inventory.currentItem = fireballSlot;
            swappedSlot = true;
        }
    }

    private void resetSlot() {
        if (swappedSlot && lastSlot != -1) {
            PacketUtil.sendPacket(new C09PacketHeldItemChange(lastSlot));
            mc.thePlayer.inventory.currentItem = lastSlot;
        }
        swappedSlot = false;
        lastSlot = -1;
    }

    private int findFireballSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack st = mc.thePlayer.inventory.getStackInSlot(i);
            if (st != null && st.getItem() == Items.fire_charge) return i;
        }
        return -1;
    }

    private void sendMessage(String msg) {
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText("§8[§cMMC§8] §7" + msg));
        }
    }

    @Override
    public String tag() {
        return "MMC";
    }
}
