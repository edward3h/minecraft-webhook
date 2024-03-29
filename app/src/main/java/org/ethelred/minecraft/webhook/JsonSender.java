/* (C) Edward Harman 2022-2023 */
package org.ethelred.minecraft.webhook;

import io.micronaut.context.annotation.Parameter;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.HttpClient;
import jakarta.inject.Named;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Prototype
@Named("json")
public class JsonSender implements Sender {

  private static final Logger LOGGER = LogManager.getLogger();

  private final BlockingHttpClient client;
  private final URI url;

  public JsonSender(HttpClient client, @Parameter SenderConfiguration configuration)
      throws URISyntaxException {
    if (configuration.url() == null) {
      throw new ConfigurationException("json sender requires url to be set");
    }
    this.client = client.toBlocking();
    this.url = configuration.url().toURI();
  }

  @Override
  public void sendMessage(ServerEvent event, String message) {
    LOGGER.debug("Send message {}", event);
    var request = HttpRequest.POST(url, event);
    var response = client.exchange(request);
    LOGGER.debug(response);
  }
}
