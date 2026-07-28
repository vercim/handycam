import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.AbstractArchiveTask

plugins {
    java
    id("dev.kikugie.loom-back-compat")
    id("me.modmuss50.mod-publish-plugin")
}

val minecraftVersion = stonecutter.current.version
val modVersion = project.property("mod_version") as String
val javaVersion = (project.property("java_version") as String).toInt()
val releaseType = providers.gradleProperty("release_type")
    .orElse(if ("-alpha" in modVersion) "alpha" else if ("-beta" in modVersion) "beta" else "release")
    .get()

group = project.property("mod_group") as String
version = "$modVersion+$minecraftVersion-fabric"
base.archivesName = project.property("mod_id") as String

java {
    toolchain.languageVersion = JavaLanguageVersion.of(javaVersion)
    withSourcesJar()
}

loom {
    runs.named("client") {
        client()
        ideConfigGenerated(true)
        runDir = "run"
        environment = "client"
        programArgs("--username=Dev")
        configName = "Handycam Fabric Client"
    }
}

repositories {
    mavenCentral()
    maven("https://maven.shedaniel.me/") { name = "Shedaniel" }
    maven("https://maven.terraformersmc.com/releases/") { name = "TerraformersMC" }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings(loom.layered { officialMojangMappings() })
    modImplementation("net.fabricmc:fabric-loader:${project.property("fabric_loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_api_version")}")
    modImplementation("me.shedaniel.cloth:cloth-config-fabric:${project.property("cloth_config_version")}")
    modImplementation("com.terraformersmc:modmenu:${project.property("modmenu_version")}")
}

tasks.processResources {
    val metadata = mapOf(
        "version" to project.version,
        "minecraft_version" to minecraftVersion,
        "fabric_loader_min_version" to project.property("fabric_loader_min_version"),
        "fabric_api_version" to project.property("fabric_api_version"),
        "cloth_config_version" to project.property("cloth_config_version"),
        "modmenu_version" to project.property("modmenu_version"),
        "java_version" to javaVersion
    )
    inputs.properties(metadata)
    filesMatching("fabric.mod.json") { expand(metadata) }
    exclude("META-INF/neoforge.mods.toml")
}

tasks.withType<JavaCompile>().configureEach {
    options.release = javaVersion
}

tasks.register<Copy>("buildAndCollect") {
    group = "build"
    description = "Builds Fabric and copies its release JAR to the root build directory."
    val releaseJar = tasks.named<AbstractArchiveTask>("remapJar")
    dependsOn("build")
    from(releaseJar.flatMap { it.archiveFile })
    into(rootProject.layout.buildDirectory.dir("libs"))
}

publishMods {
    val releaseJar = tasks.named<AbstractArchiveTask>("remapJar")
    file.set(releaseJar.flatMap { it.archiveFile })
    displayName.set("Handycam $modVersion Fabric $minecraftVersion")
    changelog.set(providers.environmentVariable("RELEASE_CHANGELOG").orElse("See the GitHub release notes."))
    type.set(when (releaseType) {
        "release" -> STABLE
        "beta" -> BETA
        "alpha" -> ALPHA
        else -> error("Unsupported release_type '$releaseType'")
    })
    modLoaders.add("fabric")

    curseforge {
        projectId.set(providers.gradleProperty("curseforge_project_id"))
        accessToken.set(providers.environmentVariable("CURSEFORGE_TOKEN"))
        minecraftVersions.add(minecraftVersion)
        client.set(true)
        server.set(false)
        requires("fabric-api")
        requires("cloth-config")
        optional("modmenu")
    }

    modrinth {
        projectId.set(providers.gradleProperty("modrinth_project_id"))
        accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
        minecraftVersions.add(minecraftVersion)
        requires("fabric-api")
        requires("cloth-config")
        optional("modmenu")
    }
}
