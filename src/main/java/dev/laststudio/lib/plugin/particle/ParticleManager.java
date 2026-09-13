package dev.laststudio.lib.plugin.particle;

import dev.by1337.particle.ParticleRender;
import dev.by1337.particle.ParticleType;
import dev.by1337.particle.particle.ParticleData;
import dev.by1337.particle.particle.ParticleSource;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import dev.laststudio.lib.api.particle.ParticleService;

import java.util.Collection;

public final class ParticleManager implements ParticleService {

    @Override
    public void spawn(Player player, ParticleType type, Location location) {
        spawn(player, ParticleData.of(type), location);
    }

    @Override
    public void spawn(Collection<Player> players, ParticleType type, Location location) {
        spawn(players, ParticleData.of(type), location);
    }

    @Override
    public void spawn(Player player, ParticleSource source, Location location) {
        ParticleRender.render(player, source, location.getX(), location.getY(), location.getZ());
    }

    @Override
    public void spawn(Collection<Player> players, ParticleSource source, Location location) {
        ParticleRender.render(players, source, location.getX(), location.getY(), location.getZ());
    }
}
