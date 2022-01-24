/* (C) Edward Harman 2022 */
package org.ethelred.minecraft.webhook;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;

@Introspected
public class MinecraftServerEvent {

    public MinecraftServerEvent(
        @NonNull Type type,
        @NonNull String containerId,
        @NonNull String containerName,
        @NonNull String worldName,
        @Nullable String playerName
    ) {
        this.type = type;
        this.containerId = containerId;
        this.containerName = containerName;
        this.worldName = worldName;
        this.playerName = playerName;
    }

    enum Type {
        PLAYER_CONNECTED,
        PLAYER_DISCONNECTED,
        SERVER_STARTED,
        SERVER_STOPPED,
    }

    @NonNull
    public Type getType() {
        return type;
    }

    @NonNull
    public String getContainerId() {
        return containerId;
    }

    @NonNull
    public String getContainerName() {
        return containerName;
    }

    @NonNull
    public String getWorldName() {
        return worldName;
    }

    @Nullable
    public String getPlayerName() {
        return playerName;
    }

    @NonNull
    private final Type type;

    @NonNull
    private final String containerId;

    @NonNull
    private final String containerName;

    @NonNull
    private final String worldName;

    @Nullable
    private final String playerName;
}
