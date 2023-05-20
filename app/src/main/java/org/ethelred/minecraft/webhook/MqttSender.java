/* (C) Edward Harman 2022-2023 */
package org.ethelred.minecraft.webhook;

import io.micronaut.context.annotation.Parameter;
import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Prototype
@Named("mqtt")
public class MqttSender implements Sender {
  private static final Logger LOGGER = LogManager.getLogger();

  private final MqttClient client;
  private final String topic;

  public MqttSender(MqttClient client, @Parameter SenderConfiguration configuration) {
    this.client = client;
    this.topic = configuration.topic();
    LOGGER.debug("Constructed {} {}", client, topic);
  }

  @Override
  public void sendMessage(ServerEvent event, String message) {
    LOGGER.debug("sendMessage {} {}", topic, event);
    client.sendEvent(topic, event);
  }
}
