package com.dew.system.command.commands;

import com.dew.DewCommon;
import com.dew.system.command.Command;
import com.dew.utils.LogUtil;

public class BindConfigCommand extends Command {

    public BindConfigCommand() {
        super("b");
    }

    @Override
    public String getUsage() {
        return "b <load/save/list> <name> - Customize bind configuration through ConfigManager";
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            LogUtil.printChat("§cUsage: .b <load/save/list> <name>");
            return;
        }

        switch (args[0].toLowerCase()) {
            case "load":
                if (args.length < 2) {
                    LogUtil.printChat("§cUsage: .b load <name>");
                    return;
                }
                if (DewCommon.bindConfigManager.load(args[1], DewCommon.moduleManager.getModules())) {
                    LogUtil.printChat("Loaded bind config: " + args[1]);
                } else {
                    LogUtil.printChat("Bind config " + args[1] + " was not found");
                }
                break;

            case "save":
                if (args.length < 2) {
                    LogUtil.printChat("§cUsage: .b save <name>");
                    return;
                }
                DewCommon.bindConfigManager.save(args[1], DewCommon.moduleManager.getModules());
                LogUtil.printChat("Saved bind config: " + args[1]);
                break;

            case "list":
                LogUtil.printChat("Bind Configs:");
                for (String config : DewCommon.bindConfigManager.getConfigNames()) {
                    LogUtil.printChat(" " + config);
                }
                break;

            default:
                LogUtil.printChat("§cUsage: .b <load/save/create> <name>");
                break;
        }
    }
}