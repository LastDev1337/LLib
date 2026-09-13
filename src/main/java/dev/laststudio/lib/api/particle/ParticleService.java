package dev.laststudio.lib.api.particle;

import dev.by1337.particle.ParticleType;
import dev.by1337.particle.particle.ParticleSource;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;

public interface ParticleService {

    void spawn(Player player, ParticleType type, Location location);

    void spawn(Collection<Player> players, ParticleType type, Location location);

    void spawn(Player player, ParticleSource source, Location location);

    void spawn(Collection<Player> players, ParticleSource source, Location location);
}
