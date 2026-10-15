pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("dev.kikugie.stonecutter") version "0.9.8"
    id("dev.kikugie.loom-back-compat") version "0.4.3" apply false
    id("net.neoforged.moddev") version "2.0.148" apply false
    id("me.modmuss50.mod-publish-plugin") version "2.2.1" apply false
}

stonecutter {
    create(rootProject) {
        // One node per Minecraft version and loader. Per-target settings live in versions/<node>/gradle.properties.
        val targets = linkedMapOf(
            "1.21.1" to listOf("fabric", "neoforge"),
            "1.21.4" to listOf("fabric", "neoforge"),
            "1.21.10" to listOf("fabric", "neoforge"),
        )
        targets.forEach { (minecraft, loaders) ->
            loaders.forEach { loader -> version("$minecraft-$loader", minecraft).buildscript = "build.$loader.gradle.kts" }
        }
        vcsVersion = "1.21.1-fabric"
    }
}
