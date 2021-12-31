package org.ethelred.minecraft.webhook;

import com.github.dockerjava.api.DockerClient;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Inject;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Lists docker containers to check for new/removed ones. Creates Tailers
 */
@Singleton
public class Monitor {

    private static final Logger LOGGER = LogManager.getLogger(Monitor.class);

    private final DockerClient docker;
    private final Collection<String> imageNames;
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
        LOGGER.info("Constructed");
    }

    private final Map<String, Tailer> tails = new ConcurrentHashMap<>();

    @Scheduled(fixedRate = "${mc-webhook.options.monitor.rate:1m}")
    public void checkForContainers() {
        LOGGER.info("Checking for containers");
        docker
            .listContainersCmd()
            .withAncestorFilter(imageNames)
            .exec()
            .forEach(c -> _checkContainer(c.getId()));
    }

    private void _checkContainer(String containerId) {
        if (!tails.containsKey(containerId)) {
            LOGGER.info("Adding container {}", containerId);
            tails.putIfAbsent(
                containerId,
                new Tailer(
                    docker,
                    containerId,
                    () -> onComplete(containerId),
                    applicationContext.getBean(Sender.class)
                )
            );
        }
    }

    void onComplete(String containerId) {
        tails.remove(containerId);
    }
}
