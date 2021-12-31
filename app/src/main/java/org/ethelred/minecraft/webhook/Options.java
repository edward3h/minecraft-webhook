package org.ethelred.minecraft.webhook;

import io.micronaut.context.annotation.ConfigurationProperties;
import java.net.URL;
import java.util.Set;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * options for app
 */
@ConfigurationProperties("mc-webhook.options")
public class Options {

    public Set<String> getImageNames() {
        return imageNames;
    }

    @SuppressWarnings("unused")
    public void setImageNames(Set<String> imageNames) {
        this.imageNames = imageNames;
    }

    public URL getWebhook() {
        return webhook;
    }

    public void setWebhook(URL webhook) {
        this.webhook = webhook;
    }

    @NotEmpty
    private Set<String> imageNames = Set.of("itzg/minecraft-bedrock-server");

    @NotNull
    private URL webhook;
}
