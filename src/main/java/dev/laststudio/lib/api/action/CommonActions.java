package dev.laststudio.lib.api.action;

import dev.by1337.particle.ParticleType;
import dev.by1337.particle.particle.ParticleData;
import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.DataResult;
import dev.laststudio.lib.items.ItemModel;
import dev.laststudio.lib.items.util.Placeholders;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import dev.laststudio.lib.api.message.MessageService;
import dev.laststudio.lib.api.particle.ParticleService;

import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class CommonActions {

    private CommonActions() {
    }

    public static void register(ActionService actions, MessageService messages, ParticleService particles) {
        registerText(actions, "message", messages);
        registerText(actions, "chat", messages);
        actions.register("broadcast", (target, args) -> messages.broadcast(args.raw()));

        registerActionBar(actions, "actionbar", messages);

        actions.register("title", (target, args) -> sendTitle(target, args.raw(), messages));
        actions.register("title_all", (target, args) -> sendTitleAll(args.raw(), messages));

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

        actions.register("give", (target, args) -> give(target, args, messages));
        actions.register("give_all", (target, args) -> giveAll(args, messages));
        actions.register("take", (target, args) -> take(target, args));
    }

    private static void give(CommandSender target, ActionArgs args, MessageService messages) {
        Placeholders placeholders = placeholders(messages, target);
        if (args.isNamed()) {
            YamlMap map = args.named();
            Player player = resolvePlayer(target, map.get("player").asString(null));
            if (player == null) return;
            DataResult<ItemModel> result = ItemModel.CODEC.decode(YamlValue.wrap(map));
            if (!result.hasResult()) return;
            giveItem(player, result.getOrThrow().build(placeholders));
            return;
        }
        if (!(target instanceof Player player)) return;
        String[] tokens = args.raw().trim().split("\\s+");
        if (tokens.length == 0 || tokens[0].isEmpty()) return;
        int amount = tokens.length > 1 ? parseInt(tokens[1], 1) : 1;
        ItemStack item = ItemModel.ofMaterial(tokens[0]).withAmount(Math.max(1, amount)).build(placeholders);
        Map<Enchantment, Integer> enchants = parseEnchants(tokens.length > 2 ? tokens[2] : null);
        if (!enchants.isEmpty()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                enchants.forEach((e, l) -> meta.addEnchant(e, l, true));
                item.setItemMeta(meta);
            }
        }
        giveItem(player, item);
    }

    private static void giveAll(ActionArgs args, MessageService messages) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            give(player, args, messages);
        }
    }

    private static Placeholders placeholders(MessageService messages, CommandSender target) {
        return raw -> {
            if (raw == null || raw.indexOf('%') < 0) return raw;
            try {
                return messages.placeholders().setPlaceholders(raw, target);
            } catch (Exception e) {
                return raw;
            }
        };
    }

    private static void take(CommandSender target, ActionArgs args) {
        if (!(target instanceof Player player)) return;
        String[] tokens = args.raw().trim().split("\\s+");
        if (tokens.length == 0 || tokens[0].isEmpty()) return;
        Material material = Material.matchMaterial(tokens[0]);
        if (material == null || material.isAir()) return;
        int amount = tokens.length > 1 ? parseInt(tokens[1], 1) : 1;
        player.getInventory().removeItem(new ItemStack(material, Math.max(1, amount)));
    }

    private static void giveItem(Player player, ItemStack item) {
        if (item == null || item.getType().isAir() || item.getAmount() <= 0) return;
        player.getInventory().addItem(item).values().forEach(left ->
                player.getWorld().dropItemNaturally(player.getLocation(), left));
    }

    private static Player resolvePlayer(CommandSender target, String name) {
        if (name == null || name.isBlank() || name.equals("{player}")) {
            return target instanceof Player player ? player : null;
        }
        return Bukkit.getPlayerExact(name);
    }

    private static Map<Enchantment, Integer> parseEnchants(String raw) {
        Map<Enchantment, Integer> result = new HashMap<>();
        if (raw == null || raw.isBlank()) return result;
        String body = raw.trim();
        if (body.startsWith("{")) body = body.substring(1);
        if (body.endsWith("}")) body = body.substring(0, body.length() - 1);
        for (String part : body.split(";")) {
            String[] kv = part.split(":", 2);
            if (kv.length != 2) continue;
            Enchantment enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(kv[0].trim().toLowerCase(Locale.ROOT)));
            if (enchantment == null) continue;
            result.put(enchantment, parseInt(kv[1], 1));
        }
        return result;
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
        Sound sound = resolveSound(tokens[0]);
        if (sound == null) {
            return;
        }
        float volume = tokens.length > 1 ? parseFloat(tokens[1], 1f) : 1f;
        float pitch = tokens.length > 2 ? parseFloat(tokens[2], 1f) : 1f;
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    private static Sound resolveSound(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        Sound sound = Registry.SOUNDS.get(NamespacedKey.minecraft(key.toLowerCase(Locale.ROOT)));
        if (sound != null) {
            return sound;
        }
        try {
            java.lang.reflect.Field field = Sound.class.getField(key.toUpperCase(Locale.ROOT));
            if (Sound.class.isAssignableFrom(field.getType())) {
                return (Sound) field.get(null);
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return null;
    }

    private static void sendTitle(CommandSender target, String raw, MessageService messages) {
        if (!(target instanceof Player player)) {
            return;
        }
        Title title = parseTitle(raw, messages);
        if (title != null) {
            player.showTitle(title);
        }
    }

    private static void sendTitleAll(String raw, MessageService messages) {
        Title title = parseTitle(raw, messages);
        if (title == null) {
            return;
        }
        Bukkit.getOnlinePlayers().forEach(player -> player.showTitle(title));
    }

    private static Title parseTitle(String raw, MessageService messages) {
        String trimmed = raw.trim();
        if (!trimmed.startsWith("\"")) {
            return null;
        }
        int end = trimmed.indexOf('"', 1);
        if (end < 0) {
            return null;
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
        return Title.title(titleComponent, subtitleComponent, times);
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
