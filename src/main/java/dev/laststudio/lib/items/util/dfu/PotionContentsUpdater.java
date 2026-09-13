package dev.laststudio.lib.items.util.dfu;

import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.codec.YamlCodec;
import org.jetbrains.annotations.Nullable;
import java.util.logging.Logger;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PotionContentsUpdater {
    private static final Map<String, String> NAME_UPDATER = Map.of(
            "slow", "slowness",
            "fast_digging", "haste",
            "slow_digging", "mining_fatigue",
            "increase_damage", "strength",
            "heal", "instant_health",
            "harm", "instant_damage",
            "jump", "jump_boost",
            "confusion", "nausea",
            "damage_resistance", "resistance"
    );
    private static final Logger log = Logger.getLogger(PotionContentsUpdater.class.getName());

    @SuppressWarnings("unchecked")
    public static @Nullable Object update(YamlMap item, YamlValue in) {
        YamlMap toMap = potionContentsUpdater(in);
        if (toMap == null) {
            var map = in.asYamlMap();
            if (map.hasResult()) {
                toMap = new YamlMap((LinkedHashMap<String, Object>) YamlUpdater.deepCopy(map.getOrThrow().getRaw()));
            } else {
                log.warning("Failed to update potion_contents: " + map.error());
                return null;
            }
        }
        var raw = toMap.getRaw();
        for (String s : new HashSet<>(raw.keySet())) {
            String newName = NAME_UPDATER.get(s.toLowerCase());
            if (newName == null || raw.containsKey(newName)) continue;
            item.set("$potion_contents-updete-" + s, newName);
            raw.put(newName, raw.remove(s));
        }
        return raw;
    }

    // speed;20;0 -> map{type: "duration amplifier"}
    private static YamlMap potionContentsUpdater(YamlValue value) {
        if (value.isMap()) return null;
        DataResult<List<String>> legacy = YamlCodec.STRINGS.decode(value);
        if (!legacy.hasResult()) {
            log.warning("Failed to update potion_contents: " + legacy.error());
            return null;
        }
        List<String> list = legacy.getOrThrow();
        YamlMap result = new YamlMap();
        for (String s : list) {
            String[] args = s.split(";", 3);
            if (args.length != 3) {
                log.warning("Failed to update potion_contents, expected '<PotionEffectType>;<duration>;<amplifier>' but got '" + s + "'");
                continue;
            }
            result.set(args[0], args[1] + " " + args[2]);
        }
        return result;
    }
}
