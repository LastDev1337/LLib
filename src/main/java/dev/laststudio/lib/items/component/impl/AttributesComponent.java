package dev.laststudio.lib.items.component.impl;

import dev.laststudio.lib.items.component.MergeableComponent;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.codec.RecordYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.SchemaType;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public record AttributesComponent(List<Entry> modifiers) implements MergeableComponent<AttributesComponent> {

    public static YamlCodec<AttributesComponent> CODEC = Entry.CODEC.listOf()
            .map(AttributesComponent::new, AttributesComponent::modifiers);

    public record Entry(Attribute attribute, AttributeModifier modifier) {
        public static final YamlCodec<Entry> CODEC = RecordYamlCodecBuilder.mapOf(
                Entry::new,
                Codecs.ATTRIBUTE_CODEC.fieldOf("attribute", Entry::attribute),
                Codecs.ATTRIBUTE_MODIFIER_CODEC.fieldOf(null, Entry::modifier)
        );
    }

    @Override
    public AttributesComponent and(AttributesComponent t1) {
        List<Entry> modifiers = new ArrayList<>(this.modifiers);
        modifiers.addAll(t1.modifiers);
        return new AttributesComponent(modifiers);
    }

    private static final class Codecs {
        private static final YamlCodec<Attribute> ATTRIBUTE_CODEC = make(() -> {
            Map<String, Attribute> normal = new HashMap<>();
            for (Attribute attr : Registry.ATTRIBUTE) {
                normal.put(attr.getKey().toString(), attr);
                String name = attr.getKey().getKey();
                normal.put(name.substring(name.lastIndexOf('.') + 1), attr);
            }
            YamlCodec<Attribute> codec = YamlCodec.lookup(normal);
            return new YamlCodec<Attribute>() {
                @Override
                public DataResult<Attribute> decode(YamlValue yaml) {
                    var res = codec.decode(yaml);
                    if (res.hasResult()) return res;
                    return STRING.decode(yaml).flatMap(s -> {
                        String fixed = s.substring(s.lastIndexOf('.') + 1)
                                .replace("GENERIC_", "")
                                .replace("HORSE_", "")
                                .replace("ZOMBIE_", "");
                        return codec.decode(fixed);
                    });
                }

                @Override
                public YamlValue encode(Attribute attribute) {
                    return codec.encode(attribute);
                }

                @Override
                public @NotNull SchemaType schema() {
                    return codec.schema();
                }
            };
        });

        private static <T> T make(Supplier<T> s) {
            return s.get();
        }

        public static final YamlCodec<AttributeModifier> ATTRIBUTE_MODIFIER_CODEC = AttributeData.CODEC
                .map(AttributeData::toAttributeModifier, AttributeData::fromAttributeModifier);

        private record AttributeData(NamespacedKey key, double amount, AttributeModifier.Operation operation,
                                     EquipmentSlotGroup slot) {

            private static final YamlCodec<AttributeModifier.Operation> OPERATION_CODEC =
                    YamlCodec.fromEnum(AttributeModifier.Operation.class);
            private static final YamlCodec<NamespacedKey> KEY_CODEC =
                    YamlCodec.STRING.map(AttributeData::parseKey, NamespacedKey::toString);
            private static final YamlCodec<EquipmentSlotGroup> SLOT_CODEC =
                    YamlCodec.STRING.map(AttributeData::parseSlot, EquipmentSlotGroup::toString);

            public static final YamlCodec<AttributeData> CODEC = RecordYamlCodecBuilder.mapOf(
                    AttributeData::new,
                    KEY_CODEC.fieldOf("uuid", AttributeData::key, (NamespacedKey) null),
                    YamlCodec.DOUBLE.fieldOf("amount", AttributeData::amount),
                    OPERATION_CODEC.fieldOf("operation", AttributeData::operation),
                    SLOT_CODEC.fieldOf("slot", AttributeData::slot, EquipmentSlotGroup.ANY)
            );

            private AttributeData(@Nullable NamespacedKey key, double amount, AttributeModifier.Operation operation, EquipmentSlotGroup slot) {
                this.key = key != null ? key : new NamespacedKey("lastitems", "generated." + UUID.randomUUID());
                this.amount = amount;
                this.operation = operation;
                this.slot = slot;
            }

            public AttributeModifier toAttributeModifier() {
                return new AttributeModifier(key, amount, operation, slot);
            }

            public static AttributeData fromAttributeModifier(AttributeModifier modifier) {
                return new AttributeData(modifier.getKey(), modifier.getAmount(), modifier.getOperation(), modifier.getSlotGroup());
            }

            private static NamespacedKey parseKey(String raw) {
                if (raw == null || raw.isBlank()) {
                    return new NamespacedKey("lastitems", "generated." + UUID.randomUUID());
                }
                String s = raw.toLowerCase(Locale.ROOT);
                NamespacedKey parsed = NamespacedKey.fromString(s);
                return parsed != null ? parsed : new NamespacedKey("lastitems", s.replaceAll("[^a-z0-9._-]", "_"));
            }

            private static EquipmentSlotGroup parseSlot(String raw) {
                if (raw == null || raw.isBlank()) {
                    return EquipmentSlotGroup.ANY;
                }
                EquipmentSlotGroup group = EquipmentSlotGroup.getByName(raw.toLowerCase(Locale.ROOT));
                if (group != null) {
                    return group;
                }
                try {
                    return EquipmentSlot.valueOf(raw.toUpperCase(Locale.ROOT)).getGroup();
                } catch (IllegalArgumentException e) {
                    return EquipmentSlotGroup.ANY;
                }
            }
        }
    }
}
