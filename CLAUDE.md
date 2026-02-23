# minecraft-webhook

## Commands

- **Build:** `./gradlew build`
- **Test all:** `./gradlew test`
- **Single test:** `./gradlew :app:test --tests "org.ethelred.minecraft.webhook.MonitorSpec"`
- **Format code:** `./gradlew spotlessApply`
- **Check format:** `./gradlew spotlessCheck`

## Architecture

A **Micronaut 4** application (not Spring Boot) that monitors Docker containers running Minecraft Bedrock Edition and backup services. It tails container logs, parses game events via regex, and forwards them to configurable webhooks.

**Build system:** Gradle 8.14.4 multi-module (root + `app/` subproject). Convention plugin in `buildSrc/` applies Spotless with Google Java Format — run `spotlessApply` before committing.

**Target:** Java 17 source compatibility, JDK 25, GraalVM native image.

## Key Components

| Class | Role |
|---|---|
| `Monitor` | Scheduled bean; polls Docker every 5s for target containers |
| `Tailer` / `BackupTailer` | Stream container logs, regex-match game/backup events |
| `MinecraftServerEventListener` | `@EachBean` per sender config; dispatches parsed events |
| `DiscordWebhookSender` | Sends formatted messages to Discord webhooks |
| `JsonSender` | Posts JSON payloads to an HTTP endpoint |
| `MqttSender` | Publishes events to an MQTT broker |
| `SenderConfiguration` | `@EachProperty` — per-webhook config with message templates |
| `Options` | `@ConfigurationProperties` — Docker image names/labels to watch |

## Configuration

Webhooks are configured via `@EachProperty` (`SenderConfiguration`), so each entry under the config key creates a separate sender instance. See `src/main/resources/application.yml` for the structure.

## Tests

Spock 2 (Groovy) with TestContainers for integration tests:

- `MonitorSpec` — spins up MockServer + fake Bedrock/Backup containers
- `MqttSenderSpec` — uses a real Mosquitto container
