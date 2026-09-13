package dev.laststudio.lib.plugin.message;

import dev.by1337.plc.Placeholders;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import dev.laststudio.lib.api.message.ColorType;
import dev.laststudio.lib.api.message.MessageService;

import java.util.List;

public final class MessageManager implements MessageService {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();
    private static final LegacyComponentSerializer HEX =
            LegacyComponentSerializer.builder().character('&').hexColors().build();
    private static final LegacyComponentSerializer LEGACY_OUT = LegacyComponentSerializer.builder()
            .character('§').hexColors().useUnusualXRepeatedCharacterHexFormat().build();
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final GsonComponentSerializer GSON = GsonComponentSerializer.gson();
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    private final ColorType type;
    private final Placeholders<CommandSender> placeholders = Placeholders.create();

    public MessageManager(ColorType type) {
        this.type = type;
    }

    @Override
    public Placeholders<CommandSender> placeholders() {
        return placeholders;
    }

    @Override
    public Component render(String raw) {
        return switch (type) {
            case LEGACY -> LEGACY.deserialize(raw);
            case HEX -> HEX.deserialize(raw);
            case MINI_MESSAGE -> MM.deserialize(raw);
            case GSON -> GSON.deserialize(raw);
            case ALL -> MM.deserialize(LegacyToMiniMessage.convert(raw));
        };
    }

    @Override
    public String escape(String raw) {
        return TagEscaper.escape(type, raw);
    }

    @Override
    public String renderLegacy(String raw) {
        return LEGACY_OUT.serialize(render(raw));
    }

    @Override
    public String plain(String raw) {
        return PLAIN.serialize(render(raw));
    }

    @Override
    public List<Component> renderList(List<String> raw) {
        return raw.stream().map(this::render).toList();
    }

    @Override
    public void send(CommandSender target, String raw) {
        target.sendMessage(render(placeholders.setPlaceholders(raw, target)));
    }

    @Override
    public void sendList(CommandSender target, List<String> raw) {
        raw.forEach(line -> send(target, line));
    }

    @Override
    public void broadcast(String raw) {
        Component component = render(raw);
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(component));
        Bukkit.getConsoleSender().sendMessage(component);
    }
}
