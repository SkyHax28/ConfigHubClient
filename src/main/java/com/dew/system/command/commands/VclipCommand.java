package com.dew.system.command.commands;

import com.dew.system.command.Command;
import com.dew.utils.LogUtil;

public class VclipCommand extends Command {

    public VclipCommand() {
        super("vclip");
    }

    @Override
    public String getUsage() {
        return "vclip <height> - Tp up vertically";
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            LogUtil.printChat("§cUsage: .vclip <height>");
            return;
        }

        double height;

        try {
            height = Double.parseDouble(args[0]);
        } catch (NumberFormatException e) {
            LogUtil.printChat("§cUsage: .vclip <height>");
            return;
        }

        mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + height, mc.thePlayer.posZ);
        LogUtil.printChat("Teleported");
    }
}