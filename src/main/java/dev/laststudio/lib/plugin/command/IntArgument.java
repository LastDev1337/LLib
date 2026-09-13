package dev.laststudio.lib.plugin.command;

import dev.by1337.cmd.Argument;
import dev.by1337.cmd.ArgumentMap;
import dev.by1337.cmd.CommandMsgError;
import dev.by1337.cmd.CommandReader;
import dev.by1337.cmd.SuggestionsList;
import org.bukkit.command.CommandSender;
import dev.laststudio.lib.api.locale.LocaleService;

final class IntArgument extends Argument<CommandSender, Integer> {

    private final LocaleService locale;

    IntArgument(String name, LocaleService locale) {
        super(name);
        this.locale = locale;
    }

    @Override
    public void parse(CommandSender ctx, CommandReader reader, ArgumentMap out) throws CommandMsgError {
        String s = reader.readString();
        if (s.isEmpty()) {
            return;
        }
        try {
            out.put(name(), Integer.parseInt(s));
        } catch (NumberFormatException e) {
            throw new CommandMsgError(locale.get(ctx, "nan", s));
        }
    }

    @Override
    public void suggest(CommandSender ctx, CommandReader reader, SuggestionsList suggestions, ArgumentMap args) {
        reader.readString();
    }

    @Override
    public boolean allowAsync() {
        return true;
    }
}
