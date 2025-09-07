package com.dew.system.module.modules.render;

import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import org.lwjgl.input.Keyboard;

public class ItemPhysics extends Module {

    public ItemPhysics() {
        super("Item Physics", ModuleCategory.RENDER, Keyboard.KEY_NONE, false, false, true);
    }
}
