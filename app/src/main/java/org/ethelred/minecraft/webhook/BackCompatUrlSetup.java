package org.ethelred.minecraft.webhook;

import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import java.net.URL;

@Context
@Requires(property = "mc-webhook.webhook-url")
public class BackCompatUrlSetup extends MinecraftServerEventListener {

    public BackCompatUrlSetup(
        BeanContext context,
        @Property(name = "mc-webhook.webhook-url") URL url
    ) {
        super(context, getConfiguration(url));
    }

    private static SenderConfiguration getConfiguration(URL url) {
        var config = new SenderConfiguration();
        config.setUrl(url); // defaults are ok for other properties
        return config;
    }
}
