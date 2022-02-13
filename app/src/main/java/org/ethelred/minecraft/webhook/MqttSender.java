/* (C) Edward Harman 2022 */
package org.ethelred.minecraft.webhook;

import io.micronaut.context.annotation.Parameter;
import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Named;

@Prototype
@Named("mqtt")
public class MqttSender implements Sender {
  private final MqttClient client;
  private final String topic;

  public MqttSender(MqttClient client, @Parameter SenderConfiguration configuration) {
    this.client = client;
    this.topic = configuration.topic();
  }

  @Override
  public void sendMessage(ServerEvent event, String message) {
    client.sendEvent(topic, event);
  }
}
