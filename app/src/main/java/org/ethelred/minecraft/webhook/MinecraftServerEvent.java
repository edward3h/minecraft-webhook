/* (C) Edward Harman 2022 */
package org.ethelred.minecraft.webhook;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;

@Introspected
public record MinecraftServerEvent(
    @NonNull Type type,
    @NonNull String containerId,
    @NonNull String containerName,
    @NonNull String worldName,
    @Nullable String playerName,
    @Nullable String playerXuid) {

  public MinecraftServerEvent(
      @NonNull Type type,
      @NonNull String containerId,
      @NonNull String containerName,
      @NonNull String worldName) {
    this(type, containerId, containerName, worldName, null, null);
  }

  enum Type {
    PLAYER_CONNECTED,
    PLAYER_DISCONNECTED,
    SERVER_STARTED,
    SERVER_STOPPED,
  }
}
