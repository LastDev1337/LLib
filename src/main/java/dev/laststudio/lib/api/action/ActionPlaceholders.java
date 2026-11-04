package dev.laststudio.lib.api.action;

import org.bukkit.command.CommandSender;

@FunctionalInterface
public interface ActionPlaceholders {

    String resolve(CommandSender target, String text);
}
