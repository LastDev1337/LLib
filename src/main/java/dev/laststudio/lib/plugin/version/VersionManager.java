package dev.laststudio.lib.plugin.version;

import org.bukkit.Bukkit;
import dev.laststudio.lib.api.version.VersionService;

public final class VersionManager implements VersionService {

    public static final String MIN_SUPPORTED = "1.20";
    public static final String MAX_SUPPORTED = "1.26.2";

    private final String current;

    public VersionManager() {
        String raw = Bukkit.getBukkitVersion();
        int dash = raw.indexOf('-');
        this.current = dash >= 0 ? raw.substring(0, dash) : raw;
    }

    @Override
    public String current() {
        return current;
    }

    @Override
    public String minSupported() {
        return MIN_SUPPORTED;
    }

    @Override
    public String maxSupported() {
        return MAX_SUPPORTED;
    }

    @Override
    public boolean isSupported() {
        return compare(current, MIN_SUPPORTED) >= 0 && compare(current, MAX_SUPPORTED) <= 0;
    }

    private static int compare(String a, String b) {
        String[] pa = a.split("\\.");
        String[] pb = b.split("\\.");
        int len = Math.max(pa.length, pb.length);
        for (int i = 0; i < len; i++) {
            int va = i < pa.length ? parse(pa[i]) : 0;
            int vb = i < pb.length ? parse(pb[i]) : 0;
            if (va != vb) {
                return Integer.compare(va, vb);
            }
        }
        return 0;
    }

    private static int parse(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
