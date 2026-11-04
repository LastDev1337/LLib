package dev.laststudio.lib.plugin.action.economy;

import org.bukkit.OfflinePlayer;

public interface EconomyHook {

    double balance(OfflinePlayer player);

    void give(OfflinePlayer player, double amount);

    void take(OfflinePlayer player, double amount);

    void set(OfflinePlayer player, double amount);
}
