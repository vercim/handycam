package dev.vercim.handycam;

import dev.vercim.handycam.config.HandycamConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.nio.file.Path;

@Environment(EnvType.CLIENT)
public final class HandycamMod {

    public static final String MOD_ID = "handycam";

    public static Path configDir;

    public static void initClient(Path dir) {
        configDir = dir;
        HandycamConfig.load(dir);
    }
}
