package dev.laststudio.lib.items.util;

import org.bukkit.Bukkit;

/**
 * The library targets 1.21 - 26.3 (26.x = year-based versions that follow 1.21.x).
 * Only the 1.21.3 / 1.21.4 item-component splits need a runtime check;
 * every 26.x server counts as newer than any 1.21.x patch.
 */
public final class ItemVersion {

    private static final int PATCH_ON_1_21 = resolve();

    private ItemVersion() {
    }

    public static boolean is1_21_3orNewer() {
        return PATCH_ON_1_21 >= 3;
    }

    public static boolean is1_21_4orNewer() {
        return PATCH_ON_1_21 >= 4;
    }

    private static int resolve() {
        String[] parts = Bukkit.getBukkitVersion().split("-", 2)[0].split("\\.");
        try {
            int major = Integer.parseInt(parts[0]);
            int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            if (major > 1 || minor > 21) {
                return Integer.MAX_VALUE;
            }
            return parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }
}
