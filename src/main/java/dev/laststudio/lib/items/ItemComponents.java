package dev.laststudio.lib.items;

import com.destroystokyo.paper.profile.ProfileProperty;
import dev.laststudio.lib.items.component.BaseComponent;
import dev.laststudio.lib.items.component.ComponentsHolder;
import dev.laststudio.lib.items.component.impl.*;
import dev.laststudio.lib.items.util.ColorHolder;
import dev.laststudio.lib.items.util.Holder;
import dev.laststudio.lib.items.util.IntHolder;
import dev.laststudio.lib.items.util.ItemVersion;
import dev.laststudio.lib.items.util.SourcedComponent;
import dev.laststudio.lib.items.util.dfu.YamlUpdater;
import dev.by1337.yaml.BukkitCodecs;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.PipelineYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Container;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

public class ItemComponents {
    private static final List<BaseComponent<?>> COMPONENTS = new ArrayList<>();

    public static final BaseComponent<ItemLoreComponent> LORE = register("lore", ItemLoreComponent.CODEC);
    public static final BaseComponent<ComponentLike> NAME = register("name", SourcedComponent.CODEC);
    public static final BaseComponent<IntHolder> AMOUNT = register("amount", IntHolder.CODEC);
    public static final BaseComponent<IntHolder> DAMAGE = register("damage", IntHolder.CODEC);
    public static final BaseComponent<MaterialComponent> MATERIAL = register("material", MaterialComponent.CODEC);
    public static final BaseComponent<CustomModelDataComponent> MODEL_DATA = register("model_data", CustomModelDataComponent.CODEC);
    public static final BaseComponent<PotionContentsComponent> POTION_CONTENTS = register("potion_contents", PotionContentsComponent.CODEC);
    public static final BaseComponent<ColorHolder> COLOR = register("color", ColorHolder.CODEC);
    public static final BaseComponent<EnchantmentsComponent> ENCHANTMENTS = register("enchantments", EnchantmentsComponent.CODEC);
    public static final BaseComponent<Boolean> UNBREAKABLE = register("unbreakable", YamlCodec.BOOL);
    public static final BaseComponent<ContainerComponent> CONTAINER = register("container", ContainerComponent.CODEC);
    public static final BaseComponent<HideFlagsComponents> HIDE_FLAGS = register("item_flags", HideFlagsComponents.CODEC);
    public static final BaseComponent<BasePotionComponent> BASE_POTION = register("potion", BasePotionComponent.CODEC);
    public static final BaseComponent<AttributesComponent> ATTRIBUTES = register("attributes", AttributesComponent.CODEC);
    public static final BaseComponent<StoredEnchantmentsComponent> STORED_ENCHANTMENTS = register("stored_enchantments", StoredEnchantmentsComponent.CODEC);
    public static final BaseComponent<BundleContentsComponent> BUNDLE_CONTENTS = register("bundle_contents", BundleContentsComponent.CODEC);
    public static final BaseComponent<ArmorTrimComponent> TRIM = register("trim", ArmorTrimComponent.CODEC);
    public static final BaseComponent<Boolean> HIDE_TOOLTIP = register("hide_tooltip", YamlCodec.BOOL);
    public static final BaseComponent<Integer> MAX_STACK_SIZE = register("max_stack_size", YamlCodec.INT);
    public static final BaseComponent<Boolean> ENCHANTMENT_GLINT_OVERRIDE = register("enchantment_glint_override", YamlCodec.BOOL);
    public static final BaseComponent<String> TEXTURES = register("textures", YamlCodec.STRING);

    @Nullable
    public static final BaseComponent<Boolean> GLIDER = register("glider", YamlCodec.BOOL, ItemVersion.is1_21_3orNewer());
    @Nullable
    public static final BaseComponent<NamespacedKey> TOOLTIP_STYLE = register("tooltip_style", BukkitCodecs.namespaced_key(), ItemVersion.is1_21_3orNewer());
    @Nullable
    public static final BaseComponent<NamespacedKey> ITEM_MODEL = register("item_model", BukkitCodecs.namespaced_key(), ItemVersion.is1_21_3orNewer());

