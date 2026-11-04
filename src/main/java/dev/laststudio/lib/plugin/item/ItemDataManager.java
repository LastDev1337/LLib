package dev.laststudio.lib.plugin.item;

import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.YamlValue;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import dev.laststudio.lib.api.item.ItemDataService;
import dev.laststudio.lib.api.item.ItemSnapshot;
import dev.laststudio.lib.plugin.message.GlobalText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.Set;

public final class ItemDataManager implements ItemDataService {

    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final Set<ItemFlag> ALL_ITEM_FLAGS = Set.of(ItemFlag.values());

    @Override
    public Set<ItemFlag> allItemFlags() {
        return ALL_ITEM_FLAGS;
    }

    @Override
    public ItemSnapshot read(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return new ItemSnapshot(item.getType(), item.getAmount(), null, List.of(), Map.of(), null, false, Set.of(), null);
        }
        Integer customModelData = meta.hasCustomModelData() ? meta.getCustomModelData() : null;
        ItemSnapshot.Potion potion = null;
        if (meta instanceof PotionMeta potionMeta) {
            List<PotionEffect> effects = potionMeta.hasCustomEffects()
                    ? List.copyOf(potionMeta.getCustomEffects())
                    : List.of();
            potion = new ItemSnapshot.Potion(
                    potionMeta.getBasePotionType(),
                    effects,
                    potionMeta.hasColor() ? potionMeta.getColor() : null
            );
        }
        return new ItemSnapshot(
                item.getType(),
                item.getAmount(),
                meta.displayName(),
                meta.hasLore() ? List.copyOf(meta.lore()) : List.of(),
                Map.copyOf(meta.getEnchants()),
                customModelData,
                meta.isUnbreakable(),
                Set.copyOf(meta.getItemFlags()),
                potion
        );
    }

    @Override
    public ItemStack write(ItemSnapshot snapshot) {
        ItemStack item = new ItemStack(snapshot.material(), snapshot.amount());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (snapshot.name() != null) {
                meta.displayName(snapshot.name());
            }
            if (!snapshot.lore().isEmpty()) {
                meta.lore(snapshot.lore());
            }
            snapshot.enchantments().forEach((enchantment, level) -> meta.addEnchant(enchantment, level, true));
            if (snapshot.customModelData() != null) {
                meta.setCustomModelData(snapshot.customModelData());
            }
            meta.setUnbreakable(snapshot.unbreakable());
            snapshot.itemFlags().forEach(meta::addItemFlags);
            if (snapshot.potion() != null && meta instanceof PotionMeta potionMeta) {
                ItemSnapshot.Potion potion = snapshot.potion();
                if (potion.baseType() != null) {
                    potionMeta.setBasePotionType(potion.baseType());
                }
                potion.customEffects().forEach(effect -> potionMeta.addCustomEffect(effect, true));
                if (potion.color() != null) {
                    potionMeta.setColor(potion.color());
                }
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public ItemSnapshot fromYaml(YamlMap map) {
        Material material = Material.matchMaterial(map.get("material").asString("STONE"));
        if (material == null) {
            material = Material.STONE;
        }
        int amount = map.get("amount").asInt(1);

        Component name = map.has("name") ? GlobalText.render(map.get("name").asString("")) : null;

        List<Component> lore = new ArrayList<>();
        Function<YamlValue, String> asString = v -> v.asString("");
        for (String line : map.get("lore").asList(asString, List.of())) {
            lore.add(GlobalText.render(line));
        }

        Map<Enchantment, Integer> enchantments = new HashMap<>();
        YamlMap enchMap = map.get("enchantments").asYamlMap().result();
        if (enchMap != null) {
            for (String key : enchMap.getRaw().keySet()) {
                Enchantment enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(key));
                if (enchantment != null) {
                    enchantments.put(enchantment, enchMap.get(key).asInt(1));
                }
            }
        }

        Integer customModelData = map.has("custom-model-data") ? map.get("custom-model-data").asInt(0) : null;
        boolean unbreakable = map.get("unbreakable").asBool(false);

        Set<ItemFlag> flags = new HashSet<>();
        for (String flag : map.get("flags").asList(asString, List.of())) {
            try {
                flags.add(ItemFlag.valueOf(flag));
            } catch (IllegalArgumentException ignored) {
            }
        }

        ItemSnapshot.Potion potion = null;
        YamlMap potionMap = map.get("potion").asYamlMap().result();
        if (potionMap != null) {
            PotionType baseType = null;
            if (potionMap.has("type")) {
                try {
                    baseType = PotionType.valueOf(potionMap.get("type").asString("").toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException ignored) {
                }
            }
            Color color = potionMap.has("color") ? Color.fromRGB(potionMap.get("color").asInt(0xFFFFFF)) : null;

            List<PotionEffect> customEffects = new ArrayList<>();
            Function<YamlValue, YamlMap> asYamlMap = v -> v.asYamlMap().result();
            for (YamlMap effectMap : potionMap.get("effects").asList(asYamlMap, List.of())) {
                if (effectMap == null) {
                    continue;
                }
                PotionEffectType type = Registry.EFFECT.get(NamespacedKey.minecraft(effectMap.get("type").asString("speed")));
                if (type == null) {
                    continue;
                }
                customEffects.add(new PotionEffect(type, effectMap.get("duration").asInt(200), effectMap.get("amplifier").asInt(0)));
            }
            potion = new ItemSnapshot.Potion(baseType, customEffects, color);
        }

        return new ItemSnapshot(material, amount, name, lore, enchantments, customModelData, unbreakable, flags, potion);
    }

    @Override
    public YamlMap toYaml(ItemSnapshot snapshot) {
        YamlMap map = new YamlMap();
        map.set("material", snapshot.material().name());
        map.set("amount", snapshot.amount());
        if (snapshot.name() != null) {
            map.set("name", MINI.serialize(snapshot.name()));
        }
        if (!snapshot.lore().isEmpty()) {
            List<String> lore = new ArrayList<>();
            for (Component line : snapshot.lore()) {
                lore.add(MINI.serialize(line));
            }
            map.set("lore", lore);
        }
        if (!snapshot.enchantments().isEmpty()) {
            YamlMap enchMap = new YamlMap();
            snapshot.enchantments().forEach((enchantment, level) -> enchMap.set(enchantment.getKey().getKey(), level));
            map.set("enchantments", enchMap);
        }
        if (snapshot.customModelData() != null) {
            map.set("custom-model-data", snapshot.customModelData());
        }
        if (snapshot.unbreakable()) {
            map.set("unbreakable", true);
        }
        if (!snapshot.itemFlags().isEmpty()) {
            List<String> flags = new ArrayList<>();
            snapshot.itemFlags().forEach(flag -> flags.add(flag.name()));
            map.set("flags", flags);
        }
        if (snapshot.potion() != null) {
            ItemSnapshot.Potion potion = snapshot.potion();
            YamlMap potionMap = new YamlMap();
            if (potion.baseType() != null) {
                potionMap.set("type", potion.baseType().name());
            }
            if (potion.color() != null) {
                potionMap.set("color", potion.color().asRGB());
            }
            if (!potion.customEffects().isEmpty()) {
                List<Map<String, Object>> effects = new ArrayList<>();
                for (PotionEffect effect : potion.customEffects()) {
                    YamlMap effectMap = new YamlMap();
                    effectMap.set("type", effect.getType().getKey().getKey());
                    effectMap.set("duration", effect.getDuration());
                    effectMap.set("amplifier", effect.getAmplifier());
                    effects.add(effectMap.getRaw());
                }
                potionMap.set("effects", effects);
            }
            map.set("potion", potionMap);
        }
        return map;
    }
}
