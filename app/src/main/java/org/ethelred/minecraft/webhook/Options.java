/* (C) Edward Harman 2022 */
package org.ethelred.minecraft.webhook;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Context;
import io.micronaut.core.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

/** general options for app */
@Context // ensure options are validated early
@ConfigurationProperties("mc-webhook")
public record Options(@Nullable Set<String> imageNames) {

  private static final Set<String> DEFAULT_IMAGE_NAMES = Set.of("itzg/minecraft-bedrock-server");

  public Options {
    if (imageNames == null || imageNames.isEmpty()) {
      imageNames = new HashSet<>(DEFAULT_IMAGE_NAMES);
    }
  }

  public void setImageName(String imageName) {
    this.imageNames.clear();
    this.imageNames.add(imageName);
  }
}
