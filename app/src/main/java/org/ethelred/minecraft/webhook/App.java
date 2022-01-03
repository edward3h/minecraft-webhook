package org.ethelred.minecraft.webhook;

import io.micronaut.runtime.Micronaut;
import jakarta.inject.Singleton;

@Singleton
public class App {

    public static void main(String[] args) {
        Micronaut
            .build(args)
            .eagerInitSingletons(true)
            .mainClass(App.class)
            .defaultEnvironments("dev")
            .start();
    }
}
