/* (C) Edward Harman 2022 */
package org.ethelred.minecraft.webhook;

import io.micronaut.context.annotation.EachProperty;
import java.net.URL;
import java.util.Map;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@EachProperty("mc-webhook.webhooks")
public class SenderConfiguration {

  @NotBlank private String type = "discord";

  @NotNull private URL url;

  private Map<MinecraftServerEvent.Type, String> events =
      Map.of(
          MinecraftServerEvent.Type.SERVER_STARTED,
          "World %worldName% starting on %containerName%",
          MinecraftServerEvent.Type.SERVER_STOPPED,
          "World %worldName% stopping on %containerName%",
          MinecraftServerEvent.Type.PLAYER_CONNECTED,
          "%playerName% connected to %worldName%",
          MinecraftServerEvent.Type.PLAYER_DISCONNECTED,
          "%playerName% disconnected from %worldName%");

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public URL getUrl() {
    return url;
  }

  public void setUrl(URL url) {
    this.url = url;
  }

  public Map<MinecraftServerEvent.Type, String> getEvents() {
    return events;
  }

  public void setEvents(Map<MinecraftServerEvent.Type, String> events) {
    this.events = events;
  }
}
