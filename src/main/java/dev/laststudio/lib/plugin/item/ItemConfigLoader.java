package dev.laststudio.lib.plugin.item;

import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.DataResult;
import dev.laststudio.lib.api.item.ItemRegistryService;
import dev.laststudio.lib.items.ItemModel;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

public final class ItemConfigLoader {

    private final JavaPlugin plugin;
    private final ItemRegistryService registries;

    public ItemConfigLoader(JavaPlugin plugin, ItemRegistryService registries) {
        this.plugin = plugin;
        this.registries = registries;
    }

    public void load() {
        File dir = new File(plugin.getDataFolder(), "items");
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                try {
                    plugin.saveResource("items/example.yml", false);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        File[] files = dir.listFiles((d, n) -> n.endsWith(".yml"));
        if (files == null) return;
        for (File file : files) {
            loadFile(file);
        }
    }

    private void loadFile(File file) {
        try {
            YamlMap root = YamlMap.load(file);
            String space = root.get("space").asString(ItemRegistryService.DEFAULT_SPACE).toLowerCase();
            YamlMap items = root.get("items").asYamlMap().result();
            if (items == null) return;

            int count = 0;
            for (String id : items.getRaw().keySet()) {
                YamlMap section = items.get(id).asYamlMap().result();
                if (section == null) continue;
                DataResult<ItemModel> result = ItemModel.CODEC.decode(YamlValue.wrap(section));
                if (!result.hasResult()) {
                    plugin.getLogger().warning("Не удалось разобрать предмет '" + id + "' в " + file.getName() + ": " + result.error());
                    continue;
                }
                try {
                    registries.register(space, id.toLowerCase(), result.getOrThrow());
                    count++;
                } catch (Exception e) {
                    plugin.getLogger().warning("Предмет " + space + ":" + id + " не зарегистрирован: " + e.getMessage());
                }
            }
            plugin.getLogger().info("Загружено " + count + " предмет(ов) из " + file.getName() + " в пространство '" + space + "'");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Не удалось загрузить файл предметов " + file.getName(), e);
        }
    }
}
