# Minecraft Webhook

This service runs against a docker host. 
It looks for docker containers running Minecraft Bedrock Dedicated Server and tails their logs.
It matches a variety of different events and sends messages for them to Discord, a generic JSON webhook, or MQTT topic.

## Example Configuration

Using docker-compose to run the Bedrock server and related services.

_docker-compose.yml_

```yaml
version: "3.9"
services:
  minecraft1:
    image: itzg/minecraft-bedrock-server
    container_name: minecraft1
    restart: "unless-stopped"
    environment:
      EULA: "TRUE"
      SERVER_NAME: "My Server"
      GAMEMODE: "survival"
      DIFFICULTY: "hard"
      LEVEL_NAME: "My Level"
      UID: 1000
      GID: 998
      VERSION: LATEST
    ports:
      - 19132:19132/udp
    volumes:
      - "${MC_HOME}/server:/data"
    stdin_open: true
    tty: true

  webhook:
    image: ghcr.io/edward3h/mc-webhook:0.4
    restart: "unless-stopped"
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
      - ./config.yml:/config.yml
```

### Configuration File
Since 0.3, mc-webhook supports multiple webhooks defined in a config file. It should be mounted into docker as `config.yml`.

_config.yml_
```yaml
mc-webhook:
  image-names: itzg/minecraft-bedrock-server
  webhooks:
    discord1:
      type: discord
      url: https://discordapp.com/api/webhooks/[ids from your discord server]
      # if events: are not specified, defaults to all events, with default messages
    json1:
      type: json
      url: http://your.server/webhook
      # POSTs a JSON object to the given URL. See below.
    discord_custom:
      type: discord
      url: https://discordapp.com/api/webhooks/[ids from your discord server]
      events:
        PLAYER_CONNECTED: Hello %playerName% with id %playerXuid%
        SERVER_STARTED: The world %worldName% is starting on %containerName%
        # only these events will send a message
        # values in %% will be substituted
    mqtt1:
      type: mqtt
      topic: mc-webhook
      # Send JSON of all event types to this MQTT topic
      
# configuration for connecting to an MQTT broker running on host 'messages'      
mqtt:
  client:
    client-id: mc-webhook
    server-uri: tcp://messages:1883
    clean-start: true
```
### Container detection
Setting the `image-names` property tells the service which containers to watch. (Default 'itzg/minecraft-bedrock-server').
Alternatively, add a label to your BDS container and set `image-labels` to the key of the label. The value is ignored.

To follow a `minecraft-bedrock-backup` container, set `backup-image-names` to `kaiede/minecraft-bedrock-backup`. 
Alternatively, add a label to your backup container and set `backup-image-labels` to the key of the label.

### Events
These are the current event types:
* PLAYER_CONNECTED
* PLAYER_DISCONNECTED
* SERVER_STARTED
* SERVER_STOPPED
* BACKUP_COMPLETE

### JSON message
Using the `json` type will send a message like:
```json
{
  "type": "PLAYER_CONNECTED",
  "containerId": "dd5f449daad5dbd0a3f659bbfabcde47605e1a69211e1f7a9d47b758cc54",
  "containerName": "/minecraft1",
  "worldName": "My Level",
  "playerName": "Steve",
  "playerXuid": "12345"
}
```

### MQTT messages
Since 0.4, you can send JSON messages to an MQTT topic. 
See https://github.com/edward3h/docker-bds-integration-test for an example of how this is set up.

### Custom Message format
You can customize the message sent for each event by entering it after the event type (see above example). 
Any of the following will be substituted into the text:
* %containerId%
* %containerName%
* %worldName%
* %playerName%
* %playerXuid%
* %filename%

Which properties are available depends on the type of event.

### Legacy configuration
The previous environment variables are still supported, so you don't have to reconfigure from a previous version.

_.env_

```ini
MC_WEBHOOK_WEBHOOK_URL=https://discordapp.com/api/webhooks/[ids from your discord server]
```
* The server defaults to looking for containers running 'itzg/minecraft-bedrock-server'. It should work for other images as long as the bedrock-server console output appears in the docker logs. To use a different image, specify it in `MC_WEBHOOK_IMAGE_NAME`.

## Links

* https://github.com/itzg/docker-minecraft-bedrock-server
* https://hub.docker.com/r/kaiede/minecraft-bedrock-backup
