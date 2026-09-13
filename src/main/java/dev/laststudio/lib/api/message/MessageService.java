package dev.laststudio.lib.api.message;

import dev.by1337.plc.Placeholders;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

import java.util.List;

public interface MessageService {

    Placeholders<CommandSender> placeholders();

    Component render(String raw);

    String escape(String raw);

    String renderLegacy(String raw);

    String plain(String raw);

    List<Component> renderList(List<String> raw);

    void send(CommandSender target, String raw);

    void sendList(CommandSender target, List<String> raw);

    void broadcast(String raw);
}
