package dev.laststudio.lib.plugin.libs;

import org.bukkit.plugin.Plugin;
import sun.misc.Unsafe;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.logging.Level;

public final class LibraryBootstrap {

    private static final String REPO = "https://repo.by1337.space/repository/maven-releases/";

    private static final Library[] LIBRARIES = {
            new Library("dev.by1337.cmd.Command", "dev.by1337.cmd", "BCmd", "1.4"),
            // транзитивные зависимости перечислены явно: runtime-загрузчик Maven-зависимости сам не подтягивает
            new Library("dev.by1337.yaml.YamlMap", "dev.by1337.yaml", "common", "1.6"),
            new Library("dev.by1337.yaml.BukkitCodecs", "dev.by1337.yaml", "byaml-bukkit", "1.6"),
            new Library("dev.by1337.particle.particle.ParticleSource", "dev.by1337.particle", "render", "1.8"),
            new Library("dev.by1337.particle.ParticleRenderBootstrapper", "dev.by1337.particle", "render-bukkit", "1.8"),
            new Library("dev.by1337.plc.PlaceholderProcessor", "dev.by1337.plc", "BPlaceholder", "1.3"),
    };

    private static final MethodHandle ADD_URL;

    private LibraryBootstrap() {
    }

    public static void bootstrap(Plugin plugin) {
        Path folder = plugin.getDataFolder().toPath().resolve(".libraries");
        for (Library library : LIBRARIES) {
            if (hasClass(library.probeClass())) {
                continue;
            }
            try {
                Path jar = download(library, folder);
                addToClasspath(plugin, jar);
                plugin.getLogger().info("Loaded runtime library " + library.artifactId() + " " + library.version());
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load runtime library " + library.artifactId(), e);
            }
        }
    }

    private static boolean hasClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static Path download(Library library, Path folder) throws IOException, InterruptedException {
        Path resultFolder = folder.resolve(library.groupId().replace('.', '/'))
                .resolve(library.artifactId()).resolve(library.version());
        Files.createDirectories(resultFolder);
        Path out = resultFolder.resolve(library.artifactId() + "-" + library.version() + ".jar");
        if (Files.exists(out)) {
            return out;
        }
        String path = library.groupId().replace('.', '/') + '/' + library.artifactId() + '/' + library.version() + '/'
                + library.artifactId() + '-' + library.version() + ".jar";
        String url = REPO + path;

        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(30)).build();
        Path tmp = out.resolveSibling(out.getFileName() + ".part");
        HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(tmp));
        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode() + " for " + url);
        }
        Files.move(tmp, out, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        return out;
    }

    private static void addToClasspath(Plugin plugin, Path jar) throws IOException {
        ClassLoader loader = plugin.getClass().getClassLoader();
        if (!(loader instanceof URLClassLoader)) {
            throw new IllegalStateException("Plugin classloader is not a URLClassLoader: " + loader.getClass());
        }
        try {
            ADD_URL.invoke(loader, jar.toUri().toURL());
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to add " + jar + " to classpath", e);
        }
    }

    static {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            Unsafe unsafe = (Unsafe) theUnsafe.get(null);

            Field implLookup = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            MethodHandles.Lookup lookup = (MethodHandles.Lookup)
                    unsafe.getObject(unsafe.staticFieldBase(implLookup), unsafe.staticFieldOffset(implLookup));

            ADD_URL = lookup.findVirtual(URLClassLoader.class, "addURL", MethodType.methodType(void.class, URL.class));
        } catch (Throwable e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private record Library(
            String probeClass,
            String groupId,
            String artifactId,
            String version
    ) {}
}
