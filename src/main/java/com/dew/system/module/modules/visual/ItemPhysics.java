package com.dew.system.module.modules.visual;

import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import org.lwjgl.input.Keyboard;

public class ItemPhysics extends Module {

    public ItemPhysics() {
        super("Item Physics", ModuleCategory.VISUAL, Keyboard.KEY_NONE, false, false, true);
    }
}
