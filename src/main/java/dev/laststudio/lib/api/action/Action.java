package dev.laststudio.lib.api.action;

import org.bukkit.command.CommandSender;

@FunctionalInterface
public interface Action {

    void execute(CommandSender target, ActionArgs args);
}
