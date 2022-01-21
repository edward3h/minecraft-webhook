package org.ethelred.minecraft.webhook;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Context;
import java.util.HashSet;
import java.util.Set;
import javax.validation.constraints.NotEmpty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * options for app
 */
@Context // ensure options are validated early
@ConfigurationProperties("mc-webhook")
public class Options {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Set<String> DEFAULT_IMAGE_NAMES = Set.of(
        "itzg/minecraft-bedrock-server"
    );

    @NotEmpty
    public Set<String> getImageNames() {
        return imageNames.isEmpty() ? DEFAULT_IMAGE_NAMES : imageNames;
    }

    @SuppressWarnings("unused")
    public void setImageNames(Set<String> imageNames) {
        this.imageNames = imageNames;
    }

    public void setImageName(String imageName) {
        this.imageNames.add(imageName);
    }

    private Set<String> imageNames = new HashSet<>();
}
