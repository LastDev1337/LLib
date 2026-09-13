package dev.laststudio.lib.items.component.impl;

import dev.by1337.yaml.KeyedYamlCodec;
import dev.by1337.yaml.codec.YamlCodec;
import org.bukkit.Registry;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.Nullable;

public final class BasePotionComponent {

    public static final YamlCodec<BasePotionComponent> CODEC =
            new KeyedYamlCodec<>(Registry.POTION, "PotionType")
                    .map(BasePotionComponent::new, c -> c.type);

    private final PotionType type;

    public BasePotionComponent(PotionType type) {
        this.type = type;
    }

    public PotionType type() {
        return type;
    }

    public void apply(PotionMeta meta) {
        meta.setBasePotionType(type);
    }

    public static @Nullable BasePotionComponent fromMeta(PotionMeta meta) {
        PotionType base = meta.getBasePotionType();
        return base == null ? null : new BasePotionComponent(base);
    }
}
