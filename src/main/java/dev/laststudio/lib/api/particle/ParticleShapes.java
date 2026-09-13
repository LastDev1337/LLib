package dev.laststudio.lib.api.particle;

import dev.by1337.particle.particle.PacketBuilder;
import dev.by1337.particle.particle.ParticleData;
import dev.by1337.particle.particle.ParticleSource;

import java.util.function.IntFunction;

public final class ParticleShapes {

    private ParticleShapes() {
    }

    public static ParticleSource circle(ParticleData particle, double radius, int points) {
        return points(particle, points, i -> {
            double angle = (2 * Math.PI / points) * i;
            return new double[]{radius * Math.cos(angle), 0, radius * Math.sin(angle)};
        });
    }

    public static ParticleSource ring(ParticleData particle, double innerRadius, double outerRadius, int rings, int pointsPerRing) {
        ParticleSource[] sources = new ParticleSource[Math.max(1, rings)];
        for (int r = 0; r < sources.length; r++) {
            double radius = sources.length == 1 ? outerRadius
                    : innerRadius + (outerRadius - innerRadius) * r / (sources.length - 1);
            sources[r] = circle(particle, radius, pointsPerRing);
        }
        return ParticleSource.of(sources);
    }

    public static ParticleSource sphere(ParticleData particle, double radius, int points) {
        double goldenAngle = Math.PI * (3 - Math.sqrt(5));
        return points(particle, points, i -> {
            double y = 1 - (i / (double) (points - 1)) * 2;
            double r = Math.sqrt(Math.max(0, 1 - y * y));
            double theta = goldenAngle * i;
            return new double[]{Math.cos(theta) * r * radius, y * radius, Math.sin(theta) * r * radius};
        });
    }

    public static ParticleSource line(ParticleData particle, double dx, double dy, double dz, double step) {
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        int points = Math.max(1, (int) Math.round(length / Math.max(step, 0.01)));
        double ux = dx / points;
        double uy = dy / points;
        double uz = dz / points;
        return points(particle, points + 1, i -> new double[]{ux * i, uy * i, uz * i});
    }

    public static ParticleSource helix(ParticleData particle, double radius, double height, double turns, int points) {
        return points(particle, points, i -> {
            double t = i / (double) Math.max(1, points - 1);
            double angle = 2 * Math.PI * turns * t;
            return new double[]{radius * Math.cos(angle), height * t, radius * Math.sin(angle)};
        });
    }

    private static ParticleSource points(ParticleData particle, int count, IntFunction<double[]> offsetAt) {
        return new ParticleSource() {
            @Override
            public void doWrite(PacketBuilder writer, double baseX, double baseY, double baseZ) {
                for (int i = 0; i < count; i++) {
                    double[] offset = offsetAt.apply(i);
                    writer.append(particle, baseX + offset[0], baseY + offset[1], baseZ + offset[2],
                            particle.xDist(), particle.yDist(), particle.zDist());
                }
            }
        };
    }
}
