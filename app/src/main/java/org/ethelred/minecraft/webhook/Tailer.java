/* (C) Edward Harman 2022-2023 */
package org.ethelred.minecraft.webhook;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Frame;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.context.event.ApplicationEventPublisher;
import jakarta.inject.Inject;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** tail container logs */
public class Tailer {

  private static final Logger LOGGER = LogManager.getLogger();

  private static final Pattern levelName = Pattern.compile("Level Name:(.*)");
  /*
  [INFO] Player connected: Foxer191, xuid: 2535428717109723
  [INFO] Player disconnected: Foxer191, xuid: 2535428717109723
      */
  private static final Pattern playerEvent =
      Pattern.compile(" Player ([^ ]*connected): (.*), xuid: (\\d+)");
  private final Runnable completionCallback;
  private final ApplicationEventPublisher<MinecraftServerEvent> eventPublisher;
  private final String containerId;
  private final String containerName;
  private final Reaper reaper;
  private volatile String worldName = "Unknown";

  @Inject
  public Tailer(
      ApplicationEventPublisher<MinecraftServerEvent> eventPublisher,
      DockerClient docker,
      Reaper reaper,
      @Parameter String containerId,
      @Parameter String[] containerNames,
      @Parameter Runnable completionCallback) {
    this.eventPublisher = eventPublisher;
    this.completionCallback = completionCallback;
    this.reaper = reaper;
    this.containerId = containerId;
    this.containerName = String.join(",", containerNames);
    LOGGER.info("Tailer is starting for {}", containerName);

    _initial(docker, containerId);
    _follow(docker, containerId);
  }

  private void _initial(DockerClient docker, String containerId) {
    docker.logContainerCmd(containerId).withStdOut(true).exec(new InitialCallback());
  }

  private void _follow(DockerClient docker, String containerId) {
    docker
        .logContainerCmd(containerId)
        .withStdOut(true)
        .withTail(0)
        .withFollowStream(true)
        .exec(new FollowCallback());
  }

  private class InitialCallback extends ResultCallback.Adapter<Frame> {
    private volatile boolean foundWorld = false;

    @Override
    public void onNext(Frame frame) {
      var matcher = levelName.matcher(frame.toString());
      if (matcher.find()) {
        worldName = matcher.group(1).trim();
        foundWorld = true;
        LOGGER.debug("Found world name {}", worldName);
      }
    }

    @Override
    public void onComplete() {
      if (foundWorld) {
        reaper.check(
            LOGGER,
            eventPublisher.publishEventAsync(
                new MinecraftServerEvent(
                    EventType.SERVER_STARTED, containerId, containerName, worldName)));
      }
    }
  }

  private class FollowCallback extends ResultCallback.Adapter<Frame> {

    @Override
    public void onNext(Frame frame) {
      var matcher = playerEvent.matcher(frame.toString());
      if (matcher.find()) {
        var connect = "connected".equals(matcher.group(1));
        var player = matcher.group(2).trim();
        var xuid = matcher.group(3).trim();
        LOGGER.debug("matched {} {}", player, connect);
        reaper.check(
            LOGGER,
            eventPublisher.publishEventAsync(
                new MinecraftPlayerEvent(
                    connect ? EventType.PLAYER_CONNECTED : EventType.PLAYER_DISCONNECTED,
                    containerId,
                    containerName,
                    worldName,
                    player,
                    xuid)));
      }
    }

    @Override
    public void onComplete() {
      reaper.check(
          LOGGER,
          eventPublisher.publishEventAsync(
              new MinecraftServerEvent(
                  EventType.SERVER_STOPPED, containerId, containerName, worldName)));
      completionCallback.run();
    }
  }
}
