package dev.laststudio.lib.plugin.command;

import dev.by1337.cmd.Argument;
import dev.by1337.cmd.ArgumentMap;
import dev.by1337.cmd.CommandMsgError;
import dev.by1337.cmd.CommandReader;
import dev.by1337.cmd.SuggestionsList;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import dev.laststudio.lib.api.locale.LocaleService;

import java.util.Locale;

final class PlayerArgument extends Argument<CommandSender, Player> {

    private final LocaleService locale;

    PlayerArgument(String name, LocaleService locale) {
        super(name);
        this.locale = locale;
    }

    @Override
    public void parse(CommandSender ctx, CommandReader reader, ArgumentMap out) throws CommandMsgError {
        String s = reader.readString();
        if (s.isEmpty()) {
            return;
        }
        Player player = Bukkit.getPlayerExact(s);
        if (player == null) {
            throw new CommandMsgError(locale.get(ctx, "unknown-player", s));
        }
        out.put(name(), player);
    }

    @Override
    public void suggest(CommandSender ctx, CommandReader reader, SuggestionsList suggestions, ArgumentMap args) {
        String s = reader.readString().toLowerCase(Locale.ROOT);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (s.isEmpty() || player.getName().toLowerCase(Locale.ROOT).startsWith(s)) {
                suggestions.suggest(player.getName());
            }
        }
    }
}
