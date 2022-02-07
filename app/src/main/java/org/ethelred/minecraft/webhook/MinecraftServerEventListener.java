/* (C) Edward Harman 2022 */
package org.ethelred.minecraft.webhook;

import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.scheduling.annotation.Async;
import jakarta.inject.Singleton;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;

@Singleton
@EachBean(SenderConfiguration.class)
public class MinecraftServerEventListener
    implements ApplicationEventListener<MinecraftServerEvent> {

  private static final BeanIntrospection<MinecraftServerEvent> eventIntrospection =
      BeanIntrospection.getIntrospection(MinecraftServerEvent.class);

  private final SenderConfiguration configuration;
  private final Sender sender;
  private final ConversionService<?> conversionService;

  public MinecraftServerEventListener(
      BeanContext beanContext,
      ConversionService<?> conversionService,
      SenderConfiguration configuration) {
    this.conversionService = conversionService;
    this.configuration = configuration;
    this.sender =
        beanContext.createBean(
            Sender.class, Qualifiers.byName(configuration.getType()), configuration.getUrl());
  }

  @Async
  @Override
  public void onApplicationEvent(MinecraftServerEvent event) {
    if (configuration.getEvents().containsKey(event.type())) {
      var substitutor = new StringSubstitutor(_eventLookup(event), "%", "%", '\\');
      var messageFormat = configuration.getEvents().get(event.type());
      var message = substitutor.replace(messageFormat);
      sender.sendMessage(event, message);
    }
  }

  private StringLookup _eventLookup(MinecraftServerEvent event) {
    return key -> {
      var property = eventIntrospection.getProperty(key);
      return property
          .map(p -> p.get(event))
          .flatMap(o -> conversionService.convert(o, String.class))
          .orElse(null);
    };
  }
}
