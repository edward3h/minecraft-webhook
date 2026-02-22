/* (C) Edward Harman 2022-2026 */
package org.ethelred.minecraft.webhook;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Context;
import io.micronaut.core.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

/** general options for app */
@Context // ensure options are validated early
@ConfigurationProperties("mc-webhook")
public record Options(
    @Nullable Set<String> imageNames,
    @Nullable Set<String> imageLabels,
    @Nullable Set<String> backupImageNames,
    @Nullable Set<String> backupImageLabels) {

  private static final Set<String> DEFAULT_IMAGE_NAMES = Set.of("itzg/minecraft-bedrock-server");
  private static final Set<String> DEFAULT_BACKUP_IMAGE_NAMES =
      Set.of("kaiede/minecraft-bedrock-backup");

  public Options {
    if (imageNames == null || imageNames.isEmpty()) {
      imageNames = new HashSet<>(DEFAULT_IMAGE_NAMES);
    }
    if (backupImageNames == null || backupImageNames.isEmpty()) {
      backupImageNames = new HashSet<>(DEFAULT_BACKUP_IMAGE_NAMES);
    }
    if (imageLabels == null) {
      imageLabels = Set.of();
    }
    if (backupImageLabels == null) {
      backupImageLabels = Set.of();
    }
  }

  public void setImageName(String imageName) {
    this.imageNames.clear();
    this.imageNames.add(imageName);
  }
}
