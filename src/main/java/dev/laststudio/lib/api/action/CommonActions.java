package dev.laststudio.lib.api.action;

import dev.by1337.particle.ParticleType;
import dev.by1337.particle.particle.ParticleData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import dev.laststudio.lib.api.message.MessageService;
import dev.laststudio.lib.api.particle.ParticleService;

import java.time.Duration;
import java.util.Locale;

public final class CommonActions {

    private CommonActions() {
    }

    public static void register(ActionService actions, MessageService messages, ParticleService particles) {
        registerText(actions, "message", messages);
        registerText(actions, "chat", messages);
        actions.register("broadcast", (target, args) -> messages.broadcast(args.raw()));

        registerActionBar(actions, "actionbar", messages);

        actions.register("title", (target, args) -> sendTitle(target, args.raw(), messages));

        registerSound(actions, "sound", messages);

        actions.register("console", (target, args) -> {
            String cmd = args.raw().replace("\r", "").replace("\n", " ").trim();
            if (!cmd.isEmpty()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }
        });
        actions.register("player", (target, args) -> {
            if (target instanceof Player player) {
                String cmd = args.raw().replace("\r", "").replace("\n", " ").trim();
                if (!cmd.isEmpty()) {
                    if (cmd.startsWith("/")) {
                        cmd = cmd.substring(1).trim();
                    }
                    player.performCommand(cmd);
                }
            }
        });

        actions.register("particle", (target, args) -> spawnParticle(target, args, particles));
    }

    private static void registerText(ActionService actions, String name, MessageService messages) {
        actions.register(name, (target, args) -> {
            Audience audience = Audience.parse(args.raw());
            if (audience.all()) {
                messages.broadcast(audience.rest());
            } else {
                messages.send(target, audience.rest());
            }
        });
        actions.register(name + "_all", (target, args) -> messages.broadcast(args.raw()));
    }

    private static void registerActionBar(ActionService actions, String name, MessageService messages) {
        actions.register(name, (target, args) -> {
            Audience audience = Audience.parse(args.raw());
            Component component = messages.render(audience.rest());
            if (audience.all()) {
                Bukkit.getOnlinePlayers().forEach(p -> p.sendActionBar(component));
            } else if (target instanceof Player player) {
                player.sendActionBar(component);
            }
        });
        actions.register(name + "_all", (target, args) -> {
            Component component = messages.render(args.raw());
            Bukkit.getOnlinePlayers().forEach(p -> p.sendActionBar(component));
        });
    }

    private static void registerSound(ActionService actions, String name, MessageService messages) {
        actions.register(name, (target, args) -> {
            Audience audience = Audience.parse(args.raw());
            if (audience.all()) {
                Bukkit.getOnlinePlayers().forEach(p -> playSound(p, audience.rest()));
            } else if (target instanceof Player player) {
                playSound(player, audience.rest());
            }
        });
        actions.register(name + "_all", (target, args) ->
                Bukkit.getOnlinePlayers().forEach(p -> playSound(p, args.raw())));
    }

    private static void playSound(Player player, String raw) {
        String[] tokens = raw.trim().split("\\s+");
        if (tokens.length == 0 || tokens[0].isEmpty()) {
            return;
        }
        Sound sound = Registry.SOUNDS.get(NamespacedKey.minecraft(tokens[0].toLowerCase(Locale.ROOT)));
        if (sound == null) {
            return;
        }
        float volume = tokens.length > 1 ? parseFloat(tokens[1], 1f) : 1f;
        float pitch = tokens.length > 2 ? parseFloat(tokens[2], 1f) : 1f;
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    private static void sendTitle(CommandSender target, String raw, MessageService messages) {
        if (!(target instanceof Player player)) {
            return;
        }
        String trimmed = raw.trim();
        if (!trimmed.startsWith("\"")) {
            return;
        }
        int end = trimmed.indexOf('"', 1);
        if (end < 0) {
            return;
        }
        String text = trimmed.substring(1, end);
        String[] lines = text.split("\\\\n", 2);
        Component titleComponent = messages.render(lines[0]);
        Component subtitleComponent = lines.length > 1 ? messages.render(lines[1]) : Component.empty();

        String[] rest = trimmed.substring(end + 1).trim().split("[\\s;]+");
        int fadeIn = 10, stay = 70, fadeOut = 20;
        if (rest.length >= 3 && !rest[0].isEmpty()) {
            fadeIn = parseInt(rest[0], fadeIn);
            stay = parseInt(rest[1], stay);
            fadeOut = parseInt(rest[2], fadeOut);
        }
        Title.Times times = Title.Times.times(ticks(fadeIn), ticks(stay), ticks(fadeOut));
        player.showTitle(Title.title(titleComponent, subtitleComponent, times));
    }

    private static void spawnParticle(CommandSender target, ActionArgs args, ParticleService particles) {
        if (!(target instanceof Player player)) {
            return;
        }
        String[] tokens = args.raw().trim().split("\\s+");
        if (tokens.length == 0 || tokens[0].isEmpty()) {
            return;
        }
        ParticleType type;
        try {
            type = ParticleType.valueOf(tokens[0].toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return;
        }
        int count = tokens.length > 1 ? parseInt(tokens[1], 1) : 1;
        float speed = tokens.length > 2 ? parseFloat(tokens[2], 0f) : 0f;
        float offX = 0, offY = 0, offZ = 0;
        if (tokens.length > 3) {
            String[] offset = tokens[3].split(";");
            if (offset.length == 3) {
                offX = parseFloat(offset[0], 0f);
                offY = parseFloat(offset[1], 0f);
                offZ = parseFloat(offset[2], 0f);
            }
        }
        ParticleData data = ParticleData.builder()
                .particle(type)
                .count(count)
                .maxSpeed(speed)
                .xDist(offX).yDist(offY).zDist(offZ)
                .build();
        Location location = player.getLocation();
        particles.spawn(player, data, location);
    }

    private static Duration ticks(int ticks) {
        return Duration.ofMillis(ticks * 50L);
    }

    private static int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static float parseFloat(String s, float def) {
        try {
            return Float.parseFloat(s.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private record Audience(boolean all, String rest) {
        static Audience parse(String raw) {
            String trimmed = raw.trim();
            if (trimmed.regionMatches(true, 0, "[all]", 0, 5)) {
                return new Audience(true, trimmed.substring(5).trim());
            }
            return new Audience(false, trimmed);
        }
    }
}
