plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("com.diffplug.spotless:spotless-plugin-gradle:8.7.0")
    implementation("com.github.jakemarsden:git-hooks-gradle-plugin:0.0.2")
}
