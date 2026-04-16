plugins {
    java
    idea
    `maven-publish`
    // hytale-mod plugin removed — maven.hytale-modding.info is unreachable.
    // HytaleServer.jar is now referenced directly as a compileOnly file dependency
    // (same pattern as the hytale-mod-template-maven project).
}

group = "com.example"
version = "0.31.0"
val javaVersion = 25

// Hytale install location — set hytale.install_dir in ~/.gradle/gradle.properties
// pointing to the AppData/Roaming directory (e.g. C:/Users/You/AppData/Roaming).
// The server JAR is resolved at: <install_dir>/Hytale/install/<channel>/package/game/latest/
val hytaleInstallBase = findProperty("hytale.install_dir")?.toString()
    ?: "${System.getProperty("user.home")}/AppData/Roaming"
val hytaleChannel = findProperty("hytale.channel")?.toString() ?: "release"
val hytaleBase = "$hytaleInstallBase/Hytale/install/$hytaleChannel/package/game/latest"
val hytaleServerJar = "$hytaleBase/Server/HytaleServer.jar"
val hytaleAssetsZip = "$hytaleBase/Assets.zip"

repositories {
    mavenCentral()
    maven {
        name = "hytale"
        url = uri("https://maven.hytale.com/release")
    }
}

dependencies {
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.jspecify)
    // Hytale server API — resolved from official Maven repository.
    compileOnly("com.hypixel.hytale:Server:+")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }

    withSourcesJar()
}

tasks.named<ProcessResources>("processResources") {
    var replaceProperties = mapOf(
        "plugin_group" to findProperty("plugin_group"),
        "plugin_maven_group" to project.group,
        "plugin_name" to project.name,
        "plugin_version" to project.version,
        "server_version" to findProperty("server_version"),

        "plugin_description" to findProperty("plugin_description"),
        "plugin_website" to findProperty("plugin_website"),

        "plugin_main_entrypoint" to findProperty("plugin_main_entrypoint"),
        "plugin_author" to findProperty("plugin_author")
    )

    filesMatching("manifest.json") {
        expand(replaceProperties)
    }

    inputs.properties(replaceProperties)
}

tasks.withType<Jar> {
    manifest {
        attributes["Specification-Title"] = rootProject.name
        attributes["Specification-Version"] = version
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] =
            providers.environmentVariable("COMMIT_SHA_SHORT")
                .map { "${version}-${it}" }
                .getOrElse(version.toString())
    }
}

publishing {
    repositories {
        // This is where you put repositories that you want to publish to.
        // Do NOT put repositories for your dependencies here.
    }

    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

// IDEA no longer automatically downloads sources/javadoc jars for dependencies, so we need to explicitly enable the behavior.
idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

// Run the Hytale development server with this plugin loaded.
// Equivalent to the exec-maven-plugin run-server goal in hytale-mod-template-maven.
tasks.register<Exec>("runServer") {
    group = "hytale"
    description = "Run the Hytale development server with this plugin."
    dependsOn("jar")

    val devServerDir = file("dev-server")
    doFirst { devServerDir.mkdirs() }
    workingDir(devServerDir)

    val javaHome = System.getProperty("java.home")
    val javaBin = "$javaHome/bin/java"
    val modsDir = layout.buildDirectory.get().asFile.absolutePath + "/libs"

    commandLine(
        javaBin, "-jar", hytaleServerJar,
        "--allow-op",
        "--assets", hytaleAssetsZip,
        "--mods=$modsDir"
    )
}

val syncAssets = tasks.register<Copy>("syncAssets") {
    group = "hytale"
    description = "Automatically syncs assets from Build back to Source after server stops."

    // Take from the temporary build folder (Where the game saved changes)
    from(layout.buildDirectory.dir("resources/main"))

    // Copy into your actual project source (Where your code lives)
    into("src/main/resources")

    // IMPORTANT: Protect the manifest template from being overwritten
    exclude("manifest.json")

    // If a file exists, overwrite it with the new version from the game
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    doLast {
        println("Assets successfully synced from Game to Source Code!")
    }
}

afterEvaluate {
    val targetTask = tasks.findByName("runServer")
    if (targetTask != null) {
        targetTask.finalizedBy(syncAssets)
        logger.lifecycle("Task '${targetTask.name}' hooked for auto-sync.")
    }
}
