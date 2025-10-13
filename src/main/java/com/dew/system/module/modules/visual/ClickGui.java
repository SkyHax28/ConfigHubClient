package com.dew.system.module.modules.visual;

import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.BooleanValue;
import com.dew.system.settingsvalue.NumberValue;
import com.dew.system.settingsvalue.SelectionValue;
import org.lwjgl.input.Keyboard;

public class ClickGui extends Module {

    private static final SelectionValue mode = new SelectionValue("Mode", "Modern", "Modern", "Nostalgia");
    private static final NumberValue guiScale = new NumberValue("Gui Scale", 1.0, 0.5, 2.0, 0.1, () -> mode.get().equals("Modern"));
    private static final BooleanValue blur = new BooleanValue("Blur", true);
    private Float cachedGuiScale = null;

    public ClickGui() {
        super("Click Gui", ModuleCategory.VISUAL, Keyboard.KEY_RSHIFT, false, false, false);
    }

    public String getMode() {
        return mode.get();
    }

    public float getGuiScale() {
        this.updateCache();
        return cachedGuiScale;
    }

    public boolean renderBlur() {
        return blur.get();
    }

    private void updateCache() {
        if (mc.currentScreen == null) {
            cachedGuiScale = null;
        }

        if (cachedGuiScale == null) {
            cachedGuiScale = guiScale.get().floatValue();
        }
    }

    @Override
    public void onEnable() {
        this.updateCache();
    }
}
