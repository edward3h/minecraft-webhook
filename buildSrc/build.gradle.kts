plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("com.diffplug.spotless:spotless-plugin-gradle:8.2.1")
    implementation("com.github.jakemarsden:git-hooks-gradle-plugin:0.0.2")
}
