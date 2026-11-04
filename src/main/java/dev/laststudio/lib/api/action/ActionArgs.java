package dev.laststudio.lib.api.action;

import dev.by1337.yaml.YamlMap;

import java.util.function.UnaryOperator;

public final class ActionArgs {

    private final String raw;
    private final YamlMap named;
    private final UnaryOperator<String> resolver;

    private ActionArgs(String raw, YamlMap named, UnaryOperator<String> resolver) {
        this.raw = raw;
        this.named = named;
        this.resolver = resolver;
    }

    /** Копия аргументов, в которой raw() и getString() прогоняются через resolver. */
    public ActionArgs withResolver(UnaryOperator<String> resolver) {
        return new ActionArgs(raw, named, resolver);
    }

    private String resolve(String value) {
        return resolver == null || value == null ? value : resolver.apply(value);
    }

    public static ActionArgs raw(String raw) {
        return new ActionArgs(raw, null, null);
    }

    public static ActionArgs named(YamlMap named) {
        return new ActionArgs(null, named, null);
    }

    public boolean isNamed() {
        return named != null;
    }

    public String raw() {
        return raw == null ? "" : resolve(raw);
    }

    public String[] tokens() {
        String r = raw().trim();
        return r.isEmpty() ? new String[0] : r.split("\\s+");
    }

    public YamlMap named() {
        return named;
    }

    public String getString(String key, String def) {
        return named == null ? def : resolve(named.get(key).asString(def));
    }

    public float getFloat(String key, float def) {
        return named == null ? def : named.get(key).asFloat(def);
    }

    public int getInt(String key, int def) {
        return named == null ? def : named.get(key).asInt(def);
    }

    public boolean getBoolean(String key, boolean def) {
        return named == null ? def : named.get(key).asBool(def);
    }
}
