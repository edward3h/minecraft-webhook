package org.ethelred.minecraft.webhook;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.ethelred.util.picocli.defaults.EnvironmentDefaultValueProvider;
import picocli.CommandLine;
import picocli.CommandLine.Mixin;

@CommandLine.Command(
    name = "mc-webhook",
    mixinStandardHelpOptions = true,
    defaultValueProvider = EnvironmentDefaultValueProvider.class
)
public class App implements Runnable {
    static {
        try {
            LogManager
                .getLogManager()
                .readConfiguration(
                    App.class.getResourceAsStream("logging.properties")
                );
        } catch (SecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        new CommandLine(new App()).execute(args);
    }

    @Mixin
    Options options;

    @Override
    public void run() {
        LOGGER.info("mc-webhook is starting");
        var component = DaggerAppComponent
            .builder()
            .imageNames(options.imageNames)
            .webhook(options.webhook)
            .build();

        var executor = Executors.newScheduledThreadPool(3);
        executor.scheduleAtFixedRate(
            component.monitor(),
            0,
            1,
            TimeUnit.MINUTES
        );
    }
}
