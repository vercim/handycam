package dev.vercim.handycam;

import dev.vercim.handycam.config.HandycamConfig;

import java.nio.file.Path;

public final class HandycamMod {

    public static final String MOD_ID = "handycam";

    public static Path configDir;

    public static void initClient(Path dir) {
        configDir = dir;
        HandycamConfig.load(dir);
    }
}
