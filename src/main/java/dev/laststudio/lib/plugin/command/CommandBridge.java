package dev.laststudio.lib.plugin.command;

import dev.by1337.cmd.Command;
import dev.by1337.cmd.CommandMsgError;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import dev.laststudio.lib.api.locale.LocaleService;
import dev.laststudio.lib.api.message.MessageService;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

final class CommandBridge implements CommandExecutor, TabCompleter {

    private final Command<CommandSender> command;
    private final LocaleService locale;
    private final MessageService messages;
    private final Logger logger;

    CommandBridge(Command<CommandSender> command, LocaleService locale, MessageService messages, Logger logger) {
        this.command = command;
        this.locale = locale;
        this.messages = messages;
        this.logger = logger;
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        try {
            command.execute(sender, String.join(" ", args));
        } catch (CommandMsgError e) {
            messages.send(sender, e.getMessage());
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Unhandled error executing /" + label, t);
            messages.send(sender, locale.get(sender, "internal-error"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        try {
            return command.suggest(sender, String.join(" ", args)).toList();
        } catch (Throwable t) {
            return List.of();
        }
    }
}
