package dev.vercim.handycam;

import dev.vercim.handycam.config.HandycamConfig;
import dev.vercim.handycam.internal.telemetrika.Telemetry;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public final class HandycamMod {

    public static final String MOD_ID = "handycam";
    private static final System.Logger LOG = System.getLogger(HandycamMod.class.getName());

    public static Path configDir;
    private static volatile LoaderMetadata pendingTelemetry;
    private static Telemetry telemetry;

    public static void initClient(Path dir, String loader, String modVersion) {
        configDir = dir;
        HandycamConfig.load(dir);
        pendingTelemetry = new LoaderMetadata(loader, modVersion);
    }

    public static void startTelemetry(Minecraft client) {
        LoaderMetadata metadata = pendingTelemetry;
        if (metadata == null) return;
        pendingTelemetry = null;

        HandycamConfig config = HandycamConfig.get();
        try {
            String endpoint = telemetryEndpoint();
            if (endpoint.isBlank()) return;

            Telemetry.Builder builder = Telemetry.builder(MOD_ID)
                    .endpoint(URI.create(endpoint))
                    .stateDirectory(configDir)
                    .collectionExecutor(client::execute)
                    .enabled(() -> config.telemetryEnabled)
                    .field("minecraft_version", HandycamMod::minecraftVersion)
                    .field("loader", metadata::loader)
                    .field("mod_version", metadata::modVersion);
            telemetry = HandycamTelemetrySettings.register(builder, config).build();
            telemetry.start();
        } catch (IOException | IllegalArgumentException ex) {
            LOG.log(System.Logger.Level.WARNING, "HandyCam telemetry endpoint is invalid or unavailable");
        }
    }

    public static void stopTelemetry() {
        pendingTelemetry = null;
        if (telemetry != null) {
            telemetry.close();
            telemetry = null;
        }
    }

    private static String telemetryEndpoint() throws IOException {
        String override = System.getProperty("handycam.telemetry.endpoint");
        if (override != null) return override;
        try (var resource = HandycamMod.class.getResourceAsStream("/handycam-telemetry-endpoint.txt")) {
            return resource == null ? "" : new String(resource.readAllBytes(), StandardCharsets.UTF_8).strip();
        }
    }

    private static String minecraftVersion() {
        //? if >=1.21.10 {
        return SharedConstants.getCurrentVersion().name();
        //?} else {
        /*return SharedConstants.getCurrentVersion().getName();
        *///?}
    }

    private record LoaderMetadata(String loader, String modVersion) {}
}
