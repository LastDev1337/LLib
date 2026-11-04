package dev.laststudio.lib.items;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import dev.laststudio.lib.items.util.ItemVersion;
import dev.laststudio.lib.items.util.SourcedComponent;
import dev.laststudio.lib.items.component.BaseComponent;
import dev.laststudio.lib.items.component.ComponentsHolder;
import dev.laststudio.lib.items.component.impl.AttributesComponent;
import dev.laststudio.lib.items.component.impl.ContainerComponent;
import dev.laststudio.lib.items.component.impl.MaterialComponent;
import dev.laststudio.lib.items.util.IntHolder;
import dev.laststudio.lib.items.util.Placeholders;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

public class ItemStackRenderer {

    private static final Attribute HIDE_ATTRIBUTES_MARKER = resolveAttribute("luck");
    private static final NamespacedKey HIDE_ATTRIBUTES_KEY = new NamespacedKey("lastitems", "hide_attributes");

    private static final RenderFilter CONTAINER_FILTER = new RenderFilter().only(
            ItemComponents.MATERIAL,
            ItemComponents.MODEL_DATA,
            ItemComponents.NAME,
            ItemComponents.DAMAGE,
            ItemComponents.AMOUNT,
            ItemComponents.COLOR,
            ItemComponents.ENCHANTMENTS,
            ItemComponents.BASE_POTION,
            ItemComponents.TRIM,
            ItemComponents.ITEM_MODEL
    );

