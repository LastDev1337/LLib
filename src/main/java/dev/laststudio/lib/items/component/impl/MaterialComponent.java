package dev.laststudio.lib.items.component.impl;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import dev.laststudio.lib.items.ItemModel;
import dev.laststudio.lib.items.registry.GlobalItemRegistry;
import dev.laststudio.lib.items.util.Placeholders;
import dev.by1337.yaml.BukkitCodecs;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.codec.YamlCodec;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Nullable;
import java.util.logging.Logger;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class MaterialComponent {
    public static final YamlCodec<MaterialComponent> CODEC =YamlCodec.STRING.schema(s -> s.or(BukkitCodecs.material().schema()))
            .map(MaterialComponent::new, MaterialComponent::input);
    public static final MaterialComponent DEFAULT = new MaterialComponent("dirt");
    private static final Logger log = Logger.getLogger("LLib-Items");
    private final String input;
    private @Nullable ItemStack cashed;
    private final boolean hasNoPlaceholders;
    private @Nullable ItemModel ref;
    private boolean hasRef;

    public MaterialComponent(String input) {
        this.input = input;
        hasNoPlaceholders = hasNoPlaceholders(input);
    }

    public ItemStack create(Placeholders placeholders) {
        if (cashed != null) return cashed.clone();
        ItemStack result = Builder.build(placeholders.apply(input));
        if (hasNoPlaceholders) {
            cashed = result.clone();
        }
        return result;
    }

    public @Nullable ItemModel fromGlobalRegistry(Placeholders placeholders){
        if (hasRef) return ref;
        hasRef = true;
       return ref = GlobalItemRegistry.resolveItemModel(placeholders.apply(input));
    }

    public boolean isFinal(){
        return hasNoPlaceholders;
    }

    private static boolean hasNoPlaceholders(String input) {
        return !input.contains("{") && !input.contains("%");
    }

    public String input() {
        return input;
    }

    private static class Builder {

        private static final YamlCodec<Material> MATERIAL = BukkitCodecs.material();

        private static ItemStack build(String input) {
            if (input.startsWith("basehead-")) {
                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                String url = input.substring("basehead-".length());
                head.editMeta(m -> {
                    PlayerProfile profile = Bukkit.createProfile(
                            UUID.nameUUIDFromBytes(url.getBytes(StandardCharsets.UTF_8)));
                    profile.setProperty(new ProfileProperty("textures", url));
                    ((SkullMeta) (m)).setPlayerProfile(
                            profile
                    );
                });
                return head;
            } else if (input.startsWith("player-")) {
                String value = input.substring("player-".length());
                if (value.length() == 36) {
                    try {
                        UUID uuid = UUID.fromString(value);
                        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                        head.editMeta(m -> ((SkullMeta) (m)).setPlayerProfile(Bukkit.createProfile(uuid)));
                        return head;
                    } catch (Exception ignore) {
                    }
                }
                Player player = Bukkit.getPlayer(value);
                if (player == null) {
                    log.warning("Unknown player " + value);
                    return new ItemStack(Material.PLAYER_HEAD);
                }
                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                head.editMeta(m -> ((SkullMeta) (m)).setOwningPlayer(player));
                return head;
            } else {
                DataResult<Material> res = MATERIAL.decode(YamlValue.wrap(input));
                if (res.hasResult()) return new ItemStack(res.result());
                log.warning("Failed to create item " + input);
                return new ItemStack(Material.DIRT);
            }
        }
    }
}
