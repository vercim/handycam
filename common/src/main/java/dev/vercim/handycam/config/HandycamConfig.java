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
    public float masterIntensity = 2.0f;
    public int   noiseOctaves    = 3;    // max octave layers for all FractalNoise (1–6)

    // ── Idle ───────────────────────────────────────────────────────────────
    public boolean idleEnabled      = true;
    public float   idleIntensity    = 1.5f;
    public float   idleFrequency    = 0.5f;
    public float   idleTremorScale  = 0.8f;  // tremor magnitude relative to breath (1=same)

    // ── Walk Bob ───────────────────────────────────────────────────────────
    public boolean walkBobEnabled      = true;
    public float   walkBobIntensity    = 2.5f;  // degrees per axis at full speed
    public float   walkBobFrequency    = 0.9f;  // Hz at speed=1.0
    public float   walkBobVerticalMult = 2.5f;  // vertical scale relative to other axes
    public float   walkNoiseAmount     = 0.25f;
    public float   sprintBobMult       = 2.2f;  // sprint amplitude multiplier over walk

    // ── Landing ────────────────────────────────────────────────────────────
    public boolean landingEnabled   = true;
    public float   landingIntensity = 2.0f;   // overall multiplier
    public float   landingPitchMax  = 9.0f;   // max degrees of downward pitch slam
    public float   landingRollMax   = 3.5f;   // max degrees of sideways roll
    public float   landingYawMax    = 2.5f;   // max degrees of yaw jitter

    // ── Damage ─────────────────────────────────────────────────────────────
    public boolean damageEnabled   = true;
    public float   damageIntensity = 1.5f;
    public float   damageDecay     = 1.2f;

    // ── Sprint / Sway ──────────────────────────────────────────────────────
    public boolean sprintEnabled  = true;
    public float   turnSway       = 0.08f;  // roll per degree/tick of yaw turn
    public float   maxTurnRoll    = 2.5f;   // clamp on turn-induced roll (degrees)
    public float   swayYawLag     = 0.06f;  // yaw inertia coefficient
    public float   swayPitchLag   = 3.0f;   // pitch inertia for vertical movement
                                             // jump vy ≈ 0.42 → ~1.3° pitch lag

    // ── Jump ───────────────────────────────────────────────────────────────
    public boolean jumpEnabled   = true;
    public float   jumpIntensity = 4.0f;   // degrees of pitch kick on jump
    public float   jumpDecay     = 4.0f;   // decay rate (lower = longer impulse)

    // ── Strafe Tilt ────────────────────────────────────────────────────────
    public boolean strafeTiltEnabled   = true;
    public float   strafeTiltIntensity = 3.0f;

    // ── Forward Tilt ───────────────────────────────────────────────────────
    public boolean forwardTiltEnabled   = true;
    public float   forwardTiltIntensity = 3.0f;

    // ── Crouch ─────────────────────────────────────────────────────────────
    public boolean crouchEnabled   = true;
    public float   crouchIntensity = 2.6f;

    // ── Mouse Lead ─────────────────────────────────────────────────────────
    public boolean mouseLeadEnabled   = true;
    public float   mouseLeadIntensity = 0.15f;  // crosshair lead: camera moves ahead of mouse turn

    // ── Hit Impact ─────────────────────────────────────────────────────────
    public boolean hitEnabled   = true;
    public float   hitIntensity = 1.2f;   // degrees of pitch kick on attack
    public float   hitDecay     = 8.0f;   // decay rate (higher = snappier)

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
