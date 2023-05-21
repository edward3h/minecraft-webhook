/* (C) Edward Harman 2022-2023 */
package org.ethelred.minecraft.webhook;

import io.micronaut.scheduling.TaskExecutors;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Singleton
public class Reaper {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Executor executor;

    @Inject
    public Reaper(@Named(TaskExecutors.SCHEDULED) Executor executor) {
        this.executor = executor;
    }

    public void check(Logger logger, Future<?> future) {
        executor.execute(() -> {
            try {
                var r = future.get();
                LOGGER.debug("Reaped {}", r);
            } catch (ExecutionException | InterruptedException e) {
                logger.error("Task failed", e);
            }
        });
    }
}
