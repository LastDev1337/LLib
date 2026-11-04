package dev.laststudio.lib.plugin.message;

import dev.laststudio.lib.api.message.ColorType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * Глобальный текстовый рендер, управляемый секцией {@code color.type} из config.yml.
 * Используется и сообщениями, и предметами (name/lore), чтобы поведение совпадало.
 */
public final class GlobalText {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();
    private static final LegacyComponentSerializer HEX =
            LegacyComponentSerializer.builder().character('&').hexColors().build();
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final GsonComponentSerializer GSON = GsonComponentSerializer.gson();

    private static volatile ColorType type = ColorType.ALL;

    private GlobalText() {
    }

    public static void set(ColorType colorType) {
        if (colorType != null) type = colorType;
    }

    public static ColorType type() {
        return type;
    }

    public static Component render(String raw) {
        if (raw == null || raw.isEmpty()) return Component.empty();
        return switch (type) {
            case LEGACY -> LEGACY.deserialize(raw);
            case HEX -> HEX.deserialize(raw);
            case MINI_MESSAGE -> MM.deserialize(raw);
            case GSON -> GSON.deserialize(raw);
            case ALL -> MM.deserialize(LegacyToMiniMessage.convert(raw));
        };
    }
}
