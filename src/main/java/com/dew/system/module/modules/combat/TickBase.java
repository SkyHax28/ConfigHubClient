package com.dew.system.module.modules.combat;

import com.dew.system.event.events.AttackEvent;
import com.dew.system.event.events.PreMotionEvent;
import com.dew.system.event.events.Render3DEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.utils.AaBbUtil;
import com.dew.utils.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.potion.Potion;
import org.lwjgl.input.Keyboard;

public class TickBase extends Module {
    private Mode mode = Mode.NONE;
    private long time, balance;
    private double range, distance;
    private Entity target = null;

    public TickBase() {
        super("Tick Base", ModuleCategory.COMBAT, Keyboard.KEY_NONE, false, true, true);
    }

    @Override
    public void onDisable() {
        TimerUtil.resetTimerSpeed();
        mode = Mode.NONE;
        target = null;
        balance = 0;
    }

    @Override
    public void onAttack(AttackEvent event) {
        Entity target = event.target;
        if (target != null) {
            this.target = target;
        }
    }

    @Override
    public void onPreMotion(PreMotionEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (mode == Mode.REDUCING) {
            return;
        }

        if (target == null) return;

        distance = AaBbUtil.distanceTo(mc.thePlayer.getPositionEyes(1f), target.getEntityBoundingBox());
        double currentRange = distance;

        if (currentRange >= 1 && balance >= 50 && mode == Mode.BASING) {
            balance -= 100;
            mc.timer.elapsedTicks += 1;
        } else {
            balance = 0;
            mode = Mode.NONE;
        }

        double lagRange = 8.0;

        if (currentRange < lagRange && this.range >= lagRange && mode == Mode.NONE) {
            mode = Mode.REDUCING;
            time = System.currentTimeMillis();
            balance = 0;
        }

        this.range = currentRange;
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (mode != Mode.REDUCING || target == null) return;

        double speedMultiplier = mc.thePlayer.isPotionActive(Potion.moveSpeed) ? 0.36 : 0.25;
        long requiredTime = (long) ((range / speedMultiplier) * 25) + 25;

        if (distance <= 1 || System.currentTimeMillis() - time >= requiredTime) {
            TimerUtil.resetTimerSpeed();
            mode = Mode.BASING;
            balance = System.currentTimeMillis() - time;
            return;
        }

        TimerUtil.setTimerSpeed(0f);
    }

    private enum Mode {
        REDUCING, BASING, NONE
    }
}