package dev.vercim.handycam.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

@Environment(EnvType.CLIENT)
public class HandycamConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static HandycamConfig instance;

    // ── Master ─────────────────────────────────────────────────────────────
    public float masterIntensity = 1.0f;

    // ── Idle ───────────────────────────────────────────────────────────────
    // idleIntensity = max amplitude in degrees (FractalNoise output is in [-1, 1])
    public boolean idleEnabled   = true;
    public float   idleIntensity = 0.25f;  // ±0.25° drift — subtle but alive
    public float   idleFrequency = 0.3f;   // speed multiplier for noise time

    // ── Walk Bob ───────────────────────────────────────────────────────────
    // walkBobFrequency = cycles per second at speed=1.0
    public boolean walkBobEnabled   = true;
    public float   walkBobIntensity = 0.8f;   // max degrees of vertical dip
    public float   walkBobFrequency = 1.6f;   // Hz — 1 full cycle every 0.625s
    public float   walkNoiseAmount  = 0.15f;  // irregularity factor

    // ── Landing ────────────────────────────────────────────────────────────
    public boolean landingEnabled  = true;
    public float   landingIntensity = 1.0f;   // multiplier on impact strength

    // ── Damage ─────────────────────────────────────────────────────────────
    public boolean damageEnabled   = true;
    public float   damageIntensity = 1.5f;
    public float   damageDecay     = 1.2f;    // trauma units/sec lost

    // ── Sprint ─────────────────────────────────────────────────────────────
    public boolean sprintEnabled = true;
    public float   sprintRoll    = 1.8f;   // degrees of roll when sprinting
    public float   turnSway      = 0.08f;  // roll per degree/tick of turn rate
                                           // (was 0.5 — way too strong)

    // ── Misc ───────────────────────────────────────────────────────────────
    public boolean disableVanillaBob = true;

    public static HandycamConfig get() {
        if (instance == null) instance = new HandycamConfig();
        return instance;
    }

    public static void load(Path configDir) {
        Path file = configDir.resolve("handycam.json");
        if (Files.exists(file)) {
            try (Reader r = Files.newBufferedReader(file)) {
                instance = GSON.fromJson(r, HandycamConfig.class);
            } catch (IOException e) {
                instance = new HandycamConfig();
            }
        } else {
            instance = new HandycamConfig();
            save(configDir);
        }
    }

    public static void save(Path configDir) {
        Path file = configDir.resolve("handycam.json");
        try {
            Files.createDirectories(configDir);
            try (Writer w = Files.newBufferedWriter(file)) {
                GSON.toJson(get(), w);
            }
        } catch (IOException e) {
            // silently skip — defaults still work in-game
        }
    }
}
