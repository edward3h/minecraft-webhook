/* (C) Edward Harman 2022 */
package org.ethelred.minecraft.webhook;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Inject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Lists docker containers to check for new/removed ones. Creates Tailers */
@Context
public class Monitor {

  private static final Logger LOGGER = LogManager.getLogger(Monitor.class);

  private final DockerClient docker;
  private final ApplicationContext applicationContext;
  private final Options options;

  @Inject
  public Monitor(ApplicationContext applicationContext, DockerClient docker, Options options) {
    this.applicationContext = applicationContext;
    this.docker = docker;
    this.options = options;
    LOGGER.debug("Constructed with {}", options);
  }

  private final Map<String, Object> tails = new ConcurrentHashMap<>();

  @Scheduled(fixedRate = "${mc-webhook.options.monitor.rate:5s}")
  public void checkForContainers() {
    docker
        .listContainersCmd()
        .withAncestorFilter(options.imageNames())
        .exec()
        .forEach(c -> _checkContainer(c, Tailer.class));
    docker
        .listContainersCmd()
        .withAncestorFilter(options.backupImageNames())
        .exec()
        .forEach(c -> _checkContainer(c, BackupTailer.class));
    if (CollectionUtils.isNotEmpty(options.imageLabels())) {
      docker
          .listContainersCmd()
          .withLabelFilter(options.imageLabels())
          .exec()
          .forEach(c -> _checkContainer(c, Tailer.class));
    }
    if (CollectionUtils.isNotEmpty(options.backupImageLabels())) {
      docker
          .listContainersCmd()
          .withLabelFilter(options.backupImageLabels())
          .exec()
          .forEach(c -> _checkContainer(c, BackupTailer.class));
    }
  }

  private void _checkContainer(Container container, Class<?> tailerClass) {
    var containerId = container.getId();
    if (!tails.containsKey(containerId)) {
      LOGGER.debug("Adding container {}", (Object) container.getNames());
      tails.putIfAbsent(
          containerId,
          applicationContext.createBean(
              tailerClass, containerId, container.getNames(), onComplete(containerId)));
    }
  }

  private Runnable onComplete(String containerId) {
    return () -> tails.remove(containerId);
  }
}
