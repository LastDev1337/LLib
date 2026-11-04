package dev.laststudio.lib.api.action;

import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;
import org.bukkit.plugin.Plugin;

public interface ActionService {

    void register(String name, Action action);

    void unregister(String name);

    List<ParsedAction> parse(List<?> rawActions);

    void execute(CommandSender target, List<ParsedAction> actions);

    /**
     * Выполнить действия для нескольких целей. Порядок: бросок шанса (один на действие) ->
     * каждая цель получает действие в своём потоке (игрок - в потоке игрока, остальные - в глобальном).
     * Ошибка в одном действии пишется в лог и не мешает остальным.
     *
     * @param placeholders может быть null
     */
    void execute(Plugin owner, Collection<? extends CommandSender> targets, List<ParsedAction> actions, ActionPlaceholders placeholders);

    boolean isRegistered(String name);
}
