/* (C) Edward Harman 2022 */
package org.ethelred.minecraft.webhook;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Lists docker containers to check for new/removed ones. Creates Tailers
 */
@Context
public class Monitor {

    private static final Logger LOGGER = LogManager.getLogger(Monitor.class);

    private final DockerClient docker;
    private final Collection<String> imageNames;
    private final Instant startTime;
    private final ApplicationContext applicationContext;

    @Inject
    public Monitor(
        ApplicationContext applicationContext,
        DockerClient docker,
        Options options
    ) {
        this.applicationContext = applicationContext;
        this.docker = docker;
        this.imageNames = options.getImageNames();
        this.startTime = Instant.now();
        LOGGER.debug("Constructed");
    }

    private final Map<String, Tailer> tails = new ConcurrentHashMap<>();

    @Scheduled(fixedRate = "${mc-webhook.options.monitor.rate:5s}")
    public void checkForContainers() {
        LOGGER.debug("Checking for containers");
        docker
            .listContainersCmd()
            .withAncestorFilter(imageNames)
            .exec()
            .forEach(this::_checkContainer);
    }

    private void _checkContainer(Container container) {
        var containerId = container.getId();
        if (!tails.containsKey(containerId)) {
            LOGGER.debug("Adding container {}", container);
            tails.putIfAbsent(
                containerId,
                applicationContext.createBean(
                    Tailer.class,
                    containerId,
                    container.getNames(),
                    onComplete(containerId)
                )
            );
        }
    }

    private Runnable onComplete(String containerId) {
        return () -> tails.remove(containerId);
    }
}
