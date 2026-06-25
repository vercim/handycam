package dev.vercim.handycam;

import dev.vercim.handycam.config.HandycamConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class HandycamMod {

    public static final String MOD_ID = "handycam";

    public static void initClient(java.nio.file.Path configDir) {
        HandycamConfig.load(configDir);
    }
}
