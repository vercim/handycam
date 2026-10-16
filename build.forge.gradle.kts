import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.AbstractArchiveTask

plugins {
    java
    id("net.neoforged.moddev.legacyforge")
    id("me.modmuss50.mod-publish-plugin")
}

val minecraftVersion = stonecutter.current.version
val modVersion = project.property("mod_version") as String
val publicModVersion = modVersion.substringBefore('-')
val publishVersion = "$publicModVersion+$minecraftVersion"
val javaVersion = (project.property("java_version") as String).toInt()
val releaseType = providers.gradleProperty("release_type")
    .orElse(if ("-alpha" in modVersion) "alpha" else if ("-beta" in modVersion) "beta" else "release")
    .get()

group = project.property("mod_group") as String
version = "$modVersion+$minecraftVersion-forge"
base.archivesName = project.property("mod_id") as String

java {
    toolchain.languageVersion = JavaLanguageVersion.of(javaVersion)
    withSourcesJar()
}

// Resolved outside the block: inside it, `minecraftVersion` would be the extension's property, not ours.
val forgeCoordinate = "$minecraftVersion-${project.property("forge_version")}"

legacyForge {
    enable {
        forgeVersion = forgeCoordinate
    }

    runs {
        register("client") {
            client()
            gameDirectory = file("run")
            programArgument("--username=Dev")
            ideName = "Handycam Forge Client"
        }
    }

    mods {
        register(project.property("mod_id") as String) {
            sourceSet(sourceSets["main"])
        }
    }
}

mixin {
    add(sourceSets["main"], "handycam.refmap.json")
    config("handycam.mixins.json")
}

repositories {
    mavenCentral()
    maven("https://maven.shedaniel.me/") { name = "Shedaniel" }
}

dependencies {
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
    modImplementation("me.shedaniel.cloth:cloth-config-forge:${project.property("cloth_config_version")}")
}

tasks.jar {
    manifest.attributes("MixinConfigs" to "handycam.mixins.json")
}

tasks.processResources {
    val metadata = mapOf(
        "version" to project.version,
        "minecraft_version" to minecraftVersion,
        "forge_min_version" to project.property("forge_min_version"),
        "minecraft_version_range" to project.property("minecraft_version_range"),
        "cloth_config_version_range" to project.property("cloth_config_version_range"),
        "mixin_compatibility_level" to project.property("mixin_compatibility_level")
    )
    inputs.properties(metadata)
    filesMatching(listOf("META-INF/mods.toml", "handycam.mixins.json")) { expand(metadata) }
    // Forge 1.20.1 runs on SRG names, so the config must name the refmap the annotation processor writes; the
    // other loaders have no refmap and would warn about a missing one.
    filesMatching("handycam.mixins.json") {
        filter { line -> if ("\"package\"" in line) line + "\n  \"refmap\": \"handycam.refmap.json\"," else line }
    }
    exclude("fabric.mod.json", "META-INF/neoforge.mods.toml")
}

tasks.withType<JavaCompile>().configureEach {
    options.release = javaVersion
}

tasks.register<Copy>("buildAndCollect") {
    group = "build"
    description = "Builds Forge and copies its release JAR to the root build directory."
    val releaseJar = tasks.named<AbstractArchiveTask>("reobfJar")
    dependsOn("build")
    from(releaseJar.flatMap { it.archiveFile })
    into(rootProject.layout.buildDirectory.dir("libs"))
}

publishMods {
    val releaseJar = tasks.named<AbstractArchiveTask>("reobfJar")
    file.set(releaseJar.flatMap { it.archiveFile })
    version.set(publishVersion)
    changelog.set(providers.environmentVariable("RELEASE_CHANGELOG").orElse("See the GitHub release notes."))
    type.set(when (releaseType) {
        "release" -> STABLE
        "beta" -> BETA
        "alpha" -> ALPHA
        else -> error("Unsupported release_type '$releaseType'")
    })
    modLoaders.add("forge")

    curseforge {
        projectId.set(providers.gradleProperty("curseforge_project_id"))
        accessToken.set(providers.environmentVariable("CURSEFORGE_TOKEN"))
        displayName.set(publishVersion)
        minecraftVersions.add(minecraftVersion)
        client.set(true)
        server.set(false)
        requires("cloth-config")
    }

    modrinth {
        projectId.set(providers.gradleProperty("modrinth_project_id"))
        accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
        displayName.set("Handycam $publicModVersion Forge")
        minecraftVersions.add(minecraftVersion)
        requires("cloth-config")
    }
}
