package com.dew.system.module.modules.movement.flight;

import com.dew.system.event.events.*;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.module.modules.movement.flight.flies.*;
import com.dew.system.settingsvalue.NumberValue;
import com.dew.system.settingsvalue.SelectionValue;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;
import java.util.Map;

public class FlightModule extends Module {

    private static final SelectionValue mode = new SelectionValue("Mode", "Vanilla", "Vanilla", "No Clip", "Hypixel Prediction", "Bloxd", "Verus", "Test");
    public static final NumberValue horizontalSpeed = new NumberValue("Horizontal Speed", 3, 0.1, 10.0, 0.1, () -> mode.get().equals("Vanilla") || mode.get().equals("No Clip"));
    public static final NumberValue verticalSpeed = new NumberValue("Vertical Speed", 2, 0.1, 10.0, 0.1, () -> mode.get().equals("Vanilla") || mode.get().equals("No Clip"));
    private final Map<String, FlightMode> modes = new HashMap<>();
    private FlightMode currentMode = null;
    private String lastModeName = null;

    public FlightModule() {
        super("Flight", ModuleCategory.MOVEMENT, Keyboard.KEY_NONE, false, true, true);

        modes.put("Vanilla", new VanillaFlight());
        modes.put("No Clip", new NoClipFlight());
        modes.put("Hypixel Prediction", new HypixelPredictionFlight());
        modes.put("Bloxd", new BloxdFlight());
        modes.put("Verus", new VerusFlight());
        modes.put("Test", new TestFlight());
    }

    public String getMode() {
        return mode.get();
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
    public void onBlockBB(BlockBBEvent event) {
        if (currentMode != null)
            currentMode.onBlockBB(event);
    }

    @Override
    public void onSendPacket(SendPacketEvent event) {
        if (currentMode != null)
            currentMode.onSendPacket(event);
    }

    @Override
    public void onReceivedPacket(ReceivedPacketEvent event) {
        if (currentMode != null)
            currentMode.onReceivedPacket(event);
    }
}
