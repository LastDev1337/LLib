package dev.laststudio.lib.plugin;

import dev.by1337.cmd.Command;
import dev.by1337.particle.ParticleRenderBootstrapper;
import dev.by1337.yaml.YamlMap;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import dev.laststudio.lib.api.LLibAPI;
import dev.laststudio.lib.api.LLibProvider;
import dev.laststudio.lib.api.action.CommonActions;
import dev.laststudio.lib.api.message.ColorType;
import dev.laststudio.lib.plugin.action.ActionManager;
import dev.laststudio.lib.plugin.action.economy.EconomyActions;
import dev.laststudio.lib.plugin.scheduler.SchedulerManager;
import dev.laststudio.lib.plugin.command.CommandManager;
import dev.laststudio.lib.plugin.item.ItemConfigLoader;
import dev.laststudio.lib.plugin.item.ItemDataManager;
import dev.laststudio.lib.plugin.item.ItemRegistryManager;
import dev.laststudio.lib.plugin.libs.LibraryBootstrap;
import dev.laststudio.lib.plugin.locale.LocaleManager;
import dev.laststudio.lib.plugin.message.GlobalText;
import dev.laststudio.lib.plugin.message.MessageManager;
import dev.laststudio.lib.plugin.particle.ParticleManager;
import dev.laststudio.lib.plugin.version.VersionManager;

import java.io.File;
import java.util.Locale;
import java.util.logging.Level;

public final class LLibPlugin extends JavaPlugin {

    private LocaleManager locale;
    private CommandManager commands;
    private Command<CommandSender> reloadCommand;
    private ParticleRenderBootstrapper particles;
    private ItemRegistryManager itemRegistries;
    private ItemConfigLoader itemLoader;

    @Override
    public void onLoad() {
        LibraryBootstrap.bootstrap(this);
    }

    @Override
    public void onEnable() {
        getLogger().info("enabling...");
        VersionManager version = new VersionManager();
        if (!version.isSupported()) {
            getLogger().warning("Server version " + version.current() + " is not supported ("
                    + version.minSupported() + " - " + version.maxSupported() + "), disabling");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("Detected server version: " + version.current());

        ColorType colorType = loadColorType();
        GlobalText.set(colorType);
        MessageManager messages = new MessageManager(colorType);

        locale = new LocaleManager(colorType);
        loadLocale();

        commands = new CommandManager(locale, messages);

        particles = new ParticleRenderBootstrapper("lib", this);
        particles.enable();

        ParticleManager particleManager = new ParticleManager();
        SchedulerManager scheduler = new SchedulerManager();
        ActionManager actionManager = new ActionManager(getLogger(), scheduler, locale);
        ItemDataManager itemManager = new ItemDataManager();
        CommonActions.register(actionManager, messages, particleManager);
        EconomyActions.register(actionManager, getLogger(), locale);

        itemRegistries = new ItemRegistryManager();
        itemLoader = new ItemConfigLoader(this, itemRegistries);
        itemLoader.load();

        LLibAPI.registerProvider(new LLibProviderImpl(
                locale, messages, version, commands, itemManager, itemRegistries, particleManager, actionManager, scheduler));

        registerReloadCommand();

        Metrics metrics = new Metrics(this, 33292);

        metrics.addCustomChart(
                new SimplePie("chart_id", () -> "LLib")
        );

        getLogger().info("enabling successfully");
    }

    @Override
    public void onDisable() {
        if (commands != null && reloadCommand != null) {
            commands.unregister(reloadCommand);
        }
        if (particles != null) {
            particles.disable();
        }
        LLibAPI.unregisterProvider();
    }

    private ColorType loadColorType() {
        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            saveResource("config.yml", false);
        }
        try {
            String type = YamlMap.load(file).get("color.type").asString("ALL");
            return ColorType.valueOf(type.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to load color.type from config.yml, falling back to ALL", e);
            return ColorType.ALL;
        }
    }

    private void loadLocale() {
        File file = new File(getDataFolder(), "translation.json");
        if (!file.exists()) {
            saveResource("translation.json", false);
        }
        try {
            locale.load(file);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to load translation.json", e);
        }
    }

    private void registerReloadCommand() {
        reloadCommand = commands.command("llib")
                .aliases("lastlib", "lib")
                .requires(commands.permission("llib.reload"))
                .executor(sender -> {
                    long start = System.currentTimeMillis();
                    ColorType colorType = loadColorType();
                    GlobalText.set(colorType);
                    loadLocale();
                    if (itemRegistries != null) itemRegistries.clearAll();
                    if (itemLoader != null) itemLoader.load();
                    long took = System.currentTimeMillis() - start;
                    LLibProvider api = LLibAPI.get();
                    api.messages().send(sender, api.locale().get(sender, "reload", took));
                });
        commands.register(this, reloadCommand);
    }
}
