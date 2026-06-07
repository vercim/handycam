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

    // InstanceCreator ensures GSON calls new HandycamConfig() before overlaying JSON,
    // so field-initializer defaults apply to any field missing from the saved file.
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(HandycamConfig.class,
                    (com.google.gson.InstanceCreator<HandycamConfig>) t -> new HandycamConfig())
            .create();
    private static HandycamConfig instance;

    // ── General ────────────────────────────────────────────────────────────
    public boolean disableInCreativeFlight = true;  // отключить все эффекты при полёте в креативе

    // ── Master ─────────────────────────────────────────────────────────────
    public float masterIntensity = 2.0f;
    public int   noiseOctaves    = 4;    // max octave layers for all FractalNoise (1–6)

    // ── Idle ───────────────────────────────────────────────────────────────
    public boolean idleEnabled      = true;
    public float   idleIntensity    = 1.5f;
    public float   idleFrequency    = 0.5f;
    public float   idleTremorScale  = 0.75f;  // tremor magnitude relative to breath (1=same)

    // ── Walk Bob ───────────────────────────────────────────────────────────
    public boolean walkBobEnabled      = false;
    public float   walkBobIntensity    = 2.5f;  // degrees per axis at full speed
    public float   walkBobFrequency    = 0.90f; // Hz at speed=1.0
    public float   walkBobVerticalMult = 2.0f;  // vertical pitch scale relative to other axes
    public float   walkBobLateralMult  = 1.5f;  // lateral roll+yaw scale relative to base
    public float   walkNoiseAmount     = 0.25f;
    public float   sprintBobMult       = 1.80f; // sprint amplitude multiplier over walk

    // ── Landing ────────────────────────────────────────────────────────────
    public boolean landingEnabled   = true;
    public float   landingIntensity = 3.85f;   // overall multiplier
    public float   landingPitchMax  = 9.0f;   // max degrees of downward pitch slam
    public float   landingRollMax   = 3.5f;   // max degrees of sideways roll
    public float   landingYawMax    = 2.5f;   // max degrees of yaw jitter

    // ── Damage ─────────────────────────────────────────────────────────────
    public boolean damageEnabled   = true;
    public float   damageIntensity = 2.0f;
    public float   damageDecay     = 1.2f;

    // ── Sprint / Sway ──────────────────────────────────────────────────────
    public float   turnSway           = 0.08f;  // roll per degree/tick of yaw turn
    public float   maxTurnRoll        = 2.5f;   // clamp on turn-induced roll (degrees)
    public boolean cameraSwayEnabled  = true;   // turn roll + yaw/pitch inertia on mouse movement
    public boolean cameraSwayLead     = true;   // true = lead (опережение), false = lag (отставание)
    public float   swayYawLag         = 0.08f;  // yaw inertia coefficient
    public float   swayPitchLag       = 0.14f;  // pitch inertia coefficient

    // ── Jump ───────────────────────────────────────────────────────────────
    public boolean jumpEnabled   = true;
    public float   jumpIntensity = 4.1f;   // degrees of pitch kick on jump
    public float   jumpDecay     = 5.1f;   // decay rate (lower = longer impulse)

    // ── Strafe Tilt ────────────────────────────────────────────────────────
    public boolean strafeTiltEnabled   = true;
    public float   strafeTiltIntensity = 3.0f;
    public float   strafeTiltDecay    = 1.0f;

    // ── Forward Tilt ───────────────────────────────────────────────────────
    public boolean forwardTiltEnabled   = true;
    public float   forwardTiltIntensity = 3.0f;
    public float   forwardTiltDecay    = 1.0f;

    // ── Crouch ─────────────────────────────────────────────────────────────
    public boolean crouchEnabled   = true;
    public float   crouchIntensity = 3.2f;

    // ── Crosshair Drift ────────────────────────────────────────────────────
    public boolean mouseLeadEnabled        = true;
    public float   mouseSwayScale          = 0.30f;  // horizontal sway on mouse turn/pitch
    public float   verticalDriftIntensity  = 0.9f;   // vertical drift on jump/fall
    public float   mouseSwaySmoothing      = 0.09f;  // lag/smoothing for mouse sway (higher = slower)

    // ── Impact: Hit (рукой) ──────────────────────────────────────────────────
    public boolean hitEnabled   = true;
    public float   hitIntensity = 2.0f;   // degrees of pitch kick on attack
    public float   hitDecay     = 20.0f;   // decay rate (higher = snappier)

    // ── Impact: Bow (выстрел из лука) ─────────────────────────────────────────
    public boolean bowEnabled        = true;
    public float   bowRecoilIntensity = 2.5f;  // сила отдачи при выстреле
    public float   bowRecoilDecay     = 9.0f;  // скорость затухания отдачи
    public float   bowConcentration       = 0.90f; // 0..1 — насколько гасится idle-дрожь при макс. натяжении
    public boolean bowDrawTiltEnabled     = true;  // включить смещение камеры при натяжении
    public float   bowCrosshairShrink    = 0.20f; // насколько уменьшается прицел при макс. натяжении (0=нет, 1=полностью)

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
            save(configDir); // записываем обратно, чтобы новые поля появились в файле
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
