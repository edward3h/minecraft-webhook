package org.ethelred.minecraft.webhook;

import dagger.BindsInstance;
import dagger.Module;
import dagger.Subcomponent;

@ContainerScope
@Subcomponent
public interface ContainerComponent {
    Tailer tailer();

    @Subcomponent.Factory
    interface Factory {
        ContainerComponent create(
            @BindsInstance @ContainerId String containerId
        );
    }

    @Module(subcomponents = ContainerComponent.class)
    interface InstallationModule {}
}
