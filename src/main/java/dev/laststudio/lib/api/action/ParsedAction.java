package dev.laststudio.lib.api.action;

public record ParsedAction(
        String name,
        ActionArgs args,
        double chance) {

    public ParsedAction(String name, ActionArgs args) {
        this(name, args, 100.0);
    }
}
