package dev.laststudio.lib.plugin.version;

import org.bukkit.Bukkit;
import dev.laststudio.lib.api.version.VersionService;

public final class VersionManager implements VersionService {

    public static final String MIN_SUPPORTED = "1.21";
    public static final String MAX_SUPPORTED = "26.3";

    private final String current;

    public VersionManager() {
        String raw = Bukkit.getBukkitVersion();
        int dash = raw.indexOf('-');
        String base = dash >= 0 ? raw.substring(0, dash) : raw;
        // "26.2.build.7" -> "26.2": берём только ведущие числовые части
        this.current = base.replaceAll("^((?:\\d+\\.)*\\d+).*$", "$1");
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

    /**
     * Сравнение версий Minecraft. Версии 1.x и новые 26.x (год.выпуск) лежат на одной шкале:
     * у "1.21.4" отбрасываем ведущую "1", получаем 21.4, а "26.1" остаётся 26.1.
     * Раньше "26.1" сравнивалось с "1.26.2" по первой цифре (26 > 1) и считалось неподдерживаемым.
     */
    static int compare(String a, String b) {
        String[] pa = normalize(a);
        String[] pb = normalize(b);
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

    private static String[] normalize(String version) {
        String[] parts = version.split("\\.");
        if (parts.length > 1 && "1".equals(parts[0])) {
            return java.util.Arrays.copyOfRange(parts, 1, parts.length);
        }
        return parts;
    }

    private static int parse(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
