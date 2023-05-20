/* (C) Edward Harman 2022-2023 */
package org.ethelred.minecraft.webhook;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.beans.BeanIntrospection;
import java.util.stream.Collectors;

@Introspected
public abstract sealed class ServerEvent<X extends ServerEvent>
    permits MinecraftServerEvent, BackupEvent {
  private final EventType type;

  protected ServerEvent(EventType type) {
    this.type = type;
  }

  public EventType getType() {
    return type;
  }

  @Override
  public String toString() {
    var intro = BeanIntrospection.getIntrospection((Class<X>) this.getClass());
    return intro.getBeanProperties().stream()
        .map(p -> "%s=%s".formatted(p.getName(), p.get((X) this)))
        .collect(Collectors.joining(", ", this.getClass().getSimpleName(), ""));
  }
}
