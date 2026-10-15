package dev.vercim.handycam.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.vercim.ui.config.ConfigModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

public class HandycamConfig {

    private static final int CURRENT_VERSION = 4;

    public static final String FILE_NAME = "handycam.json";
    private static final String VERSION_KEY = "configVersion";

    private static HandycamConfig instance;

    /** Keys of the file that this version does not know, written back unchanged on save. */
    private static JsonObject unknownKeys = new JsonObject();

    public int configVersion = 0;

    public boolean effectsEnabled            = true;
    public boolean disableInCreativeFlight   = true;
    public boolean enableEffectsThirdPerson  = false;
    public boolean enableEffectsSecondPerson = false;
    public boolean enableVanillaFov          = true;

    public float masterIntensity = 1.6f;
    public int   noiseOctaves    = 4;

    public boolean breathEnabled   = true;
    public float   breathIntensity = 1.0f;

    public boolean idleEnabled      = true;
    public float   idleIntensity    = 1.5f;
    public float   idleFrequency    = 0.5f;
    public float   idleTremorScale  = 0.75f;

    public boolean walkBobEnabled      = false;
    public float   walkBobIntensity    = 2.5f;
    public float   walkBobFrequency    = 0.90f;
    public float   walkBobVerticalMult = 2.0f;
    public float   sprintBobMult       = 1.80f;

    public boolean landingEnabled   = true;
    public float   landingIntensity = 3.85f;
    public float   landingPitchMax  = 9.0f;
    public float   landingRollMax   = 3.5f;
    public float   landingYawMax    = 2.5f;

    public boolean damageEnabled   = true;
    public float   damageIntensity = 2.0f;
    public float   damageDecay     = 1.2f;

    public float   turnSway           = 0.096f;
    public float   maxTurnRoll        = 3.0f;
    public boolean   cameraSwayEnabled  = true;
    public SwayMode  cameraSwayMode     = SwayMode.LEAD;

    public enum SwayMode { LAG, LEAD }
    public float   swayYawLag         = 0.08f;
    public float   swayPitchLag       = 0.14f;

    public boolean jumpEnabled   = true;
    public float   jumpIntensity = 4.1f;
    public float   jumpDecay     = 5.1f;

    public boolean strafeTiltEnabled   = true;
    public float   strafeTiltIntensity = 2.4f;
    public float   strafeTiltDecay    = 1.0f;

    public boolean forwardTiltEnabled   = true;
    public float   forwardTiltIntensity = 2.4f;
    public float   forwardTiltDecay    = 1.0f;

    public boolean crouchEnabled   = true;
    public float   crouchIntensity = 3.2f;

    public boolean mouseLeadEnabled        = true;
    public float   mouseSwayScale          = 0.30f;
    public float   verticalDriftIntensity  = 0.9f;
    public float   mouseSwaySmoothing      = 0.09f;

    public boolean hitEnabled   = true;
    public float   hitIntensity = 2.0f;
    public float   hitDecay     = 20.0f;

    public boolean          eatEnabled        = true;
    public float            eatIntensity      = 1.5f;
    public float            eatSwayAmount     = 1.2f;
    public EatSwayDirection eatSwayDirection  = EatSwayDirection.RANDOM;

    public enum EatSwayDirection { RIGHT, LEFT, RANDOM }

    public boolean bowEnabled        = true;
    public float   bowRecoilIntensity = 2.5f;
    public float   bowRecoilDecay     = 9.0f;
    public float   bowConcentration       = 0.90f;
    public boolean bowDrawTiltEnabled     = false;
    public boolean bowCrosshairShrinkEnabled = false;
    public float   bowCrosshairShrink    = 0.20f;

    // Explosions
    public boolean explosionEnabled     = true;
    public boolean lightningEnabled     = true;
    public float   explosionIntensity   = 1.5f;
    public float   explosionMaxDistance = 20.0f;
    public float   explosionDecay       = 0.6f;

    public static HandycamConfig get() {
        if (instance == null) instance = new HandycamConfig();
        return instance;
    }

    /**
     * Reads {@code handycam.json} into a fresh config (fields keep their defaults for missing or unusable keys),
     * migrates it and writes it back. An unreadable file is kept as {@code handycam.json.bak}.
     */
    public static void load(Path configDir) {
        Path file = configDir.resolve(FILE_NAME);
        HandycamConfig loaded = new HandycamConfig();
        ConfigModel model = HandycamOptions.create(loaded);
        try {
            model.load(file);
        } catch (IOException e) {
            keepBackup(file);
            loaded = new HandycamConfig();
            model = HandycamOptions.create(loaded);
        }

        loaded.configVersion = readVersion(model.extras().get(VERSION_KEY));
        unknownKeys = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : model.extras().entrySet()) {
            if (!entry.getKey().equals(VERSION_KEY)) unknownKeys.add(entry.getKey(), entry.getValue());
        }

        instance = loaded;
        instance.migrate();
        save(configDir);
    }

    private static int readVersion(JsonElement element) {
        try {
            return element != null && element.isJsonPrimitive() ? element.getAsInt() : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static void keepBackup(Path file) {
        try {
            if (Files.exists(file)) {
                Files.copy(file, file.resolveSibling(file.getFileName() + ".bak"), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ignored) {
            // Nothing more can be done for a file we cannot even copy.
        }
    }

    void migrate() {
        if (configVersion < 1) {
            strafeTiltIntensity  = 2.4f;
            forwardTiltIntensity = 2.4f;
        }
        if (configVersion < 2) {
            turnSway    = 0.096f;
            maxTurnRoll = 3.0f;
        }
        if (configVersion < 3) {
            if (sameValue(forwardTiltIntensity, 3.0f)) forwardTiltIntensity = 2.4f;
            if (sameValue(strafeTiltIntensity, 3.0f)) strafeTiltIntensity = 2.4f;
            if (sameValue(turnSway, 0.08f)) turnSway = 0.096f;
            if (sameValue(maxTurnRoll, 2.5f)) maxTurnRoll = 3.0f;
        }
        if (configVersion < 4) {
            if (sameValue(masterIntensity, 2.0f)) masterIntensity = 1.6f;
        }
        configVersion = CURRENT_VERSION;
    }

    private static boolean sameValue(float a, float b) {
        return Math.abs(a - b) < 0.0001f;
    }

    public static void save(Path configDir) {
        HandycamConfig config = get();
        ConfigModel model = HandycamOptions.create(config);
        model.extras().addProperty(VERSION_KEY, config.configVersion);
        for (Map.Entry<String, JsonElement> entry : unknownKeys.entrySet()) {
            model.extras().add(entry.getKey(), entry.getValue());
        }
        try {
            model.save(configDir.resolve(FILE_NAME));
        } catch (IOException e) {
            // As before, a config that cannot be written is not worth failing the game over.
        }
    }
}
