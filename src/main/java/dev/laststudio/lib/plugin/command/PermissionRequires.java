package dev.laststudio.lib.plugin.command;

import dev.by1337.cmd.Requires;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Проверка права без побочных эффектов.
 *
 * <p>Раньше {@code CommandManager.permission(...)} слал сообщение прямо из
 * {@link Requires#test} — из-за этого при автодополнении (Tab) BCmd вызывает
 * проверки для всех подкоманд и сообщение спамилось.
 *
 * <p>Теперь факт отказа лишь фиксируется в {@link #DENIED}, а само сообщение
 * отправляет {@link CommandBridge#onCommand} один раз уже при выполнении.
 */
public final class PermissionRequires implements Requires<CommandSender> {

    /** Нода, на которой был отказ во время текущего выполнения (иначе null). */
    static final ThreadLocal<String> DENIED = new ThreadLocal<>();

    private final String node;

    public PermissionRequires(String node) {
        this.node = node;
    }

    @Override
    public boolean test(@NotNull CommandSender sender) {
        if (sender.hasPermission(node)) {
            return true;
        }
        DENIED.set(node);
        return false;
    }

    public String node() {
        return node;
    }
}
