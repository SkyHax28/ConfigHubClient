package com.dew.system.command.commands;

import com.dew.DewCommon;
import com.dew.system.command.Command;
import com.dew.system.module.Module;
import com.dew.utils.LogUtil;

public class HelpCommand extends Command {

    public HelpCommand() {
        super("help");
    }

    @Override
    public String getUsage() {
        return "help - Show this help message";
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            for (Command command : DewCommon.commandManager.getCommands()) {
                LogUtil.printChat(DewCommon.commandManager.commandPrefix + command.getUsage());
            }
        }
    }
}