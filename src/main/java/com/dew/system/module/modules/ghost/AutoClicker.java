package com.dew.system.module.modules.ghost;

import com.dew.system.event.events.Render3DEvent;
import com.dew.system.event.events.TickEvent;
import com.dew.system.event.events.WorldEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.NumberValue;
import com.dew.utils.LogUtil;
import net.minecraft.block.*;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.Random;

public class AutoClicker extends Module {

    public AutoClicker() {
        super("Auto Clicker", ModuleCategory.GHOST, Keyboard.KEY_NONE, false, true, true);
    }

    private static final NumberValue maxCps = new NumberValue("Max CPS", 17.0, 0.0, 20.0, 1.0);
    private static final NumberValue minCps = new NumberValue("Min CPS", 11.0, 0.0, 20.0, 1.0);

    private long nextClickTime = 0;
    private final Random random = new Random();

    private double lastInterval = 0;
    private long lastRestTime = 0;

    @Override
    public void onDisable() {
        this.fullResetState();
    }

    @Override
    public void onWorld(WorldEvent event) {
        this.fullResetState();
    }

    private void fullResetState() {
        nextClickTime = 0;
        lastInterval = 0;
        lastRestTime = 0;
    }

    private void resetState() {
        nextClickTime = 0;
        lastInterval = 0;
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (mc.thePlayer == null) return;

        if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            mc.gameSettings.keyBindAttack.setKeyDown(Mouse.isButtonDown(0));
            resetState();
            return;
        }

        if (!Mouse.isButtonDown(0)) {
            resetState();
            return;
        }

        long currentTime = System.currentTimeMillis();

        if (currentTime - lastRestTime > 300 && random.nextDouble() < 0.03) {
            nextClickTime += 30 + random.nextInt(50);
            lastRestTime = currentTime;
            return;
        }

        mc.gameSettings.keyBindAttack.setKeyDown(false);

        if (currentTime >= nextClickTime) {
            KeyBinding.onTick(mc.gameSettings.keyBindAttack.getKeyCode());
            lastInterval = generateNextInterval();
            nextClickTime = currentTime + (long) lastInterval;
        }
    }

    private double generateNextInterval() {
        int targetCPS = minCps.get().intValue() + random.nextInt(maxCps.get().intValue() - minCps.get().intValue() + 1);

        double baseInterval = 1000.0 / targetCPS;
        double gaussianOffset = random.nextGaussian() * 3.0;

        double interval = baseInterval + gaussianOffset;

        if (random.nextDouble() < 0.08) {
            interval -= random.nextInt(6);
        }

        if (random.nextDouble() < 0.05) {
            interval += random.nextInt(10);
        }

        interval += (random.nextDouble() - 0.5) * 2;

        double minInterval = 1000.0 / maxCps.get().intValue();
        double maxInterval = 1000.0 / minCps.get().intValue();
        interval = Math.max(minInterval, Math.min(maxInterval, interval));

        return interval;
    }
}
