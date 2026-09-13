package dev.laststudio.lib.items.registry;

import dev.laststudio.lib.items.ItemModel;

@FunctionalInterface
public interface ItemModelHolder {
    ItemModel itemModel();
}
