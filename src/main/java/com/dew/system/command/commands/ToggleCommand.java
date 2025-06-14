package com.dew.system.command.commands;

import com.dew.DewCommon;
import com.dew.system.command.Command;
import com.dew.system.module.Module;
import com.dew.utils.LogUtil;

public class ToggleCommand extends Command {

    public ToggleCommand() {
        super("t");
    }

    @Override
    public String getUsage() {
        return "t <module> - Toggle the specified module";
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            LogUtil.printChat("§cUsage: .t <module>");
            return;
        }

        String name = args[0];
        for (Module module : DewCommon.moduleManager.getModules()) {
            if (module.name.replace(" ", "").equalsIgnoreCase(name)) {
                module.toggleState();
                LogUtil.printChat("§aToggled " + module.name + ": " + (module.isEnabled() ? "ON" : "OFF"));
                return;
            }
        }

        LogUtil.printChat("§cModule not found: " + name);
    }
}