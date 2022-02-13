/* (C) Edward Harman 2022 */
package org.ethelred.minecraft.webhook;

import io.micronaut.scheduling.TaskExecutors;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import org.apache.logging.log4j.Logger;

@Singleton
public class Reaper {
  private final Executor executor;

  @Inject
  public Reaper(@Named(TaskExecutors.SCHEDULED) Executor executor) {
    this.executor = executor;
  }

  public void check(Logger logger, Future<?> future) {
    executor.execute(
        () -> {
          try {
            future.get();
          } catch (ExecutionException | InterruptedException e) {
            logger.error("Task failed", e);
          }
        });
  }
}
