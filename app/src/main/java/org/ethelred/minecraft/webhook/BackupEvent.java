/* (C) Edward Harman 2022 */
package org.ethelred.minecraft.webhook;

import io.micronaut.core.annotation.Introspected;

@Introspected
public final class BackupEvent extends ServerEvent<BackupEvent> {
  private final String filename;

  public BackupEvent(EventType type, String filename) {
    super(type);
    this.filename = filename;
  }

  public String getFilename() {
    return filename;
  }
}
