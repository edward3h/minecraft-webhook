/* (C) Edward Harman 2022-2023 */
package org.ethelred.minecraft.webhook;

import io.micronaut.context.annotation.EachProperty;
import io.micronaut.core.annotation.Nullable;
import java.net.URL;
import java.util.Map;

@EachProperty("mc-webhook.webhooks")
public record SenderConfiguration(
        @Nullable String type, @Nullable URL url, @Nullable String topic, Map<EventType, String> events) {

    public static final String DEFAULT_TYPE = "discord";
    public static final Map<EventType, String> DEFAULT_EVENTS = Map.of(
            EventType.SERVER_STARTED,
            "World %worldName% starting on %containerName%",
            EventType.SERVER_STOPPED,
            "World %worldName% stopping on %containerName%",
            EventType.PLAYER_CONNECTED,
            "%playerName% connected to %worldName%",
            EventType.PLAYER_DISCONNECTED,
            "%playerName% disconnected from %worldName%",
            EventType.BACKUP_COMPLETE,
            "New backup %filename%");

    public SenderConfiguration {
        if (type == null || type.isBlank()) {
            type = DEFAULT_TYPE;
        }
        if (events.isEmpty()) {
            events = DEFAULT_EVENTS;
        }
    }
}
