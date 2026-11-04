package dev.laststudio.lib.api.scheduler;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;

public interface SchedulerService {

    void global(Plugin plugin, Runnable task);

    ScheduledTask globalLater(Plugin plugin, Runnable task, long delayTicks);

    ScheduledTask globalTimer(Plugin plugin, Runnable task, long delayTicks, long periodTicks);

    ScheduledTask region(Plugin plugin, Location location, Runnable task);

    ScheduledTask regionLater(Plugin plugin, Location location, Runnable task, long delayTicks);

    ScheduledTask entity(Plugin plugin, Entity entity, Runnable task);

    ScheduledTask async(Plugin plugin, Runnable task);

    ScheduledTask asyncLater(Plugin plugin, Runnable task, long delayTicks);

    ScheduledTask asyncTimer(Plugin plugin, Runnable task, long delayTicks, long periodTicks);

    CompletableFuture<Boolean> teleport(Player player, Location location);

    void cancelAll(Plugin plugin);
}
