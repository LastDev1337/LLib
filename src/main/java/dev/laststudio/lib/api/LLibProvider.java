package dev.laststudio.lib.api;

import dev.laststudio.lib.api.action.ActionService;
import dev.laststudio.lib.api.command.CommandService;
import dev.laststudio.lib.api.item.ItemDataService;
import dev.laststudio.lib.api.locale.LocaleService;
import dev.laststudio.lib.api.message.MessageService;
import dev.laststudio.lib.api.particle.ParticleService;
import dev.laststudio.lib.api.version.VersionService;

public interface LLibProvider {

    LocaleService locale();

    MessageService messages();

    VersionService version();

    CommandService commands();

    ItemDataService items();

    ParticleService particles();

    ActionService actions();
}
