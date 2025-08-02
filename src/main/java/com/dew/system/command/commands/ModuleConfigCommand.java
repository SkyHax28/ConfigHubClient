package com.dew.system.command.commands;

import com.dew.DewCommon;
import com.dew.system.command.Command;
import com.dew.utils.LogUtil;

public class ModuleConfigCommand extends Command {

    public ModuleConfigCommand() {
        super("m");
    }

    @Override
    public String getUsage() {
        return "m <load/save/list/folder> <name> - Customize module configuration through ConfigManager";
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            LogUtil.printChat("§cUsage: .m <load/save/list/folder> <name>");
            return;
        }

        switch (args[0].toLowerCase()) {
            case "load":
                if (args.length < 2) {
                    LogUtil.printChat("§cUsage: .m load <name>");
                    return;
                }
                if (DewCommon.moduleConfigManager.load(args[1], DewCommon.moduleManager.getModules())) {
                    LogUtil.printChat("Loaded module config: " + args[1]);
                } else {
                    LogUtil.printChat("Module config " + args[1] + " was not found");
                }
                break;

            case "save":
                if (args.length < 2) {
                    LogUtil.printChat("§cUsage: .m save <name>");
                    return;
                }
                DewCommon.moduleConfigManager.save(args[1], DewCommon.moduleManager.getModules());
                LogUtil.printChat("Saved module config: " + args[1]);
                break;

            case "list":
                LogUtil.printChat("Module Configs:");
                DewCommon.moduleConfigManager.getConfigNames().stream()
                        .sorted(String::compareToIgnoreCase)
                        .forEach(config -> LogUtil.printChat(" " + config));
                break;

            case "folder":
                LogUtil.printChat("Opening module config folder...");
                DewCommon.moduleConfigManager.openFolder();
                break;

            default:
                LogUtil.printChat("§cUsage: .m <load/save/create/folder> <name>");
                break;
        }
    }
}