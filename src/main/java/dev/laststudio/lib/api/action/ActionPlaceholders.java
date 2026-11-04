package dev.laststudio.lib.api.action;

import org.bukkit.command.CommandSender;

/** Подстановка плейсхолдеров в момент срабатывания (строка действия разбирается один раз заранее). */
@FunctionalInterface
public interface ActionPlaceholders {

    String resolve(CommandSender target, String text);
}