    public static final YamlCodec<ComponentsHolder> COMPONENTS_CODEC;

    @Nullable
    private static <T> BaseComponent<T> register(String name, YamlCodec<T> codec, boolean supplier) {
        if (supplier) {
            return register(name, codec);
        }
        return null;
    }

    private static <T> BaseComponent<T> register(String name, YamlCodec<T> codec) {
        int id = COMPONENTS.size();
        var component = new BaseComponent<>(id, name, codec);
        COMPONENTS.add(component);
        return component;
    }

    public static int count() {
        return COMPONENTS.size();
    }

    public static List<BaseComponent<?>> list() {
        return Collections.unmodifiableList(COMPONENTS);
    }

    static {
        var builder = PipelineYamlCodecBuilder.of(ComponentsHolder::new);
        for (BaseComponent component : COMPONENTS) {
            builder.field(component.codec(), component.name(),
                    v -> v.get(component),
                    (v, c) -> v.set(component, c)
            );
        }
        COMPONENTS_CODEC = builder
                .build()
                .preDecode(v -> {
                    var res = v.asYamlMap();
                    var map = res.result();
                    if (map != null) {
                        YamlUpdater.fixItem(map);
                        return YamlValue.wrap(map);
                    }
                    return v;
                })
        ;
    }

    @ApiStatus.Internal
    static ComponentsHolder fromItemStack(ItemStack itemStack) {
        ItemMeta im = itemStack.getItemMeta();
        if (im == null) return new ComponentsHolder();
        ComponentsHolder result = new ComponentsHolder();
        var lore = im.lore();
        if (lore != null) {
            result.set(ItemComponents.LORE, new ItemLoreComponent(new ArrayList<>(lore)));
        }
        var name = im.displayName();
        if (name != null) {
            result.set(ItemComponents.NAME, name);
        }
        result.set(ItemComponents.AMOUNT, new IntHolder(itemStack.getAmount()));
        if (im instanceof Damageable damageable) {
            result.set(ItemComponents.DAMAGE, new IntHolder(damageable.getDamage()));
        }
        var attributes = im.getAttributeModifiers();
        if (attributes != null) {
            List<AttributesComponent.Entry> modifiers = new ArrayList<>();
            attributes.forEach((k, v) -> modifiers.add(new AttributesComponent.Entry(k, v)));
            result.set(ItemComponents.ATTRIBUTES, new AttributesComponent(modifiers));
        }
        if (im instanceof SkullMeta skull) {
            var profile = skull.getPlayerProfile();
            if (profile != null) {
                for (ProfileProperty property : profile.getProperties()) {
                    if (property.getName().equals("textures")) {
                        result.set(ItemComponents.MATERIAL, new MaterialComponent("basehead-" + property.getValue()));
                        result.set(ItemComponents.TEXTURES, property.getValue());
                        break;
                    }
                }
                if (result.get(ItemComponents.MATERIAL) == null) {
                    var uuid = profile.getId();
                    if (uuid != null) {
                        result.set(ItemComponents.MATERIAL, new MaterialComponent("player-" + uuid));
                    }
                }
            }
        }
        if (result.get(ItemComponents.MATERIAL) == null) {
            String material = itemStack.getType().getKey().asString();
            result.set(ItemComponents.MATERIAL, new MaterialComponent(material));
        }
        if (ItemVersion.is1_21_4orNewer()) {
            var v = im.getCustomModelDataComponent();
            if (v != null) {
                result.set(ItemComponents.MODEL_DATA, new CustomModelDataComponent(
                        v.getFloats(),
                        v.getFlags(),
                        v.getStrings(),
                        v.getColors()
                ));
            }
        } else if (im.hasCustomModelData()) {
            var v = im.getCustomModelData();
            result.set(ItemComponents.MODEL_DATA, new CustomModelDataComponent(
                    List.of(((Integer) v).floatValue()),
                    List.of(),
                    List.of(),
                    List.of()
            ));
        }
        if (im instanceof PotionMeta potionMeta) {
            if (potionMeta.hasCustomEffects()) {
                result.set(ItemComponents.POTION_CONTENTS, new PotionContentsComponent(potionMeta.getCustomEffects()));
            }
            if (potionMeta.hasColor()) {
                result.set(ItemComponents.COLOR, ColorHolder.fromBukkit(potionMeta.getColor()));
            }
            var v = BasePotionComponent.fromMeta(potionMeta);
            if (v != null)
                result.set(ItemComponents.BASE_POTION, v);
        }
        if (im instanceof LeatherArmorMeta m) {
            result.set(ItemComponents.COLOR, ColorHolder.fromBukkit(m.getColor()));
        }
        {
            var map = im.getEnchants();
            if (!map.isEmpty()) {
                result.set(ItemComponents.ENCHANTMENTS, EnchantmentsComponent.fromMap(map));
            }
        }
        if (im.isUnbreakable()) {
            result.set(ItemComponents.UNBREAKABLE, true);
        }
        if (im instanceof BlockStateMeta state && state.getBlockState() instanceof Container container) {
            LinkedHashMap<Integer, ItemModel> map = new LinkedHashMap<>();
            var inv = container.getInventory();
            var arr = inv.getStorageContents();
            for (int i = 0; i < arr.length; i++) {
                var item = arr[i];
                if (item == null || item.getType().isAir()) continue;
                map.put(i, ItemModel.fromItemStack(item));
            }
            result.set(ItemComponents.CONTAINER, new ContainerComponent(map));
        }
        if (im instanceof ArmorMeta armorMeta) {
            var trim = armorMeta.getTrim();
            if (trim != null) {
                result.set(ItemComponents.TRIM, new ArmorTrimComponent(new Holder<>(trim.getMaterial()), new Holder<>(trim.getPattern())));
            }
        }
        if (im.isHideTooltip()) {
            result.set(ItemComponents.HIDE_TOOLTIP, true);
        }
        if (im.hasEnchantmentGlintOverride()) {
            result.set(ItemComponents.ENCHANTMENT_GLINT_OVERRIDE, im.getEnchantmentGlintOverride());
        }
        if (im.hasMaxStackSize()) {
            result.set(ItemComponents.MAX_STACK_SIZE, im.getMaxStackSize());
        }
        if (ItemVersion.is1_21_3orNewer()) {
            if (im.isGlider()) {
                result.set(ItemComponents.GLIDER, true);
            }
            if (im.hasTooltipStyle()) {
                result.set(ItemComponents.TOOLTIP_STYLE, im.getTooltipStyle());
            }
            if (im.hasItemModel()) {
                result.set(ItemComponents.ITEM_MODEL, im.getItemModel());
            }
        }
        if (im instanceof BundleMeta bundleMeta) {
            var items = bundleMeta.getItems();
            List<ItemModel> itemModels = new ArrayList<>(items.size());
            items.forEach(v -> itemModels.add(ItemModel.fromItemStack(v)));
            result.set(ItemComponents.BUNDLE_CONTENTS, new BundleContentsComponent(itemModels));
        }
        var set = im.getItemFlags();
        if (!set.isEmpty()) {
            result.set(ItemComponents.HIDE_FLAGS, new HideFlagsComponents(set));
        }
        if (im instanceof EnchantmentStorageMeta s && s.hasStoredEnchants()){
            List<StoredEnchantmentsComponent.Entry> list = new ArrayList<>();
            s.getStoredEnchants().forEach((e, lvl) -> list.add(new StoredEnchantmentsComponent.Entry(e, lvl)));
            result.set(ItemComponents.STORED_ENCHANTMENTS, new StoredEnchantmentsComponent(list));
        }
        return result;
    }
}
