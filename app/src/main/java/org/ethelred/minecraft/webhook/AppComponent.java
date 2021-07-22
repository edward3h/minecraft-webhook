package org.ethelred.minecraft.webhook;

import dagger.BindsInstance;
import dagger.Component;
import java.net.URL;
import java.util.Collection;
import javax.inject.Singleton;

@Singleton
@Component(
    modules = {
        DefaultDocker.class,
        WebhookModule.class,
        ContainerComponent.InstallationModule.class,
    }
)
interface AppComponent {
    Monitor monitor();

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder imageNames(Collection<String> imageNames);

        @BindsInstance
        Builder webhook(URL webhook);

        AppComponent build();
    }
}
