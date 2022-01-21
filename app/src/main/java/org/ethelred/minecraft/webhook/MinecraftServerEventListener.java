package org.ethelred.minecraft.webhook;

import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.scheduling.annotation.Async;
import jakarta.inject.Singleton;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;

@Singleton
@EachBean(SenderConfiguration.class)
public class MinecraftServerEventListener
    implements ApplicationEventListener<MinecraftServerEvent> {

    private static final BeanIntrospection<MinecraftServerEvent> eventIntrospection = BeanIntrospection.getIntrospection(
        MinecraftServerEvent.class
    );
    private final SenderConfiguration configuration;
    private final Sender sender;

    public MinecraftServerEventListener(
        BeanContext beanContext,
        SenderConfiguration configuration
    ) {
        this.configuration = configuration;
        this.sender =
            beanContext.createBean(
                Sender.class,
                Qualifiers.byName(configuration.getType()),
                configuration.getUrl()
            );
    }

    @Async
    @Override
    public void onApplicationEvent(MinecraftServerEvent event) {
        if (configuration.getEvents().containsKey(event.getType())) {
            var substitutor = new StringSubstitutor(_eventLookup(event));
            var messageFormat = configuration.getEvents().get(event.getType());
            var message = substitutor.replace(messageFormat);
            sender.sendMessage(event, message);
        }
    }

    private StringLookup _eventLookup(MinecraftServerEvent event) {
        return key -> {
            var property = eventIntrospection.getProperty(key);
            return property
                .map(p -> p.get(event))
                .map(String::valueOf)
                .orElse(null);
        };
    }
}
