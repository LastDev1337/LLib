package dev.laststudio.lib.items.component;

import dev.by1337.yaml.codec.YamlCodec;

public record BaseComponent<T>(int id, String name, YamlCodec<T> codec) {
}
