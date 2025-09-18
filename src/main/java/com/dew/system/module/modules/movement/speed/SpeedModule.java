package com.dew.system.module.modules.movement.speed;

import com.dew.DewCommon;
import com.dew.system.event.events.*;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.module.modules.exploit.SafetySwitchv2000;
import com.dew.system.module.modules.movement.speed.speeds.*;
import com.dew.system.settingsvalue.BooleanValue;
import com.dew.system.settingsvalue.NumberValue;
import com.dew.system.settingsvalue.SelectionValue;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;
import java.util.Map;

public class SpeedModule extends Module {

    private static final SelectionValue mode = new SelectionValue("Mode", "Vanilla", "Vanilla", "Hypixel", "Hypixel Prediction", "BlocksMC", "Bloxd", "Verus", "Vulcan", "Latest NCP", "Test");
    private static final BooleanValue autoBHop = new BooleanValue("Auto BHop", true, () -> mode.get().equals("Vanilla"));
    private static final NumberValue speed = new NumberValue("Speed", 1, 0.1, 5.0, 0.1, () -> mode.get().equals("Vanilla"));
    private static final SelectionValue hypixelLowHopMode = new SelectionValue("Hypixel Lowhop Mode", "More Strafe", () -> mode.get().equals("Hypixel"), "More Strafe", "7 Tick");
    private final Map<String, SpeedMode> modes = new HashMap<>();
    private SpeedMode currentMode = null;
    private String lastModeName = null;

    public SpeedModule() {
        super("Speed", ModuleCategory.MOVEMENT, Keyboard.KEY_NONE, false, true, true);

        modes.put("Vanilla", new VanillaSpeed());
        modes.put("Hypixel", new HypixelSpeed());
        modes.put("Hypixel Prediction", new HypixelPredictionSpeed());
        modes.put("BlocksMC", new BlocksMCSpeed());
        modes.put("Bloxd", new BloxdSpeed());
        modes.put("Verus", new VerusSpeed());
        modes.put("Vulcan", new VulcanSpeed());
        modes.put("Latest NCP", new LatestNCPSpeed());
        modes.put("Test", new TestSpeed());
    }

    public String getMode() {
        return mode.get();
    }

    public String getHypixelLowHopMode() {
        return hypixelLowHopMode.get();
    }

    public boolean isAutoBhop() {
        return autoBHop.get();
    }

    public double getSpeed() {
        return speed.get();
    }

    @Override
    public String tag() {
        return mode.get();
    }

    @Override
    public void onEnable() {
        lastModeName = mode.get();
        currentMode = modes.get(lastModeName);

        if (currentMode != null)
            currentMode.onEnable();
    }

    @Override
    public void onDisable() {
        if (currentMode != null)
            currentMode.onDisable();
    }

    @Override
    public void onAttack(AttackEvent event) {
        if (currentMode != null)
            currentMode.onAttack(event);
    }

    @Override
    public void onLoadWorld(LoadWorldEvent event) {
        if (DewCommon.moduleManager.getModule(SafetySwitchv2000.class).isEnabled()) {
            this.setState(false);
        }
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        String currentSelected = mode.get();

        if (!currentSelected.equals(lastModeName)) {
            if (currentMode != null)
                currentMode.onDisable();
            currentMode = modes.get(currentSelected);
            if (currentMode != null && this.isEnabled())
                currentMode.onEnable();
            lastModeName = currentSelected;
        }

        if (currentMode != null)
            currentMode.onPreUpdate(event);
    }

    @Override
    public void onPreMotion(PreMotionEvent event) {
        if (currentMode != null)
            currentMode.onPreMotion(event);
    }

    @Override
    public void onMove(MoveEvent event) {
        if (currentMode != null)
            currentMode.onMove(event);
    }

    @Override
    public void onBlockBB(BlockBBEvent event) {
        if (currentMode != null)
            currentMode.onBlockBB(event);
    }

    @Override
    public void onReceivedPacket(ReceivedPacketEvent event) {
        if (currentMode != null)
            currentMode.onReceivedPacket(event);
    }
}
