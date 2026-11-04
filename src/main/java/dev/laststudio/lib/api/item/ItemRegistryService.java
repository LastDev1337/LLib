package dev.laststudio.lib.api.item;

import dev.laststudio.lib.items.ItemModel;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface ItemRegistryService {

    String DEFAULT_SPACE = "llib";

    Set<String> spaces();

    Set<String> ids(String space);

    void register(String space, String id, ItemModel model);

    @Nullable
    ItemModel getModel(String fullId);

    @Nullable
    ItemModel getModel(String space, String id);

    @Nullable
    ItemStack getItemStack(String fullId);

    @Nullable
    ItemStack getItemStack(String space, String id);

    void unregisterSpace(String space);

    void clearAll();
}
