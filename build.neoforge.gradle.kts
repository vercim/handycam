import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.AbstractArchiveTask

plugins {
    java
    id("net.neoforged.moddev")
    id("me.modmuss50.mod-publish-plugin")
}

val minecraftVersion = stonecutter.current.version
val modVersion = project.property("mod_version") as String
val javaVersion = (project.property("java_version") as String).toInt()
val releaseType = providers.gradleProperty("release_type")
    .orElse(if ("-alpha" in modVersion) "alpha" else if ("-beta" in modVersion) "beta" else "release")
    .get()

group = project.property("mod_group") as String
version = "$modVersion+$minecraftVersion-neoforge"
base.archivesName = project.property("mod_id") as String

java {
    toolchain.languageVersion = JavaLanguageVersion.of(javaVersion)
    withSourcesJar()
}

neoForge {
    version = project.property("neoforge_version") as String

    runs {
        register("client") {
            client()
            gameDirectory = file("run")
            programArgument("--username=Dev")
            ideName = "Handycam NeoForge Client"
        }
    }

    mods {
        register(project.property("mod_id") as String) {
            sourceSet(sourceSets["main"])
        }
    }
}

repositories {
    mavenCentral()
    maven("https://maven.shedaniel.me/") { name = "Shedaniel" }
}

dependencies {
    implementation("me.shedaniel.cloth:cloth-config-neoforge:${project.property("cloth_config_version")}")
}

tasks.processResources {
    val metadata = mapOf(
        "version" to project.version,
        "minecraft_version" to minecraftVersion,
        "neoforge_version" to project.property("neoforge_version"),
        "neoforge_loader_version_range" to project.property("neoforge_loader_version_range"),
        "cloth_config_version" to project.property("cloth_config_version")
    )
    inputs.properties(metadata)
    filesMatching("META-INF/neoforge.mods.toml") { expand(metadata) }
    exclude("fabric.mod.json")
}

tasks.withType<JavaCompile>().configureEach {
    options.release = javaVersion
}

tasks.named("createMinecraftArtifacts") {
    dependsOn(tasks.named("stonecutterGenerate"))
}

tasks.register<Copy>("buildAndCollect") {
    group = "build"
    description = "Builds NeoForge and copies its release JAR to the root build directory."
    val releaseJar = tasks.named<AbstractArchiveTask>("jar")
    dependsOn("build")
    from(releaseJar.flatMap { it.archiveFile })
    into(rootProject.layout.buildDirectory.dir("libs"))
}

publishMods {
    val releaseJar = tasks.named<AbstractArchiveTask>("jar")
    file.set(releaseJar.flatMap { it.archiveFile })
    displayName.set("Handycam $modVersion NeoForge $minecraftVersion")
    changelog.set(providers.environmentVariable("RELEASE_CHANGELOG").orElse("See the GitHub release notes."))
    type.set(when (releaseType) {
        "release" -> STABLE
        "beta" -> BETA
        "alpha" -> ALPHA
        else -> error("Unsupported release_type '$releaseType'")
    })
    modLoaders.add("neoforge")

    curseforge {
        projectId.set(providers.gradleProperty("curseforge_project_id"))
        accessToken.set(providers.environmentVariable("CURSEFORGE_TOKEN"))
        minecraftVersions.add(minecraftVersion)
        client.set(true)
        server.set(false)
        requires("cloth-config")
    }

    modrinth {
        projectId.set(providers.gradleProperty("modrinth_project_id"))
        accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
        minecraftVersions.add(minecraftVersion)
        requires("cloth-config")
    }
}
