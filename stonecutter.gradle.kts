@file:OptIn(dev.kikugie.stonecutter.StonecutterExperimentalAPI::class)

plugins {
    id("dev.kikugie.stonecutter")
    id("net.fabricmc.fabric-loom-remap") version "1.16-SNAPSHOT" apply false
}

stonecutter active file(".sc_active_version")

stonecutter parameters {
    constants.match(current.project.substringAfterLast('-'), "fabric", "neoforge")
}

tasks.register<org.gradle.api.tasks.Delete>("clean") {
    group = "build"
    description = "Cleans collected artifacts and every selected target when used as an unqualified task."
    delete(layout.buildDirectory)
}

tasks.register("runActiveClient") {
    group = "stonecutter"
    description = "Runs the client for the active Stonecutter target."
    dependsOn(stonecutter.current!!.project + ":runClient")
}

tasks.register("buildActive") {
    group = "stonecutter"
    description = "Builds the active Stonecutter target."
    dependsOn(stonecutter.current!!.project + ":build")
}
