/* (C) Edward Harman 2022 */
package org.ethelred.minecraft.webhook;

import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.convert.ConversionService;
import java.net.URL;
import java.util.Map;

/**
 * this bean maps the original webhook property into the updated system for backwards compatibility
 */
@Context
@Requires(property = "mc-webhook.webhook-url")
public class BackCompatUrlSetup extends MinecraftServerEventListener {

  public BackCompatUrlSetup(
      BeanContext context,
      ConversionService<?> conversionService,
      @Property(name = "mc-webhook.webhook-url") URL url) {
    super(context, conversionService, getConfiguration(url));
  }

  private static SenderConfiguration getConfiguration(URL url) {
    return new SenderConfiguration(null, url, null, Map.of());
  }
}
