package dev.laststudio.lib.items.component.impl;

import dev.laststudio.lib.items.util.Holder;
import dev.by1337.yaml.KeyedYamlCodec;
import dev.by1337.yaml.codec.RecordYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;
import org.bukkit.Registry;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

public record ArmorTrimComponent(Holder<ArmorTrim> armorTrim) {

    public static final YamlCodec<ArmorTrimComponent> CODEC = RecordYamlCodecBuilder.mapOf(
            ArmorTrimComponent::new,
            new KeyedYamlCodec<>(Registry.TRIM_MATERIAL, "trim_material")
                    .map(Holder::new, Holder::get)
                    .fieldOf("material", v -> new Holder<>(v.armorTrim.get().getMaterial())),
            new KeyedYamlCodec<>(Registry.TRIM_PATTERN, "trim_pattern")
                    .map(Holder::new, Holder::get)
                    .fieldOf("pattern", v -> new Holder<>(v.armorTrim.get().getPattern()))
    );

    public ArmorTrimComponent(Holder<TrimMaterial> material, Holder<TrimPattern> pattern) {
        this(new Holder<>(new ArmorTrim(material.get(), pattern.get())));
    }

    public boolean has() {
        return armorTrim.has();
    }
}
