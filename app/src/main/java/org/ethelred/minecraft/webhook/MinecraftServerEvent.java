/* (C) Edward Harman 2022-2026 */
package org.ethelred.minecraft.webhook;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;

@Introspected
public sealed class MinecraftServerEvent<X> extends ServerEvent<MinecraftServerEvent<X>>
    permits MinecraftPlayerEvent {

  private final String containerId;
  private final String containerName;
  private final String worldName;

  public MinecraftServerEvent(
      @NonNull EventType type,
      @NonNull String containerId,
      @NonNull String containerName,
      @NonNull String worldName) {
    super(type);
    this.containerId = containerId;
    this.containerName = containerName;
    this.worldName = worldName;
  }

  public String getContainerId() {
    return containerId;
  }

  public String getContainerName() {
    return containerName;
  }

  public String getWorldName() {
    return worldName;
  }
}
