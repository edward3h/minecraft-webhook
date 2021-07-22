package org.ethelred.minecraft.webhook;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import dagger.Module;
import dagger.Provides;

@Module
interface DefaultDocker {
    @Provides
    static DockerClient docker() {
        var dockerCC = DefaultDockerClientConfig
            .createDefaultConfigBuilder()
            .build();
        var http = new ApacheDockerHttpClient.Builder()
            .dockerHost(dockerCC.getDockerHost())
            .sslConfig(dockerCC.getSSLConfig())
            .build();
        return DockerClientImpl.getInstance(dockerCC, http);
    }
}
