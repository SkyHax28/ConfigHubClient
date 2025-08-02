package com.dew.system.command.commands;

import com.dew.system.command.Command;
import com.dew.utils.LogUtil;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class NameCommand extends Command {
    public NameCommand() {
        super("name");
    }

    @Override
    public void execute(String[] args) {
        String username = mc.thePlayer.getName();
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection stringSelection = new StringSelection(username);
        clipboard.setContents(stringSelection, null);
        LogUtil.printChat("§aUsername: §f" + username + " §b(Copied to your clipboard)");
    }

    @Override
    public String getUsage() {
        return "name - Get current session username and copy them to your clipboard.";
    }

}
