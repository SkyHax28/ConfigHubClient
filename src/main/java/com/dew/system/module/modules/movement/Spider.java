package com.dew.system.module.modules.movement;

import com.dew.DewCommon;
import com.dew.system.event.events.BlockBBEvent;
import com.dew.system.event.events.PreMotionEvent;
import com.dew.system.event.events.LoadWorldEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.module.modules.movement.speed.SpeedModule;
import com.dew.system.settingsvalue.SelectionValue;
import com.dew.utils.*;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.input.Keyboard;

public class Spider extends Module {

    private static final SelectionValue mode = new SelectionValue("Mode", "Prediction Infinite", "Prediction Infinite");
    private final Clock hypTimer = new Clock();
    private boolean blinked = false;

    public Spider() {
        super("Spider", ModuleCategory.MOVEMENT, Keyboard.KEY_NONE, false, true, true);
    }

    public boolean ignoreJumpDelay() {
        return this.isEnabled() && mode.get().equals("Prediction Infinite");
    }

    @Override
    public String tag() {
        return mode.get();
    }

    @Override
    public void onEnable() {
        if (mode.get().equals("Prediction Infinite")) {
            LogUtil.printChat("If you hold the jump key near a wall, you can climb up");
        }
    }

    @Override
    public void onDisable() {
        BlinkUtil.sync(true, true);
        BlinkUtil.stopBlink();
        TimerUtil.resetTimerSpeed();
        hypTimer.reset();
        blinked = false;
    }

    @Override
    public void onLoadWorld(LoadWorldEvent event) {
        BlinkUtil.sync(true, true);
        BlinkUtil.stopBlink();
        TimerUtil.resetTimerSpeed();
        hypTimer.reset();
        blinked = false;
    }

    private boolean shouldSpider() {
        return MovementUtil.isBlockUnderPlayer(mc.thePlayer, 2, 0.5, false) || mc.thePlayer.isCollidedHorizontally;
    }

    @Override
    public void onBlockBB(BlockBBEvent event) {
        /*if (mc.thePlayer == null || mc.theWorld == null || mc.thePlayer.isSneaking() && mc.thePlayer.getDistanceSqToCenter(event.blockPos) < 2) return;

        if (event.blockPos.getY() != mc.thePlayer.getPosition().down().getY()) {
            int x = event.blockPos.getX();
            int y = event.blockPos.getY();
            int z = event.blockPos.getZ();
            //event.boundingBox = new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1);
            event.cancel();
        }*/
    }

    @Override
    public void onPreMotion(PreMotionEvent event) {
        if (mc.thePlayer == null) return;

        event.forceC06 = true;

        /*if (mode.get().equals("Prediction Infinite")) {
            if (shouldSpider()) {
                if (hypTimer.hasTimePassed(300)) {
                    //TimerUtil.setTimerSpeed(0.3f);
                    //PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + (0.41999998688698 + Math.random() * 0.003), mc.thePlayer.posZ, false));
                    MovementUtil.fakeJump();
                    event.onGround = true;
                    mc.thePlayer.onGround = true;
                    hypTimer.reset();
                } else {
                    event.onGround = false;
                    mc.thePlayer.onGround = false;
                    //TimerUtil.resetTimerSpeed();
                }
                blinked = true;
            } else if (blinked) {
                //TimerUtil.resetTimerSpeed();
                hypTimer.reset();
                blinked = false;
            }
        }*/
    }
}
