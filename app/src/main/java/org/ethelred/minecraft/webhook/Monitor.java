package org.ethelred.minecraft.webhook;

import com.github.dockerjava.api.DockerClient;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.ethelred.minecraft.webhook.ContainerComponent.Factory;

/**
 * Lists docker containers to check for new/removed ones. Creates Tailers
 */
@Singleton
public class Monitor implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(
        Monitor.class.getName()
    );

    private final DockerClient docker;
    private final Collection<String> imageNames;
    private final Factory containerComponentFactory;

    @Inject
    public Monitor(
        DockerClient docker,
        Collection<String> imageNames,
        ContainerComponent.Factory containerComponentFactory
    ) {
        this.docker = docker;
        this.imageNames = imageNames;
        this.containerComponentFactory = containerComponentFactory;
    }

    private Map<String, Tailer> tails = new ConcurrentHashMap<>();

    public void run() {
        LOGGER.info("Checking for containers");
        docker
            .listContainersCmd()
            .withAncestorFilter(imageNames)
            .exec()
            .forEach(c -> _checkContainer(c.getId()));
    }

    private void _checkContainer(String containerId) {
        if (!tails.containsKey(containerId)) {
            LOGGER.info(() -> "Adding container " + containerId);
            tails.putIfAbsent(
                containerId,
                containerComponentFactory.create(containerId).tailer()
            );
        }
    }

    void onComplete(String containerId) {
        tails.remove(containerId);
    }
}
