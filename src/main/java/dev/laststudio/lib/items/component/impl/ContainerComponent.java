package dev.laststudio.lib.items.component.impl;

import dev.laststudio.lib.items.ItemModel;
import dev.by1337.yaml.codec.YamlCodec;

public record ContainerComponent(java.util.LinkedHashMap<Integer, ItemModel> items) {
    public static final YamlCodec<ContainerComponent> CODEC = YamlCodec.lazyLoad(() ->
            YamlCodec.mapOf(YamlCodec.INT,
                    ItemModel.CODEC
            ).map(
                    map -> new ContainerComponent(new java.util.LinkedHashMap<>(map)),
                    map -> map.items
            )
    );
}
