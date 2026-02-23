plugins {
    id("ethelred.java-conventions")
    id("groovy")
    id("com.gradleup.shadow") version "9.3.1"
    id("io.micronaut.application") version "4.6.2"
}

version = "0.4.2"

repositories {
    mavenCentral()
}

micronaut {
    runtime("netty")
    testRuntime("spock2")
    processing {
        incremental(true)
        annotations("org.ethelred.minecraft.webhook.*")
    }
}

dependencies {
    implementation("com.github.docker-java:docker-java-core:3.7.0")
    implementation("com.github.docker-java:docker-java-transport-httpclient5:3.7.0")
    annotationProcessor("io.micronaut:micronaut-http-validation")
    annotationProcessor("io.micronaut.validation:micronaut-validation-processor")
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut.mqtt:micronaut-mqttv5")
    implementation("io.micronaut:micronaut-jackson-databind")
    implementation("jakarta.inject:jakarta.inject-api")
    implementation("org.apache.logging.log4j:log4j-api:2.25.3")
    implementation("org.apache.commons:commons-text:1.15.0")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j-impl:2.25.3")
    runtimeOnly("org.apache.logging.log4j:log4j-core:2.25.3")
    runtimeOnly("com.fasterxml.jackson.core:jackson-databind:2.21.1")
    runtimeOnly("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.21.1")
    testImplementation(platform("org.testcontainers:testcontainers-bom:2.0.3"))
    testImplementation("org.testcontainers:testcontainers:2.0.3")
    testImplementation("org.testcontainers:spock:1.17.3")
    testImplementation("org.testcontainers:mockserver:1.17.3")
    testImplementation("org.mock-server:mockserver-client-java:5.12.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:6.0.3")
}

tasks.named<JavaCompile>("compileJava") {
    options.compilerArgs.add("-Aproject=${project.group}/${project.name}")
}

application {
    mainClass = "org.ethelred.minecraft.webhook.App"
    applicationName = "mc-webhook"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.named<io.micronaut.gradle.docker.MicronautDockerfile>("dockerfile") {
    baseImage("ghcr.io/graalvm/graalvm-community:25")
    instruction("RUN touch /config.yml")
}

tasks.named<com.bmuschko.gradle.docker.tasks.image.DockerBuildImage>("dockerBuild") {
    if (project.hasProperty("github_ref")) {
        images.set(
            listOf(
                "ghcr.io/edward3h/mc-webhook:${project.version}-SNAPSHOT-${project.property("github_ref")}",
            ),
        )
    } else {
        images.set(
            listOf(
                "ghcr.io/edward3h/mc-webhook:latest",
                "ghcr.io/edward3h/mc-webhook:${project.version}",
            ),
        )
    }
}
