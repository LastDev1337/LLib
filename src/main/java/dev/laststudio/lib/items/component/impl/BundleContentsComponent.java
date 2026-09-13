package dev.laststudio.lib.items.component.impl;

import dev.laststudio.lib.items.ItemModel;
import dev.by1337.yaml.codec.YamlCodec;

import java.util.List;

public record BundleContentsComponent(List<ItemModel> contents) {
    public static final YamlCodec<BundleContentsComponent> CODEC = YamlCodec.lazyLoad(() ->
            ItemModel.CODEC.listOf().map(
                    BundleContentsComponent::new,
                    contents -> contents.contents
            )
    );
}
