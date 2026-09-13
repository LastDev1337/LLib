package dev.laststudio.lib.api.command;

import dev.by1337.cmd.Argument;
import dev.by1337.cmd.Command;
import dev.by1337.cmd.Requires;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public interface CommandService {

    Command<CommandSender> command(String name);

    Argument<CommandSender, Integer> intArg(String name);

    Argument<CommandSender, Double> doubleArg(String name);

    Argument<CommandSender, Player> playerArg(String name);

    Requires<CommandSender> permission(String node);

    void register(JavaPlugin owner, Command<CommandSender> command);

    void unregister(Command<CommandSender> command);
}
