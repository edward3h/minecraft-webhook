/* (C) Edward Harman 2022 */
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
public class BackupTailer {

  private static final Logger LOGGER = LogManager.getLogger();

  private static final Pattern backupEvent = Pattern.compile("Backed up as: (\\S+)");
  private final Runnable completionCallback;
  private final ApplicationEventPublisher<BackupEvent> eventPublisher;
  private final Reaper reaper;

  @Inject
  public BackupTailer(
      ApplicationEventPublisher<BackupEvent> eventPublisher,
      DockerClient docker,
      Reaper reaper,
      @Parameter String containerId,
      @Parameter String[] containerNames,
      @Parameter Runnable completionCallback) {
    this.eventPublisher = eventPublisher;
    this.completionCallback = completionCallback;
    this.reaper = reaper;
    String containerName = String.join(",", containerNames);
    LOGGER.info("Starting for {}", containerName);

    _follow(docker, containerId);
  }

  private void _follow(DockerClient docker, String containerId) {
    docker
        .logContainerCmd(containerId)
        .withStdOut(true)
        .withTail(0)
        .withFollowStream(true)
        .exec(new FollowCallback());
  }

  private class FollowCallback extends ResultCallback.Adapter<Frame> {

    @Override
    public void onNext(Frame frame) {
      var matcher = backupEvent.matcher(frame.toString());
      if (matcher.find()) {
        var filename = matcher.group(1).trim();
        reaper.check(
            LOGGER,
            eventPublisher.publishEventAsync(new BackupEvent(EventType.BACKUP_COMPLETE, filename)));
      }
    }

    @Override
    public void onComplete() {
      completionCallback.run();
    }
  }
}