    public static ItemStack render(ItemModel item, Placeholders placeholders, @Nullable Locale locale, @Nullable RenderFilter filter) {
        var cache = item.cached();
        if (cache != null && !item.dirty() && filter == null) {
            return renderDisplay(item, placeholders, cache, locale);
        }
        ComponentsHolder model = filter == null ? item.components() : filter.applyAndGet(item.components().copy());
        var material = model.get(ItemComponents.MATERIAL, MaterialComponent.DEFAULT);

        var baseModel = material.fromGlobalRegistry(placeholders);
        if (baseModel != null) {
            if (!baseModel.has(ItemComponents.MATERIAL)) {
                baseModel = baseModel.withMaterial("dirt");
            }
            return render(baseModel.and(item), placeholders, locale, filter);
        }

        String textures = model.get(ItemComponents.TEXTURES);
        ItemStack result = (textures != null && !textures.isBlank())
                ? buildHead(textures, placeholders)
                : material.create(placeholders);
        ItemMeta im = result.getItemMeta();
        if (im == null) {
            return result;
        }
        var attributes = model.get(ItemComponents.ATTRIBUTES);
        if (attributes != null) {
            for (AttributesComponent.Entry entry : attributes.modifiers()) {
                im.addAttributeModifier(entry.attribute(), entry.modifier());
            }
        }

        var hide = model.get(ItemComponents.HIDE_FLAGS);
        if (hide != null) {
            hide.flags().forEach(im::addItemFlags);
            if (attributes == null
                    && HIDE_ATTRIBUTES_MARKER != null
                    && im.hasItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                    && hasDefaultAttributes(result.getType())) {
                im.addAttributeModifier(HIDE_ATTRIBUTES_MARKER, new AttributeModifier(
                        HIDE_ATTRIBUTES_KEY, 0.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.ANY));
            }
        }
        var potion = model.get(ItemComponents.POTION_CONTENTS);
        if (potion != null) {
            for (PotionEffect potionEffect : potion.contents()) {
                switch (im) {
                    case PotionMeta potionMeta -> potionMeta.addCustomEffect(potionEffect, true);
                    case Arrow arrow -> arrow.addCustomEffect(potionEffect, true);
                    case SuspiciousStewMeta m -> m.addCustomEffect(potionEffect, true);
                    default -> {}
                }
            }
        }
        var basePotion = model.get(ItemComponents.BASE_POTION);
        if (basePotion != null && im instanceof PotionMeta pm) {
            basePotion.apply(pm);
        }

        var color0 = model.get(ItemComponents.COLOR);
        if (color0 != null) {
            var color = color0.toBukkit();
            switch (im) {
                case TropicalFishBucketMeta buket -> {
                    var v = DyeColor.getByColor(color);
                    if (v != null) buket.setBodyColor(v);
                }
                case PotionMeta pm -> pm.setColor(color);
                case MapMeta map -> map.setColor(color);
                case LeatherArmorMeta m -> m.setColor(color);
                case FireworkEffectMeta effectMeta ->
                        effectMeta.setEffect(FireworkEffect.builder().withColor(color).build());
                default -> {
                }
            }
        }
        var enchantments = model.get(ItemComponents.ENCHANTMENTS);
        if (enchantments != null) {
            for (var entry : enchantments.enchantments()) {
                im.addEnchant(entry.enchantment(), entry.lvl(), true);
            }
        }

        var modelData = model.get(ItemComponents.MODEL_DATA);
        if (modelData != null) {
            if (ItemVersion.is1_21_4orNewer()) {
                var component = im.getCustomModelDataComponent();
                component.setFloats(modelData.floats());
                component.setFlags(modelData.flags());
                component.setStrings(modelData.strings());
                component.setColors(modelData.colors());
                im.setCustomModelDataComponent(component);
            } else {
                if (!modelData.floats().isEmpty()) {
                    im.setCustomModelData(modelData.floats().getFirst().intValue());
                }
            }
        }
        Boolean glintOverride = model.get(ItemComponents.ENCHANTMENT_GLINT_OVERRIDE);
        if (glintOverride != null) {
            im.setEnchantmentGlintOverride(glintOverride);
        }
        if (model.getBool(ItemComponents.HIDE_TOOLTIP)) {
            im.setHideTooltip(true);
        }
        Integer maxStackSize = model.get(ItemComponents.MAX_STACK_SIZE);
        if (maxStackSize != null) {
            im.setMaxStackSize(maxStackSize);
        }
        var armorTrim = model.get(ItemComponents.TRIM);
        if (armorTrim != null && armorTrim.has() && im instanceof ArmorMeta armorMeta) {
            armorMeta.setTrim(armorTrim.armorTrim().get());
        }

        if (ItemVersion.is1_21_3orNewer()) {
            if (model.getBool(ItemComponents.GLIDER)) {
                im.setGlider(true);
            }
            var tooltip_style = model.get(ItemComponents.TOOLTIP_STYLE);
            if (tooltip_style != null) {
                im.setTooltipStyle(tooltip_style);
            }
            if (model.has(ItemComponents.ITEM_MODEL)) {
                im.setItemModel(model.get(ItemComponents.ITEM_MODEL));
            }
        }
        if (model.getBool(ItemComponents.UNBREAKABLE)) {
            im.setUnbreakable(true);
        }

        var name = model.get(ItemComponents.NAME);
        if (name != null) {
            im.displayName(render(toComponent(name, placeholders), locale));
        }
        var lore = model.get(ItemComponents.LORE);
        if (lore != null) {
            List<Component> loreComponents = new ArrayList<>();
            lore.forEachLore(line -> applyComponent(line, placeholders, v -> loreComponents.add(render(v, locale))));
            im.lore(loreComponents);
        }
        if (im instanceof Damageable damageable) {
            damageable.setDamage(model.get(ItemComponents.DAMAGE, IntHolder.ZERO).getOrDefault(placeholders, 0));
        }
        if (im instanceof BlockStateMeta bsm) {
            BlockState state = bsm.getBlockState();
            if (state instanceof Container container) {
                ContainerComponent c = model.get(ItemComponents.CONTAINER);
                if (c != null) {
                    for (Map.Entry<Integer, ItemModel> entry : c.items().entrySet()) {
                        container.getInventory().setItem(entry.getKey(), render(entry.getValue(), placeholders, locale, CONTAINER_FILTER));
                    }
                }
                bsm.setBlockState(container);
            }
        }
        if (im instanceof BundleMeta bundleMeta) {
            var content = model.get(ItemComponents.BUNDLE_CONTENTS);
            if (content != null) {
                List<ItemStack> items = new ArrayList<>();
                content.contents().forEach(m -> items.add(render(m, placeholders, locale, CONTAINER_FILTER)));
                bundleMeta.setItems(items);
            }
        }
        if (im instanceof EnchantmentStorageMeta s) {
            var stored = model.get(ItemComponents.STORED_ENCHANTMENTS);
            if (stored != null)
                stored.enchantments().forEach(e -> s.addStoredEnchant(e.enchantment(), e.lvl(), true));
        }

        result.setItemMeta(im);
        result.setAmount(model.get(ItemComponents.AMOUNT, IntHolder.ONE).getOrDefault(placeholders, 1));
        if (material.isFinal() && filter == null) {
            item.setCached(result.clone());
        }
        item.setDirty(false);
        return result;
    }

