package dev.laststudio.lib.items.util;

import dev.by1337.yaml.codec.YamlCodec;
import dev.laststudio.lib.plugin.message.GlobalText;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.List;

public final class SourcedComponent implements ComponentLike {

    public static final YamlCodec<ComponentLike> CODEC = YamlCodec.STRING.map(
            source -> (ComponentLike) new SourcedComponent(source),
            SourcedComponent::sourceOf
    );

    private final String source;

    public SourcedComponent(String source) {
        this.source = source;
    }

    public String source() {
        return source;
    }

    @Override
    public Component asComponent() {
        return deserialize(source);
    }

    public Component render(Placeholders placeholders) {
        return deserialize(placeholders.apply(source)).decoration(TextDecoration.ITALIC, false);
    }

    public List<Component> renderLines(Placeholders placeholders) {
        List<Component> out = new ArrayList<>();
        for (String line : placeholders.apply(source).split("\n")) {
            out.add(deserialize(line).decoration(TextDecoration.ITALIC, false));
        }
        return out;
    }

    private static Component deserialize(String text) {
        return GlobalText.render(text);
    }

    private static String sourceOf(ComponentLike componentLike) {
        if (componentLike instanceof SourcedComponent sourced) {
            return sourced.source;
        }
        return LegacyComponentSerializer.legacySection().serialize(componentLike.asComponent());
    }
}
