package dev.laststudio.lib.plugin.locale;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import dev.laststudio.lib.api.locale.LocaleService;
import dev.laststudio.lib.api.message.ColorType;
import dev.laststudio.lib.plugin.message.TagEscaper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class LocaleManager implements LocaleService {

    private final Map<String, Map<String, String>> messages = new HashMap<>();
    private final ColorType colorType;
    private String useLocale = "auto";
    private String defaultLocale = "ru_RU";

    public LocaleManager(ColorType colorType) {
        this.colorType = colorType;
    }

    public void load(File file) throws IOException {
        Map<String, Map<String, String>> loaded = new HashMap<>();
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            useLocale = root.has("use-locale") ? root.get("use-locale").getAsString() : "auto";
            defaultLocale = root.has("default-locale") ? root.get("default-locale").getAsString() : "ru_RU";
            JsonObject msgs = root.getAsJsonObject("messages");
            if (msgs == null) {
                throw new IOException("translation.json has no top-level \"messages\" object");
            }
            for (Map.Entry<String, JsonElement> entry : msgs.entrySet()) {
                Map<String, String> perLocale = new HashMap<>();
                for (Map.Entry<String, JsonElement> localeEntry : entry.getValue().getAsJsonObject().entrySet()) {
                    perLocale.put(localeEntry.getKey().toLowerCase(Locale.ROOT), localeEntry.getValue().getAsString());
                }
                loaded.put(entry.getKey(), perLocale);
            }
        }
        messages.clear();
        messages.putAll(loaded);
    }

    @Override
    public String resolve(CommandSender sender) {
        if (!"auto".equalsIgnoreCase(useLocale)) {
            return useLocale;
        }
        if (sender instanceof Player player) {
            return player.locale().toString();
        }
        return defaultLocale;
    }

    @Override
    public String get(String locale, String key, Object... args) {
        Map<String, String> perLocale = messages.get(key);
        if (perLocale == null) {
            return key;
        }
        String template = perLocale.get(locale.toLowerCase(Locale.ROOT));
        if (template == null) {
            template = perLocale.get(defaultLocale.toLowerCase(Locale.ROOT));
        }
        if (template == null) {
            return key;
        }
        return format(template, args);
    }

    private String format(String template, Object... args) {
        StringBuilder sb = new StringBuilder(template.length());
        int argIdx = 0;
        for (int i = 0; i < template.length(); i++) {
            char c = template.charAt(i);
            if (c == '{' && i + 1 < template.length() && template.charAt(i + 1) == '}') {
                sb.append(argIdx < args.length ? sanitize(String.valueOf(args[argIdx++])) : "{}");
                i++;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private String sanitize(String value) {
        return TagEscaper.escape(colorType, value);
    }
}
