package dev.laststudio.lib.plugin.item;

import dev.laststudio.lib.api.item.ItemRegistryService;
import dev.laststudio.lib.items.ItemModel;
import dev.laststudio.lib.items.registry.GlobalItemRegistry;
import dev.laststudio.lib.items.registry.ItemRegistry;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class ItemRegistryManager implements ItemRegistryService {

    private final Map<String, ItemRegistry<ItemHolder>> registries = new ConcurrentHashMap<>();

    private ItemRegistry<ItemHolder> registry(String space) {
        return registries.computeIfAbsent(space, s -> {
            ItemRegistry<ItemHolder> registry = new ItemRegistry<>(s);
            GlobalItemRegistry.register(registry);
            return registry;
        });
    }

    @Override
    public Set<String> spaces() {
        return Collections.unmodifiableSet(registries.keySet());
    }

    @Override
    public Set<String> ids(String space) {
        ItemRegistry<ItemHolder> registry = registries.get(space);
        return registry == null ? Set.of() : registry.ids();
    }

    @Override
    public void register(String space, String id, ItemModel model) {
        registry(space).register(id, new ItemHolder(model));
    }

    @Override
    public @Nullable ItemModel getModel(String fullId) {
        String[] split = split(fullId);
        return getModel(split[0], split[1]);
    }

    @Override
    public @Nullable ItemModel getModel(String space, String id) {
        ItemRegistry<ItemHolder> registry = registries.get(space);
        if (registry == null) return null;
        ItemHolder holder = registry.byId(id);
        return holder == null ? null : holder.itemModel();
    }

    @Override
    public @Nullable ItemStack getItemStack(String fullId) {
        String[] split = split(fullId);
        return getItemStack(split[0], split[1]);
    }

    @Override
    public @Nullable ItemStack getItemStack(String space, String id) {
        ItemRegistry<ItemHolder> registry = registries.get(space);
        if (registry == null) return null;
        ItemHolder holder = registry.byId(id);
        return holder == null ? null : registry.getItemStack(holder);
    }

    @Override
    public void unregisterSpace(String space) {
        registries.remove(space);
        GlobalItemRegistry.unregister(space);
    }

    @Override
    public void clearAll() {
        for (String space : new ArrayList<>(registries.keySet())) {
            GlobalItemRegistry.unregister(space);
        }
        registries.clear();
    }

    private static String[] split(String fullId) {
        if (fullId == null) return new String[]{DEFAULT_SPACE, ""};
        int index = fullId.indexOf(':');
        if (index < 0) return new String[]{DEFAULT_SPACE, fullId};
        return new String[]{fullId.substring(0, index), fullId.substring(index + 1)};
    }
}
