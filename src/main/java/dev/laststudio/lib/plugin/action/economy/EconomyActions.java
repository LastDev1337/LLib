package dev.laststudio.lib.plugin.action.economy;

import dev.laststudio.lib.api.action.ActionArgs;
import dev.laststudio.lib.api.action.ActionService;
import dev.laststudio.lib.api.locale.LocaleService;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * [vault] give|take|set <сумма>      /      vault: { mode: give, amount: 100 }
 * [playerpoints] give|take|set <сумма>
 * Нет плагина экономики - действие тихо пропускается, одно предупреждение в лог.
 * take при нехватке денег не выполняется вообще.
 */
public final class EconomyActions {

    private EconomyActions() {
    }

    // лямбды, а не ссылки на методы: класс хука должен грузиться только при реальном вызове
    public static void register(ActionService actions, Logger logger, LocaleService locale) {
        register(actions, logger, locale, "vault", "Vault", () -> VaultHook.create());
        register(actions, logger, locale, "playerpoints", "PlayerPoints", () -> PlayerPointsHook.create());
    }

    private static void register(ActionService actions, Logger logger, LocaleService locale, String name, String pluginName, Supplier<EconomyHook> factory) {
        EconomyHolder holder = new EconomyHolder(logger, locale, name, pluginName, factory);
        actions.register(name, (target, args) -> {
            if (!(target instanceof Player player)) return;
            EconomyHook hook = holder.get();
            if (hook == null) return;
            apply(hook, player, args, logger, locale, name);
        });
    }

    private static void apply(EconomyHook hook, Player player, ActionArgs args, Logger logger, LocaleService locale, String name) {
        java.util.function.BiFunction<String, Object[], String> text = (key, a) -> locale.get(locale.resolve(Bukkit.getConsoleSender()), key, a);
        String mode;
        String amountRaw;
        if (args.isNamed()) {
            mode = args.getString("mode", "give");
            amountRaw = args.getString("amount", "0");
        } else {
            String[] tokens = args.tokens();
            if (tokens.length < 2) {
                logger.warning(text.apply("economy-usage", new Object[]{name, args.raw()}));
                return;
            }
            mode = tokens[0];
            amountRaw = tokens[1];
        }
        double amount;
        try {
            amount = Double.parseDouble(amountRaw.trim());
        } catch (NumberFormatException e) {
            logger.warning(text.apply("economy-bad-amount", new Object[]{name, amountRaw}));
            return;
        }
        if (!Double.isFinite(amount) || amount < 0) {
            logger.warning(text.apply("economy-negative-amount", new Object[]{name, amountRaw}));
            return;
        }
        switch (mode.toLowerCase(Locale.ROOT)) {
            case "give" -> hook.give(player, amount);
            case "take" -> {
                if (hook.balance(player) >= amount) {
                    hook.take(player, amount);
                }
            }
            case "set" -> hook.set(player, amount);
            default -> logger.warning(text.apply("economy-unknown-mode", new Object[]{name, mode}));
        }
    }

    /** Ленивый поиск: экономика может зарегистрироваться уже после старта LLib. */
    private static final class EconomyHolder {
        private final Logger logger;
        private final LocaleService locale;
        private final String name;
        private final String pluginName;
        private final Supplier<EconomyHook> factory;
        private volatile EconomyHook hook;
        private volatile boolean warned;

        EconomyHolder(Logger logger, LocaleService locale, String name, String pluginName, Supplier<EconomyHook> factory) {
            this.logger = logger;
            this.locale = locale;
            this.name = name;
            this.pluginName = pluginName;
            this.factory = factory;
        }

        EconomyHook get() {
            if (hook != null) return hook;
            if (Bukkit.getPluginManager().isPluginEnabled(pluginName)) {
                hook = factory.get();
            }
            if (hook == null && !warned) {
                warned = true;
                logger.warning(locale.get(locale.resolve(Bukkit.getConsoleSender()), "economy-disabled", name, pluginName));
            }
            return hook;
        }
    }
}
