package dev.laststudio.lib.plugin.action.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

final class VaultHook implements EconomyHook {

    private final Economy economy;

    private VaultHook(Economy economy) {
        this.economy = economy;
    }

    static EconomyHook create() {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        return rsp == null ? null : new VaultHook(rsp.getProvider());
    }

    @Override
    public double balance(OfflinePlayer player) {
        return economy.getBalance(player);
    }

    @Override
    public void give(OfflinePlayer player, double amount) {
        economy.depositPlayer(player, amount);
    }

    @Override
    public void take(OfflinePlayer player, double amount) {
        economy.withdrawPlayer(player, amount);
    }

    @Override
    public void set(OfflinePlayer player, double amount) {
        double delta = amount - economy.getBalance(player);
        if (delta > 0) {
            economy.depositPlayer(player, delta);
        } else if (delta < 0) {
            economy.withdrawPlayer(player, -delta);
        }
    }
}
