# Minecraft Webhook

This service runs against a docker host. 
It looks for docker containers running Minecraft Bedrock Dedicated Server and tails their logs.
It sends player connect and disconnect messages to a Discord Webhook.

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

  backup:
    image: kaiede/minecraft-bedrock-backup
    container_name: backup
    restart: "unless-stopped"
    depends_on:
      - "minecraft1"
    environment:
      DEBUG: "true"
      BACKUP_INTERVAL: "6h"
      TZ: "America/New_York"
      UID: 1000
      GID: 998
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
      - "${MC_HOME}/backups:/backups"
      - "${MC_HOME}/server:/minecraft1"

  webhook:
    image: ghcr.io/edward3h/mc-webhook:latest
    restart: "unless-stopped"
    env_file: .env
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
```

_.env_

```ini
MC_HOME=/path/to/store/minecraft/data
MC_WEBHOOK_WEBHOOK_URL=https://discordapp.com/api/webhooks/[ids from your discord server]
```

### Notes

* MC_WEBHOOK_WEBHOOK_URL environment variable is **required** and should be copied from your Discord Server Settings.
* The server defaults to looking for containers running 'itzg/minecraft-bedrock-server'. It should work for other images as long as the bedrock-server console output appears in the docker logs. To use a different image, specify it in `MC_WEBHOOK_IMAGE_NAME`.

## Links

* https://github.com/itzg/docker-minecraft-bedrock-server
* https://github.com/Kaiede/docker-minecraft-bedrock-backup
