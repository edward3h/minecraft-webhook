/* (C) Edward Harman 2022 */
package org.ethelred.minecraft.webhook;

import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.scheduling.annotation.Async;
import jakarta.inject.Singleton;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;

@Singleton
@EachBean(SenderConfiguration.class)
public class MinecraftServerEventListener implements ApplicationEventListener<ServerEvent> {

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
            Sender.class, Qualifiers.byName(configuration.type()), configuration);
  }

  @Async
  @Override
  public void onApplicationEvent(ServerEvent event) {
    if (configuration.events().containsKey(event.getType())) {
      var substitutor = new StringSubstitutor(_eventLookup(event), "%", "%", '\\');
      var messageFormat = configuration.events().get(event.getType());
      var message = substitutor.replace(messageFormat);
      sender.sendMessage(event, message);
    }
  }

  private StringLookup _eventLookup(ServerEvent event) {
    return key -> {
      var property = BeanIntrospection.getIntrospection(event.getClass()).getProperty(key);
      return property
          .map((BeanProperty p) -> p.get(event))
          .flatMap(o -> conversionService.convert(o, String.class))
          .orElse(null);
    };
  }
}
