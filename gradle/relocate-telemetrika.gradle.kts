import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.Remapper
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

buildscript {
    repositories {
        mavenCentral()
        maven("https://maven.fabricmc.net/")
    }
    dependencies {
        classpath("org.ow2.asm:asm-commons:9.9.1")
    }
}

abstract class RelocateTelemetrika : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val inputJar: RegularFileProperty

    @get:OutputFile
    abstract val outputJar: RegularFileProperty

    @TaskAction
    fun relocate() {
        val source = "im/verc/telemetrika/"
        val target = "dev/vercim/handycam/internal/telemetrika/"
        val remapper = object : Remapper(Opcodes.ASM9) {
            override fun map(internalName: String): String =
                if (internalName.startsWith(source)) target + internalName.removePrefix(source) else internalName
        }
        val output = outputJar.get().asFile
        val license = "META-INF/LICENSE-Telemetrika"

        JarFile(inputJar.get().asFile).use { input ->
            check(input.getJarEntry(source + "Telemetry.class") != null) { "Missing Telemetrika API in the JAR." }
            val licenseEntry = checkNotNull(input.getJarEntry(license)) { "Missing license in the Telemetrika JAR." }
            output.parentFile.mkdirs()
            JarOutputStream(output.outputStream().buffered()).use { jar ->
                input.entries().asSequence()
                    .filter { it.name.startsWith(source) && it.name.endsWith(".class") }
                    .sortedBy { it.name }
                    .forEach { entry ->
                        val reader = input.getInputStream(entry).use { ClassReader(it) }
                        val writer = ClassWriter(0)
                        reader.accept(ClassRemapper(writer, remapper), 0)
                        writeEntry(jar, remapper.map(reader.className) + ".class", writer.toByteArray())
                    }
                val licenseBytes = input.getInputStream(licenseEntry).use { it.readAllBytes() }
                writeEntry(jar, license, licenseBytes)
            }
        }
    }

    private fun writeEntry(jar: JarOutputStream, name: String, bytes: ByteArray) {
        jar.putNextEntry(JarEntry(name).apply { time = 0L })
        jar.write(bytes)
        jar.closeEntry()
    }
}

repositories {
    maven {
        name = "githubPackages"
        url = uri("https://maven.pkg.github.com/vercim/handycam")
        content { includeModule("im.verc.telemetrika", "telemetrika") }
        credentials(PasswordCredentials::class)
    }
}

val telemetrikaVersion = providers.gradleProperty("telemetrikaVersion").get()
val telemetrikaDependency = dependencies.create("im.verc.telemetrika:telemetrika:$telemetrikaVersion")
val telemetrikaPackage = configurations.detachedConfiguration(telemetrikaDependency).apply {
    isTransitive = false
}

tasks.register<RelocateTelemetrika>("relocateTelemetrika") {
    val localJar = providers.gradleProperty("telemetrikaJar").map { file(it) }
    val packageJar = providers.provider { telemetrikaPackage.singleFile }
    inputJar.set(layout.file(localJar.orElse(packageJar)))
    outputJar = layout.buildDirectory.file("telemetrika/telemetrika-handycam.jar")
}