    private static ItemStack renderDisplay(ItemModel model, Placeholders placeholders, ItemStack cache, @Nullable Locale locale) {
        var result = cache.clone();
        var meta = result.getItemMeta();
        if (meta != null) {
            if (meta instanceof Damageable damageable) {
                damageable.setDamage(model.get(ItemComponents.DAMAGE, IntHolder.ZERO).getOrDefault(placeholders, 0));
            }
            var lore = model.get(ItemComponents.LORE);
            if (lore != null && lore.hasPlaceholdersOrLang()) {
                List<Component> loreComponents = new ArrayList<>();
                lore.forEachLore(line -> applyComponent(line, placeholders, v -> loreComponents.add(render(v, locale))));
                meta.lore(loreComponents);
            }
            var name = model.get(ItemComponents.NAME);
            if (name != null) {
                meta.displayName(render(toComponent(name, placeholders), locale));
            }
            result.setItemMeta(meta);
        }
        result.setAmount(model.get(ItemComponents.AMOUNT, IntHolder.ONE).getOrDefault(placeholders, 1));
        return result;
    }

    private static ItemStack buildHead(String raw, Placeholders placeholders) {
        String value = placeholders.apply(raw).trim();
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        head.editMeta(SkullMeta.class, skull -> {
            if (value.startsWith("http://") || value.startsWith("https://")) {
                String json = "{\"textures\":{\"SKIN\":{\"url\":\"" + value + "\"}}}";
                String b64 = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
                PlayerProfile profile = Bukkit.createProfile(UUID.nameUUIDFromBytes(b64.getBytes(StandardCharsets.UTF_8)));
                profile.setProperty(new ProfileProperty("textures", b64));
                skull.setPlayerProfile(profile);
                return;
            }
            if (value.length() == 36) {
                try {
                    skull.setPlayerProfile(Bukkit.createProfile(UUID.fromString(value)));
                    return;
                } catch (IllegalArgumentException ignored) {
                }
            }
            Player online = Bukkit.getPlayerExact(value);
            if (online != null) {
                skull.setPlayerProfile(online.getPlayerProfile());
                return;
            }
            PlayerProfile profile = Bukkit.createProfile(UUID.nameUUIDFromBytes(value.getBytes(StandardCharsets.UTF_8)));
            profile.setProperty(new ProfileProperty("textures", value));
            skull.setPlayerProfile(profile);
        });
        return head;
    }

    private static Component render(Component c, @Nullable Locale l) {
        if (l == null) return c;
        return GlobalTranslator.render(c, l);
    }

    @Nullable
    private static Attribute resolveAttribute(String key) {
        Attribute attr = Registry.ATTRIBUTE.get(NamespacedKey.minecraft(key));
        return attr != null ? attr : Registry.ATTRIBUTE.get(NamespacedKey.minecraft("generic." + key));
    }

    private static boolean hasDefaultAttributes(Material material) {
        String n = material.name();
        return n.endsWith("_SWORD") || n.endsWith("_AXE") || n.endsWith("_PICKAXE")
                || n.endsWith("_SHOVEL") || n.endsWith("_HOE")
                || n.endsWith("_HELMET") || n.endsWith("_CHESTPLATE")
                || n.endsWith("_LEGGINGS") || n.endsWith("_BOOTS")
                || n.endsWith("_ARMOR")
                || n.equals("TRIDENT") || n.equals("MACE");
    }

    private static void applyComponent(ComponentLike c, Placeholders placeholders, Consumer<Component> processor) {
        if (c instanceof SourcedComponent sourced) {
            sourced.renderLines(placeholders).forEach(processor);
        } else {
            processor.accept(c.asComponent().decoration(TextDecoration.ITALIC, false));
        }
    }

    private static Component toComponent(ComponentLike c, Placeholders placeholders) {
        if (c instanceof SourcedComponent sourced) {
            return sourced.render(placeholders);
        }
        return c.asComponent().decoration(TextDecoration.ITALIC, false);
    }

    public static class RenderFilter {
        private static final int INIT_SIZE = ItemComponents.count();
        final boolean[] components;

        public RenderFilter() {
            components = new boolean[INIT_SIZE];
        }

        public ComponentsHolder applyAndGet(ComponentsHolder b) {
            for (int i = 0; i < components.length; i++) {
                if (components[i]) b.raw()[i] = null;
            }
            return b;
        }

        public RenderFilter only(@Nullable BaseComponent<?>... arr) {
            Arrays.fill(components, true);
            for (BaseComponent<?> c : arr) {
                if (c == null) continue;
                components[c.id()] = false;
            }
            return this;
        }

        public RenderFilter setSkip(@Nullable BaseComponent<?> c) {
            if (c == null) return this;
            components[c.id()] = true;
            return this;
        }

        public RenderFilter setSkip(@Nullable BaseComponent<?>... arr) {
            for (BaseComponent<?> c : arr) {
                if (c == null) continue;
                components[c.id()] = true;
            }
            return this;
        }

        public boolean isSkip(@Nullable BaseComponent<?> c) {
            return c == null || components[c.id()];
        }
    }
}
