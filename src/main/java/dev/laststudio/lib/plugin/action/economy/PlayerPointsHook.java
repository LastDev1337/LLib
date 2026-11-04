package dev.laststudio.lib.plugin.action.economy;

import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.OfflinePlayer;

/** Класс грузится только если PlayerPoints установлен. PlayerPoints хранит целые числа. */
final class PlayerPointsHook implements EconomyHook {

    private final PlayerPointsAPI api;

    private PlayerPointsHook(PlayerPointsAPI api) {
        this.api = api;
    }

    static EconomyHook create() {
        PlayerPoints plugin = PlayerPoints.getInstance();
        return plugin == null ? null : new PlayerPointsHook(plugin.getAPI());
    }

    @Override
    public double balance(OfflinePlayer player) {
        return api.look(player.getUniqueId());
    }

    @Override
    public void give(OfflinePlayer player, double amount) {
        api.give(player.getUniqueId(), (int) Math.round(amount));
    }

    @Override
    public void take(OfflinePlayer player, double amount) {
        api.take(player.getUniqueId(), (int) Math.round(amount));
    }

    @Override
    public void set(OfflinePlayer player, double amount) {
        api.set(player.getUniqueId(), (int) Math.round(amount));
    }
}
