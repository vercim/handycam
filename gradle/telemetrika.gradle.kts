import org.gradle.jvm.tasks.Jar

val relocatedCore = rootProject.tasks.named("relocateTelemetrika")
val coreClasses = files(relocatedCore)

dependencies.add("implementation", coreClasses)

tasks.named<Jar>("jar") {
    dependsOn(relocatedCore)
    from(coreClasses.elements.map { zipTree(it.single().asFile) }) {
        include("dev/vercim/handycam/internal/telemetrika/**", "META-INF/LICENSE-Telemetrika")
    }
}
