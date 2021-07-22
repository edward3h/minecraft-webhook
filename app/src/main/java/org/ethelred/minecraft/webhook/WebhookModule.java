package org.ethelred.minecraft.webhook;

import dagger.Binds;
import dagger.Module;

@Module
public interface WebhookModule {
    @Binds
    Sender webhook(DiscordWebhookSender whatevs);
}
