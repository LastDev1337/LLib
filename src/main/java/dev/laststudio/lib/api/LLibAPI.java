package dev.laststudio.lib.api;

public final class LLibAPI {

    private static LLibProvider provider;

    private LLibAPI() {
    }

    public static LLibProvider get() {
        if (provider == null) {
            throw new IllegalStateException("LLib is not enabled");
        }
        return provider;
    }

    public static void registerProvider(LLibProvider provider) {
        if (LLibAPI.provider != null) {
            throw new IllegalStateException("LLibProvider is already registered");
        }
        LLibAPI.provider = provider;
    }

    public static void unregisterProvider() {
        LLibAPI.provider = null;
    }
}
