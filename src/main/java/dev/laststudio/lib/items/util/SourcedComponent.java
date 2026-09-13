package dev.laststudio.lib.items.util;

import dev.by1337.yaml.codec.YamlCodec;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Text that remembers its raw source so item name/lore can be re-rendered with fresh
 * placeholders on every build. Legacy {@code &} codes are folded into MiniMessage tags
 * before Adventure parses the string.
 */
public final class SourcedComponent implements ComponentLike {

    public static final YamlCodec<ComponentLike> CODEC = YamlCodec.STRING.map(
            source -> (ComponentLike) new SourcedComponent(source),
            SourcedComponent::sourceOf
    );

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private static final Map<Character, String> LEGACY_TAGS = Map.ofEntries(
            Map.entry('0', "black"), Map.entry('1', "dark_blue"), Map.entry('2', "dark_green"),
            Map.entry('3', "dark_aqua"), Map.entry('4', "dark_red"), Map.entry('5', "dark_purple"),
            Map.entry('6', "gold"), Map.entry('7', "gray"), Map.entry('8', "dark_gray"),
            Map.entry('9', "blue"), Map.entry('a', "green"), Map.entry('b', "aqua"),
            Map.entry('c', "red"), Map.entry('d', "light_purple"), Map.entry('e', "yellow"),
            Map.entry('f', "white"), Map.entry('k', "obfuscated"), Map.entry('l', "bold"),
            Map.entry('m', "strikethrough"), Map.entry('n', "underlined"), Map.entry('o', "italic"),
            Map.entry('r', "reset")
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
        return MINI_MESSAGE.deserialize(legacyToMiniMessage(text));
    }

    private static String sourceOf(ComponentLike componentLike) {
        if (componentLike instanceof SourcedComponent sourced) {
            return sourced.source;
        }
        return LegacyComponentSerializer.legacySection().serialize(componentLike.asComponent());
    }

    private static String legacyToMiniMessage(String raw) {
        if (raw.indexOf('&') < 0 && raw.indexOf('§') < 0) {
            return raw;
        }
        StringBuilder out = new StringBuilder(raw.length() + 16);
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if ((c == '&' || c == '§') && i + 1 < raw.length()) {
                char next = raw.charAt(i + 1);
                if (next == '#' && i + 7 < raw.length() && raw.substring(i + 2, i + 8).matches("[0-9a-fA-F]{6}")) {
                    out.append("<#").append(raw, i + 2, i + 8).append('>');
                    i += 7;
                    continue;
                }
                String tag = LEGACY_TAGS.get(Character.toLowerCase(next));
                if (tag != null) {
                    out.append('<').append(tag).append('>');
                    i++;
                    continue;
                }
            }
            out.append(c);
        }
        return out.toString();
    }
}
