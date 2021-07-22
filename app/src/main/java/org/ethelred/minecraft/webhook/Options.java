package org.ethelred.minecraft.webhook;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import picocli.CommandLine.Option;

/**
 * Command line options for app
 */
public class Options {

    @Option(
        names = { "--image-name", "-i" },
        defaultValue = "itzg/minecraft-bedrock-server"
    )
    Set<String> imageNames = new HashSet<>();

    @Option(names = { "--webhook-url", "--webhook", "-u" }, required = true)
    URL webhook;
}
