/* (C) Edward Harman 2022-2026 */
package org.ethelred.minecraft.webhook;

import io.micronaut.mqtt.annotation.Topic;
import io.micronaut.mqtt.annotation.v5.MqttPublisher;

@MqttPublisher
public interface MqttClient {
  void sendEvent(@Topic String topic, ServerEvent event);
}
