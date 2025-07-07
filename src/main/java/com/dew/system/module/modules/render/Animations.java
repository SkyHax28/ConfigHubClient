package com.dew.system.module.modules.render;

import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.BooleanValue;
import com.dew.system.settingsvalue.MultiSelectionValue;
import com.dew.system.settingsvalue.NumberValue;
import com.dew.system.settingsvalue.SelectionValue;
import org.lwjgl.input.Keyboard;

import java.util.Arrays;

public class Animations extends Module {

    public Animations() {
        super("Animations", ModuleCategory.RENDER, Keyboard.KEY_NONE, true, false, true);
    }

    public static final SelectionValue blockAnimation = new SelectionValue("Block Animation", "Crazy", "Vanilla", "Legacy", "Crazy", "Dash", "Exhibition Push", "Exhibition Swang", "Exhibition Swonk");
    public static final MultiSelectionValue oldAnimations = new MultiSelectionValue("1.7 Animations", Arrays.asList("Food", "Potion", "Bow", "Sword Third Person"), "Food", "Potion", "Bow", "Sword Third Person");
    private static final BooleanValue customSwingSpeed = new BooleanValue("Custom Swing Speed", false);
    private static final NumberValue swingSpeed = new NumberValue("Swing Speed", 9.0, -5.0, 20.0, 1.0, customSwingSpeed::get);
    private static final NumberValue itemScale = new NumberValue("Item Scale", 0.0, -2.0, 2.0, 0.5);

    private boolean visualBlocking = false;

    public boolean isVisualBlocking() {
        return this.visualBlocking;
    }

    public void setVisualBlocking(boolean state) {
        this.visualBlocking = state;
    }

    public boolean shouldCustomSwingSpeed() {
        return customSwingSpeed.get();
    }

    public int getSwingSpeed() {
        return swingSpeed.get().intValue();
    }

    public float getItemScale() {
        if (!this.isEnabled()) {
            return 0f;
        }

        return itemScale.get().floatValue();
    }
}
