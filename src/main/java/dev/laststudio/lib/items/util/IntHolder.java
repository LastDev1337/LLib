package dev.laststudio.lib.items.util;

import dev.by1337.yaml.codec.YamlCodec;
import java.util.logging.Logger;

import java.util.OptionalInt;

public class IntHolder {
    public static final YamlCodec<IntHolder> CODEC = YamlCodec.STRING.map(
            IntHolder::new,
            IntHolder::src
    );
    public static final IntHolder ZERO = new IntHolder("0");
    public static final IntHolder ONE = new IntHolder("1");

    private static final Logger log = Logger.getLogger(IntHolder.class.getName());
    private String src;
    private int value;
    private boolean cashed;

    public IntHolder(int value) {
        this.value = value;
        cashed = true;
    }

    public IntHolder(String src) {
        this.src = src;
        if (hasNoPlaceholders(src)) {
            var v = tryGet(src);
            if (v.isPresent()) {
                value = v.getAsInt();
                cashed = true;
            }
        }
    }

    public int getOrDefault(Placeholders placeholder, int def) {
        if (cashed) return value;
        try {
            return Integer.parseInt(placeholder.apply(src));
        } catch (NumberFormatException e) {
            log.warning(e.getMessage());
        }
        return def;
    }

    private OptionalInt tryGet(String s) {
        try {
            return OptionalInt.of(Integer.parseInt(s));
        } catch (NumberFormatException e) {
            log.warning("Failed to parse int " + s);
            return OptionalInt.empty();
        }
    }

    public boolean isFinal() {
        return cashed;
    }

    public String src() {
        if (src == null) return src = Integer.toString(value);
        return src;
    }

    private static boolean hasNoPlaceholders(String input) {
        return !input.contains("{") && !input.contains("%");
    }
}
