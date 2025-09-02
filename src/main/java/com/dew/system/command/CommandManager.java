package com.dew.system.command;

import com.dew.system.command.commands.*;
import com.dew.utils.LogUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandManager {
    private final List<Command> commands = new ArrayList<>();

    public String commandPrefix = ".";

    public CommandManager() {
        commands.addAll(Arrays.asList(
                new HelpCommand(), new ToggleCommand(), new BindCommand(), new ModuleConfigCommand(),
                new BindConfigCommand(), new VclipCommand(), new NameCommand(), new PluginsCommand(),
                new RescanCommand()
        ));

        LogUtil.infoLog("init commandManager");
    }

    public List<Command> getCommands() {
        return commands;
    }

    public void handleCommand(String cmd) {
        if (cmd.isEmpty()) {
            LogUtil.printChat("§cUnknown command: ");
            return;
        }

        String[] parts = cmd.split(" ");
        String commandName = parts[0];
        String[] args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length);

        for (Command command : commands) {
            if (command.getName().equalsIgnoreCase(commandName)) {
                command.execute(args);
                return;
            }
        }

        LogUtil.printChat("§cUnknown command: " + commandName);
    }
}