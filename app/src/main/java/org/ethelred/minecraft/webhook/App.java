/* (C) Edward Harman 2022 */
package org.ethelred.minecraft.webhook;

import io.micronaut.runtime.Micronaut;
import jakarta.inject.Singleton;

@Singleton
public class App {

    public static void main(String[] args) {
        // tell Micronaut to look for config in known paths of the docker image
        System.setProperty(
            "micronaut.config.files",
            "/config.yml" // in root of docker image
        );
        Micronaut
            .build(args)
            .mainClass(App.class)
            .defaultEnvironments("dev")
            .start();
    }
}
