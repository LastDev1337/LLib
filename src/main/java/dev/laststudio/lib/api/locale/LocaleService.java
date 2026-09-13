package dev.laststudio.lib.api.locale;

import org.bukkit.command.CommandSender;

public interface LocaleService {

    String resolve(CommandSender sender);

    String get(String locale, String key, Object... args);

    default String get(CommandSender sender, String key, Object... args) {
        return get(resolve(sender), key, args);
    }
}
