package dev.laststudio.lib.plugin.command;

import dev.by1337.cmd.Argument;
import dev.by1337.cmd.Command;
import dev.by1337.cmd.Requires;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import dev.laststudio.lib.api.command.CommandService;
import dev.laststudio.lib.api.locale.LocaleService;
import dev.laststudio.lib.api.message.MessageService;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class CommandManager implements CommandService {

    private final LocaleService locale;
    private final MessageService messages;
    private final Map<Command<CommandSender>, PluginCommand> registered = new IdentityHashMap<>();

    public CommandManager(LocaleService locale, MessageService messages) {
        this.locale = locale;
        this.messages = messages;
    }

    @Override
    public Command<CommandSender> command(String name) {
        return new Command<>(name);
    }

    @Override
    public Argument<CommandSender, Integer> intArg(String name) {
        return new IntArgument(name, locale);
    }

    @Override
    public Argument<CommandSender, Double> doubleArg(String name) {
        return new DoubleArgument(name, locale);
    }

    @Override
    public Argument<CommandSender, Player> playerArg(String name) {
        return new PlayerArgument(name, locale);
    }

    @Override
    public Requires<CommandSender> permission(String node) {
        return new PermissionRequires(node);
    }

    @Override
    public void register(JavaPlugin owner, Command<CommandSender> command) {
        CommandMap map = commandMap();
        clearStale(map, owner, command.name());
        for (String alias : command.aliases()) {
            clearStale(map, owner, alias);
        }
        PluginCommand bukkitCommand = create(command.name(), owner);
        CommandBridge bridge = new CommandBridge(command, locale, messages, owner.getLogger());
        bukkitCommand.setExecutor(bridge);
        bukkitCommand.setTabCompleter(bridge);
        if (!command.aliases().isEmpty()) {
            bukkitCommand.setAliases(List.copyOf(command.aliases()));
        }
        map.register(owner.getName(), bukkitCommand);
        registered.put(command, bukkitCommand);
        syncCommands();
    }

    private static void clearStale(CommandMap map, JavaPlugin owner, String label) {
        org.bukkit.command.Command existing = map.getCommand(label);
        if (!(existing instanceof PluginCommand pluginCommand)) return;
        if (!pluginCommand.getPlugin().getName().equalsIgnoreCase(owner.getName())) return;
        existing.unregister(map);
        if (map instanceof SimpleCommandMap simple) {
            simple.getKnownCommands().values().removeIf(c -> c == existing);
        }
    }

    @Override
    public void unregister(Command<CommandSender> command) {
        PluginCommand bukkitCommand = registered.remove(command);
        if (bukkitCommand == null) {
            return;
        }
        CommandMap map = commandMap();
        bukkitCommand.unregister(map);
        if (map instanceof SimpleCommandMap simple) {
            simple.getKnownCommands().values().removeIf(c -> c == bukkitCommand);
        }
        syncCommands();
    }

    private static void syncCommands() {
        try {
            Bukkit.getServer().getClass().getMethod("syncCommands").invoke(Bukkit.getServer());
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private static PluginCommand create(String name, JavaPlugin owner) {
        try {
            Constructor<PluginCommand> ctor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            ctor.setAccessible(true);
            return ctor.newInstance(name, owner);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to create command '" + name + "'", e);
        }
    }

    private static CommandMap commandMap() {
        try {
            Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            return (CommandMap) field.get(Bukkit.getServer());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to access CommandMap", e);
        }
    }
}
