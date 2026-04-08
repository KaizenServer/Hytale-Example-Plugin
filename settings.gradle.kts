pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        // HytaleModdingReleases removed — maven.hytale-modding.info is unreachable.
        // The hytale-mod plugin is no longer needed: HytaleServer.jar is referenced
        // directly as a compileOnly file dependency in build.gradle.kts.
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "ExamplePlugin"
