package dev.laststudio.lib.api.item;

import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record ItemSnapshot(
        Material material,
        int amount,
        Component name,
        List<Component> lore,
        Map<Enchantment, Integer> enchantments,
        Integer customModelData,
        boolean unbreakable,
        Set<ItemFlag> itemFlags,
        Potion potion) {

    public record Potion(
            PotionType baseType,
            List<PotionEffect> customEffects,
            Color color) { }
}
