package dev.laststudio.lib.api.version;

public interface VersionService {

    String current();

    String minSupported();

    String maxSupported();

    boolean isSupported();
}
