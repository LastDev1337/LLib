package dev.laststudio.lib.plugin.command;

import dev.by1337.cmd.Requires;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class PermissionRequires implements Requires<CommandSender> {

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
