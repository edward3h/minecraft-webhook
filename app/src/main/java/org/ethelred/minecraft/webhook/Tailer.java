package org.ethelred.minecraft.webhook;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Frame;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.inject.Inject;

/**
 * tail container logs
 */
@ContainerScope
public class Tailer {

    private static final Logger LOGGER = Logger.getLogger(
        Tailer.class.getName()
    );

    private static final Pattern levelName = Pattern.compile("Level Name:(.*)");
    /*
[INFO] Player connected: Foxer191, xuid: 2535428717109723
[INFO] Player disconnected: Foxer191, xuid: 2535428717109723
    */
    private static final Pattern playerEvent = Pattern.compile(
        " Player ([^ ]*connected): (.*), xuid"
    );
    private final Runnable completionCallback;
    private final Sender sender;
    private volatile String worldName = "Unknown";

    @Inject
    public Tailer(
        DockerClient docker,
        @ContainerId String containerId,
        Monitor monitor,
        Sender sender
    ) {
        this.completionCallback = () -> monitor.onComplete(containerId);
        this.sender = sender;
        LOGGER.info("Tailer is starting for " + containerId);

        _initial(docker, containerId);
        _follow(docker, containerId);
    }

    private void _initial(DockerClient docker, String containerId) {
        docker
            .logContainerCmd(containerId)
            .withStdOut(true)
            .exec(new InitialCallback());
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

        @Override
        public void onNext(Frame frame) {
            var matcher = levelName.matcher(frame.toString());
            if (matcher.find()) {
                worldName = matcher.group(1).trim();
                LOGGER.fine(() -> "Found world name " + worldName);
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
                sender.sendMessage(
                    String.format(
                        "%s %s %s%n",
                        player,
                        connect ? "connected to" : "disconnected from",
                        worldName
                    )
                );
            }
        }

        @Override
        public void onComplete() {
            completionCallback.run();
        }
    }
}
