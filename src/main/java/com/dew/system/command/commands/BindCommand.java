package com.dew.system.command.commands;

import com.dew.DewCommon;
import com.dew.system.command.Command;
import com.dew.system.module.Module;
import com.dew.utils.LogUtil;
import org.lwjgl.input.Keyboard;

public class BindCommand extends Command {

    public BindCommand() {
        super("bind");
    }

    @Override
    public String getUsage() {
        return "bind <module> <key> - Bind a key to the specified module";
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            LogUtil.printChat("§cUsage: .bind <module> <key>");
            return;
        }

        String moduleName = args[0];
        String keyName = args[1].toUpperCase();

        Module target = null;
        for (Module module : DewCommon.moduleManager.getModules()) {
            if (module.name.replace(" ", "").equalsIgnoreCase(moduleName)) {
                target = module;
                break;
            }
        }

        if (target == null) {
            LogUtil.printChat("§cModule not found: " + moduleName);
            return;
        }

        int keyCode = Keyboard.getKeyIndex(keyName);
        if (keyCode == Keyboard.KEY_NONE || keyCode == -1) {
            target.key = Keyboard.KEY_NONE;
            LogUtil.printChat("§aUnbound " + target.name);
            return;
        }

        target.key = keyCode;
        LogUtil.printChat("§aBound " + target.name + " to §e" + keyName);
    }
}