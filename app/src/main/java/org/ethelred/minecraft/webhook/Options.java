package org.ethelred.minecraft.webhook;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Context;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * options for app
 */
@Context // ensure options are validated early
@ConfigurationProperties("mc-webhook")
public class Options {

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

    @NotNull(
        message = "A webhook URL must be provided, for example by specifying the environment variable MC_WEBHOOK_WEBHOOK_URL."
    )
    public URL getWebhookUrl() {
        return webhook;
    }

    public void setWebhookUrl(URL webhook) {
        this.webhook = webhook;
    }

    private Set<String> imageNames = new HashSet<>();

    private URL webhook;
}
