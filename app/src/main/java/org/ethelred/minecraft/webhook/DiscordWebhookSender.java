package org.ethelred.minecraft.webhook;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.*;
import java.net.http.HttpClient;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Singleton
public class DiscordWebhookSender implements Sender {

    private static final long DEFAULT_DELAY = 3_000;
    private static final Logger LOGGER = LogManager.getLogger(
        DiscordWebhookSender.class
    );

    private final URI webhook;
    private final HttpClient client;
    private final ScheduledExecutorService scheduler;
    private final BlockingQueue<String> waiting;

    @Inject
    public DiscordWebhookSender(Options options) {
        try {
            this.webhook = options.getWebhookUrl().toURI();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
        this.client = HttpClient.newBuilder().build();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.waiting = new ArrayBlockingQueue<>(64);
        _scheduleNext(0L);
        LOGGER.info(
            "DiscordWebhookSender initalized with URL {}",
            this.webhook
        );
    }

    private void _scheduleNext(long delay) {
        LOGGER.debug("_scheduleNext({})", delay);
        scheduler.schedule(
            () -> {
                try {
                    _sendMessage(waiting.take());
                } catch (InterruptedException e) {
                    _scheduleNext(DEFAULT_DELAY);
                }
            },
            delay,
            TimeUnit.MILLISECONDS
        );
    }

    private void _sendMessage(String message) {
        LOGGER.debug(message);
        long delay = 0;
        try {
            // this works for Discord, not sure if it's compatible with other systems
            var request = HttpRequest
                .newBuilder(webhook)
                .header("Content-Type", "application/json")
                .POST(
                    HttpRequest.BodyPublishers.ofString(
                        String.format("{\"content\":\"%s\"}", message)
                    )
                )
                .build();
            var response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            );
            LOGGER.debug(response::body);
            switch (response.statusCode()) {
                case 401: // Unauthorized
                case 403: // Forbidden
                    // Most likely the webhook URL is wrong and this will never succeed
                    System.out.printf(
                        "Discord response %d %s%nDouble check your webhook URL %s%nSystem will exit",
                        response.statusCode(),
                        response.body(),
                        webhook
                    );
                    System.exit(4);
                    break;
                case 503: // Service Unavailable
                case 429: // Rate Limit https://discord.com/developers/docs/topics/rate-limits
                    // Set a delay
                    var retryValue = response
                        .headers()
                        .firstValue("Retry-After");
                    delay = _delayFromHeaderValue(retryValue);
                case 200:
                case 204: // no content
                    // awesome
                    break;
                default:
                    throw new IOException(
                        String.format(
                            "Unexpected response status %d%n%s",
                            response.statusCode(),
                            response.body()
                        )
                    );
            }
        } catch (IOException | InterruptedException e) {
            // can't tell at this point whether the message was sent or not - just give up and log
            System.err.println("Exception in _sendMessage");
            e.printStackTrace(System.err);
            delay = DEFAULT_DELAY;
        } finally {
            _scheduleNext(delay);
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private long _delayFromHeaderValue(Optional<String> retryValue) {
        if (retryValue.isPresent()) {
            try {
                return Long.parseLong(retryValue.get()) * 1000; // header is in seconds
            } catch (NumberFormatException e) {
                // TODO is date format used in this API?
                LOGGER.debug(retryValue.get(), e);
            }
        }
        return DEFAULT_DELAY;
    }

    @Override
    public void sendMessage(String message) {
        LOGGER.debug("sendMessage({})", message.trim());
        //noinspection ResultOfMethodCallIgnored
        waiting.offer(message.trim());
    }
}
