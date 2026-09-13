package dev.laststudio.lib.plugin.action;

import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.YamlValue;
import org.bukkit.command.CommandSender;
import dev.laststudio.lib.api.action.Action;
import dev.laststudio.lib.api.action.ActionArgs;
import dev.laststudio.lib.api.action.ActionService;
import dev.laststudio.lib.api.action.ParsedAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ActionManager implements ActionService {

    private final Map<String, Action> actions = new ConcurrentHashMap<>();
    private final Logger logger;

    public ActionManager(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void register(String name, Action action) {
        actions.put(name.toLowerCase(Locale.ROOT), action);
    }

    @Override
    public void unregister(String name) {
        actions.remove(name.toLowerCase(Locale.ROOT));
    }

    @Override
    public boolean isRegistered(String name) {
        return actions.containsKey(name.toLowerCase(Locale.ROOT));
    }

    @Override
    public List<ParsedAction> parse(List<?> rawActions) {
        List<ParsedAction> result = new ArrayList<>(rawActions.size());
        for (Object raw : rawActions) {
            ParsedAction parsed = parseOne(raw);
            if (parsed != null) {
                result.add(parsed);
            } else {
                logger.warning("Failed to parse action entry: " + raw);
            }
        }
        return result;
    }

    @Override
    public void execute(CommandSender target, List<ParsedAction> parsedActions) {
        for (ParsedAction action : parsedActions) {
            Action handler = actions.get(action.name().toLowerCase(Locale.ROOT));
            if (handler == null) {
                logger.warning("Unknown action: " + action.name());
                continue;
            }
            try {
                handler.execute(target, action.args());
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error executing action '" + action.name() + "'", e);
            }
        }
    }

    private ParsedAction parseOne(Object raw) {
        if (raw instanceof String s) {
            s = s.trim();
            if (!s.startsWith("[")) {
                return null;
            }
            int end = s.indexOf(']');
            if (end <= 1) {
                return null;
            }
            String name = s.substring(1, end).trim();
            String rest = s.substring(end + 1).trim();
            return new ParsedAction(name, ActionArgs.raw(rest));
        }

        YamlMap map = YamlValue.wrap(raw).asYamlMap().result();
        if (map == null || map.getRaw().size() != 1) {
            return null;
        }
        String name = map.getRaw().keySet().iterator().next();
        Object value = map.getRaw().get(name);
        if (value instanceof String s) {
            return new ParsedAction(name, ActionArgs.raw(s));
        }
        if (value instanceof Map) {
            YamlMap named = map.get(name).asYamlMap().result();
            return named == null ? null : new ParsedAction(name, ActionArgs.named(named));
        }
        return null;
    }
}
