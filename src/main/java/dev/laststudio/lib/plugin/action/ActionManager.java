package dev.laststudio.lib.plugin.action;

import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.YamlValue;
import org.bukkit.command.CommandSender;
import dev.laststudio.lib.api.action.Action;
import dev.laststudio.lib.api.action.ActionArgs;
import dev.laststudio.lib.api.action.ActionService;
import dev.laststudio.lib.api.action.ParsedAction;

import dev.laststudio.lib.api.action.ActionPlaceholders;
import dev.laststudio.lib.api.locale.LocaleService;
import org.bukkit.Bukkit;
import dev.laststudio.lib.api.scheduler.SchedulerService;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ActionManager implements ActionService {

    private final Map<String, Action> actions = new ConcurrentHashMap<>();
    private static final Pattern CHANCE = Pattern.compile("^\\[\\s*chance\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)\\s*%?\\s*]\\s*", Pattern.CASE_INSENSITIVE);

    private final Logger logger;
    private final SchedulerService scheduler;
    private final LocaleService locale;

    public ActionManager(Logger logger, SchedulerService scheduler, LocaleService locale) {
        this.logger = logger;
        this.scheduler = scheduler;
        this.locale = locale;
    }

    private String text(String key, Object... args) {
        return locale.get(locale.resolve(Bukkit.getConsoleSender()), key, args);
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
                logger.warning(text("action-parse-failed", raw));
            }
        }
        return result;
    }

    @Override
    public void execute(CommandSender target, List<ParsedAction> parsedActions) {
        for (ParsedAction action : parsedActions) {
            Action handler = actions.get(action.name().toLowerCase(Locale.ROOT));
            if (handler == null) {
                logger.warning(text("action-unknown", action.name()));
                continue;
            }
            try {
                handler.execute(target, action.args());
            } catch (Exception e) {
                logger.log(Level.SEVERE, text("action-error", action.name()), e);
            }
        }
    }

    @Override
    public void execute(Plugin owner, Collection<? extends CommandSender> targets, List<ParsedAction> parsedActions, ActionPlaceholders placeholders) {
        if (targets.isEmpty()) {
            return;
        }
        for (ParsedAction action : parsedActions) {
            if (action.chance() < 100.0 && ThreadLocalRandom.current().nextDouble(100.0) >= action.chance()) {
                continue;
            }
            Action handler = actions.get(action.name().toLowerCase(Locale.ROOT));
            if (handler == null) {
                logger.warning(text("action-unknown", action.name()));
                continue;
            }
            for (CommandSender target : targets) {
                ActionArgs args = placeholders == null
                        ? action.args()
                        : action.args().withResolver(text -> placeholders.resolve(target, text));
                Runnable run = () -> {
                    try {
                        handler.execute(target, args);
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, text("action-error", action.name()), e);
                    }
                };
                if (target instanceof Player player) {
                    scheduler.entity(owner, player, run);
                } else {
                    scheduler.global(owner, run);
                }
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
            double chance = 100.0;
            Matcher m = CHANCE.matcher(rest);
            if (m.find()) {
                chance = Math.min(100.0, Double.parseDouble(m.group(1)));
                rest = rest.substring(m.end()).trim();
            }
            return new ParsedAction(name, ActionArgs.raw(rest), chance);
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
            if (named == null) {
                return null;
            }
            double chance = Math.clamp(named.get("chance").asDouble(100.0), 0.0, 100.0);
            return new ParsedAction(name, ActionArgs.named(named), chance);
        }
        return null;
    }
}
