package dev.laststudio.lib.plugin.item;

import dev.laststudio.lib.items.ItemModel;
import dev.laststudio.lib.items.registry.ItemModelHolder;

public record ItemHolder(ItemModel itemModel) implements ItemModelHolder {
}
