package org.ethelred.minecraft.webhook;

/**
 * I can't believe it's not Consumer
 */
public interface Sender {
    void sendMessage(MinecraftServerEvent event, String message);
}
