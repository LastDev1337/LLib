package dev.laststudio.lib.api.action;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface ActionService {

    void register(String name, Action action);

    void unregister(String name);

    List<ParsedAction> parse(List<?> rawActions);

    void execute(CommandSender target, List<ParsedAction> actions);

    boolean isRegistered(String name);
}
