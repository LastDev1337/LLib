package dev.laststudio.lib.plugin;

import dev.laststudio.lib.api.LLibProvider;
import dev.laststudio.lib.api.action.ActionService;
import dev.laststudio.lib.api.command.CommandService;
import dev.laststudio.lib.api.item.ItemDataService;
import dev.laststudio.lib.api.item.ItemRegistryService;
import dev.laststudio.lib.api.locale.LocaleService;
import dev.laststudio.lib.api.message.MessageService;
import dev.laststudio.lib.api.particle.ParticleService;
import dev.laststudio.lib.api.scheduler.SchedulerService;
import dev.laststudio.lib.api.version.VersionService;

record LLibProviderImpl(LocaleService locale,
                           MessageService messages,
                           VersionService version,
                           CommandService commands,
                           ItemDataService items,
                           ItemRegistryService itemRegistries,
                           ParticleService particles,
                           ActionService actions,
                           SchedulerService scheduler
                          ) implements LLibProvider {
}
