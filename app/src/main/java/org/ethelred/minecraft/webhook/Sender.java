/* (C) Edward Harman 2022-2026 */
package org.ethelred.minecraft.webhook;

/**
 * I can't believe it's not Consumer In addition to the interface, Sender implementations are
 * expected to accept an injection @Parameter of type SenderConfiguration
 */
public interface Sender {
  void sendMessage(ServerEvent event, String message);
}
