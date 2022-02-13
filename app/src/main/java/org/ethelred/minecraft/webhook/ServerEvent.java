/* (C) Edward Harman 2022 */
package org.ethelred.minecraft.webhook;

public abstract sealed class ServerEvent permits MinecraftServerEvent, BackupEvent {
  private final EventType type;

  protected ServerEvent(EventType type) {
    this.type = type;
  }

  public EventType getType() {
    return type;
  }
}
