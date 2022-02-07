/* (C) Edward Harman 2022 */
package org.ethelred.minecraft.webhook;

import io.micronaut.context.annotation.Parameter;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.HttpClient;
import jakarta.inject.Named;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Prototype
@Named("json")
public class JsonSender implements Sender {

  private static final Logger LOGGER = LogManager.getLogger();

  private final BlockingHttpClient client;
  private final URI url;

  public JsonSender(HttpClient client, @Parameter URL url) throws URISyntaxException {
    this.client = client.toBlocking();
    this.url = url.toURI();
  }

  @Override
  public void sendMessage(MinecraftServerEvent event, String message) {
    LOGGER.debug("Send message {}", event);
    var request = HttpRequest.POST(url, event);
    var response = client.exchange(request);
    LOGGER.debug(response);
  }
}
