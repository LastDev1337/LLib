package dev.laststudio.lib.items.util;

@FunctionalInterface
public interface Placeholders {

    Placeholders NONE = raw -> raw;

    String apply(String raw);
}
