/* (C) Edward Harman 2022-2023 */
package org.ethelred.minecraft.webhook;

public final class MinecraftPlayerEvent extends MinecraftServerEvent<MinecraftPlayerEvent> {
    private final String playerName;
    private final String playerXuid;

    public MinecraftPlayerEvent(
            EventType type,
            String containerId,
            String containerName,
            String worldName,
            String playerName,
            String playerXuid) {
        super(type, containerId, containerName, worldName);
        this.playerName = playerName;
        this.playerXuid = playerXuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getPlayerXuid() {
        return playerXuid;
    }
}
