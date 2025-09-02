package com.dew.system.command.commands;

import com.dew.DewCommon;
import com.dew.system.command.Command;
import com.dew.system.module.modules.exploit.Plugins;
import com.dew.utils.rawinput.RawInputThread;

public class RescanCommand extends Command {

    public RescanCommand() {
        super("rescan");
    }

    @Override
    public String getUsage() {
        return "rescan - Rescans for raw input new mouses";
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            RawInputThread.INSTANCE.rescan();
        }
    }
}