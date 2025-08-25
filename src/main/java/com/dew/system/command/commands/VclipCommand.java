package com.dew.system.command.commands;

import com.dew.system.command.Command;
import com.dew.utils.LogUtil;
import com.dew.utils.PacketUtil;
import net.minecraft.network.play.client.C03PacketPlayer;

public class VclipCommand extends Command {

    public VclipCommand() {
        super("vclip");
    }

    @Override
    public String getUsage() {
        return "vclip <height> - Tp up/down vertically";
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

        if (height > 10 || height < -10) {
            for (int i = 0; i < 10; i++) {
                PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true));
            }
        }

        mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + height, mc.thePlayer.posZ);
        LogUtil.printChat("Teleported");
    }
}