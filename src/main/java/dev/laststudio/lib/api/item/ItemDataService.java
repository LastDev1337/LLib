package dev.laststudio.lib.api.item;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import dev.by1337.yaml.YamlMap;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Set;

public interface ItemDataService {

    Gson ITEM_JSON = new GsonBuilder().setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE).create();

    Set<ItemFlag> allItemFlags();

    ItemSnapshot read(ItemStack item);

    ItemStack write(ItemSnapshot snapshot);

    ItemSnapshot fromYaml(YamlMap map);

    YamlMap toYaml(ItemSnapshot snapshot);

    default String toJson(ItemSnapshot snapshot) {
        return ITEM_JSON.toJson(toYaml(snapshot).getRaw());
    }

    default ItemSnapshot fromJson(String json) {
        LinkedHashMap<String, Object> map = ITEM_JSON.fromJson(json, LinkedHashMap.class);
        return fromYaml(new YamlMap(map != null ? map : new LinkedHashMap<>()));
    }
}
