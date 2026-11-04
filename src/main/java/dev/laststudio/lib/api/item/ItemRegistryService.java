package dev.laststudio.lib.api.item;

import dev.laststudio.lib.items.ItemModel;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Реестр именованных предметов (шаблонов). Предметы описываются один раз под id
 * и переиспользуются по ссылке {@code space:id} (по умолчанию пространство {@code llib}).
 */
public interface ItemRegistryService {

    String DEFAULT_SPACE = "llib";

    /** Все зарегистрированные пространства (namespaces). */
    Set<String> spaces();

    /** id предметов в пространстве. */
    Set<String> ids(String space);

    /** Зарегистрировать предмет под id в пространстве. */
    void register(String space, String id, ItemModel model);

    /** Модель по полному id {@code space:id} или просто {@code id} (пространство по умолчанию). */
    @Nullable
    ItemModel getModel(String fullId);

    @Nullable
    ItemModel getModel(String space, String id);

    /** Готовый ItemStack (с PDC-меткой реестра) по полному id или просто id. */
    @Nullable
    ItemStack getItemStack(String fullId);

    @Nullable
    ItemStack getItemStack(String space, String id);

    /** Удалить всё пространство. */
    void unregisterSpace(String space);

    /** Очистить все пространства (для релоада). */
    void clearAll();
}
