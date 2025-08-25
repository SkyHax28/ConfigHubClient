package com.dew.system.command.commands;

import com.dew.DewCommon;
import com.dew.system.command.Command;
import com.dew.system.module.modules.exploit.Plugins;
import com.dew.utils.LogUtil;

public class PluginsCommand extends Command {

    public PluginsCommand() {
        super("plugins");
    }

    @Override
    public String getUsage() {
        return "plugins - Enable plugins module";
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            DewCommon.moduleManager.getModule(Plugins.class).setState(true);
        }
    }
}