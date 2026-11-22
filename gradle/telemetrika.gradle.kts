import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.jvm.tasks.Jar
import java.net.URI

val relocatedCore = rootProject.tasks.named("relocateTelemetrika")
val coreClasses = files(relocatedCore)
val telemetryEndpoint = providers.gradleProperty("telemetrikaEndpoint").orElse("").map { value ->
    if (value.isNotEmpty()) {
        val endpoint = runCatching { URI(value) }.getOrNull()
        require(endpoint != null && endpoint.scheme.equals("https", ignoreCase = true) && endpoint.host != null &&
            endpoint.rawPath == "/v1/snapshots" && endpoint.rawUserInfo == null &&
            endpoint.rawQuery == null && endpoint.rawFragment == null) {
            "telemetrikaEndpoint must be an HTTPS /v1/snapshots URL without credentials, a query, or a fragment."
        }
    }
    value
}

dependencies.add("implementation", coreClasses)

tasks.withType<JavaExec>().matching { it.name == "runClient" }.configureEach {
    providers.gradleProperty("telemetryEndpoint").orNull?.let {
        systemProperty("handycam.telemetry.endpoint", it)
    }
}

tasks.named<ProcessResources>("processResources") {
    inputs.property("telemetrikaEndpoint", telemetryEndpoint)
    filesMatching("handycam-telemetry-endpoint.txt") {
        expand(mapOf("telemetrikaEndpoint" to telemetryEndpoint.get()))
    }
}

tasks.named<Jar>("jar") {
    dependsOn(relocatedCore)
    from(coreClasses.elements.map { zipTree(it.single().asFile) }) {
        include("dev/vercim/handycam/internal/telemetrika/**", "META-INF/LICENSE-Telemetrika")
    }
}
