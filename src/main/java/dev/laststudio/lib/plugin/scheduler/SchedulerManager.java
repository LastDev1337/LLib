package dev.laststudio.lib.plugin.scheduler;

import dev.laststudio.lib.api.scheduler.SchedulerService;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class SchedulerManager implements SchedulerService {

    private static final long TICK_MS = 50L;

    private static long ticks(long value) {
        return Math.max(1L, value);
    }

    @Override
    public void global(Plugin plugin, Runnable task) {
        Bukkit.getGlobalRegionScheduler().run(plugin, t -> task.run());
    }

    @Override
    public ScheduledTask globalLater(Plugin plugin, Runnable task, long delayTicks) {
        return Bukkit.getGlobalRegionScheduler().runDelayed(plugin, t -> task.run(), ticks(delayTicks));
    }

    @Override
    public ScheduledTask globalTimer(Plugin plugin, Runnable task, long delayTicks, long periodTicks) {
        return Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, t -> task.run(), ticks(delayTicks), ticks(periodTicks));
    }

    @Override
    public ScheduledTask region(Plugin plugin, Location location, Runnable task) {
        return Bukkit.getRegionScheduler().run(plugin, location, t -> task.run());
    }

    @Override
    public ScheduledTask regionLater(Plugin plugin, Location location, Runnable task, long delayTicks) {
        return Bukkit.getRegionScheduler().runDelayed(plugin, location, t -> task.run(), ticks(delayTicks));
    }

    @Override
    public ScheduledTask entity(Plugin plugin, Entity entity, Runnable task) {
        return entity.getScheduler().run(plugin, t -> task.run(), null);
    }

    @Override
    public ScheduledTask async(Plugin plugin, Runnable task) {
        return Bukkit.getAsyncScheduler().runNow(plugin, t -> task.run());
    }

    @Override
    public ScheduledTask asyncLater(Plugin plugin, Runnable task, long delayTicks) {
        return Bukkit.getAsyncScheduler().runDelayed(plugin, t -> task.run(), ticks(delayTicks) * TICK_MS, TimeUnit.MILLISECONDS);
    }

    @Override
    public ScheduledTask asyncTimer(Plugin plugin, Runnable task, long delayTicks, long periodTicks) {
        return Bukkit.getAsyncScheduler().runAtFixedRate(plugin, t -> task.run(),
                ticks(delayTicks) * TICK_MS, ticks(periodTicks) * TICK_MS, TimeUnit.MILLISECONDS);
    }

    @Override
    public CompletableFuture<Boolean> teleport(Player player, Location location) {
        return player.teleportAsync(location);
    }

    @Override
    public void cancelAll(Plugin plugin) {
        Bukkit.getGlobalRegionScheduler().cancelTasks(plugin);
        Bukkit.getAsyncScheduler().cancelTasks(plugin);
    }
}
