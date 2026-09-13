package dev.laststudio.lib.plugin.message;

import net.kyori.adventure.text.minimessage.MiniMessage;
import dev.laststudio.lib.api.message.ColorType;

public final class TagEscaper {

    private TagEscaper() {
    }

    public static String escape(ColorType type, String value) {
        if (value == null) {
            return "";
        }
        return switch (type) {
            case GSON -> value.replace("\\", "\\\\").replace("\"", "\\\"");
            case MINI_MESSAGE, ALL -> stripLegacyCodes(MiniMessage.miniMessage().escapeTags(value));
            case LEGACY, HEX -> stripLegacyCodes(value);
        };
    }

    private static String stripLegacyCodes(String value) {
        return value.replace("&", "&​").replace("§", "§​");
    }
}
