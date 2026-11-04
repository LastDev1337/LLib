package dev.laststudio.lib.api.action;

/**
 * @param chance шанс срабатывания в процентах (0..100); 100 - всегда
 */
public record ParsedAction(
        String name,
        ActionArgs args,
        double chance) {

    public ParsedAction(String name, ActionArgs args) {
        this(name, args, 100.0);
    }
}
