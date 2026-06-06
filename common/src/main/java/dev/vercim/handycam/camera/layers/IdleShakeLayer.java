package dev.vercim.handycam.camera.layers;

import dev.vercim.handycam.camera.CameraOffset;
import dev.vercim.handycam.camera.PlayerState;
import dev.vercim.handycam.camera.ShakeLayer;
import dev.vercim.handycam.camera.math.FractalNoise;
import dev.vercim.handycam.camera.math.SpringSimulator;
import dev.vercim.handycam.config.HandycamConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Random;

@Environment(EnvType.CLIENT)
public class IdleShakeLayer implements ShakeLayer {

    // ── Breath shake (slow, continuous) ────────────────────────────────────
    private final FractalNoise noisePitch = new FractalNoise(0x1A2B3C4DL, 4, 0.3f, 0.5f);
    private final FractalNoise noiseYaw   = new FractalNoise(0x5E6F7A8BL, 4, 0.3f, 0.5f);
    private final FractalNoise noiseRoll  = new FractalNoise(0x9C0D1E2FL, 3, 0.2f, 0.4f);

    // ── Hand tremor — soft, slow springs for gentle drift feel ─────────────
    private final SpringSimulator tremorPitch = new SpringSimulator(25f, 9f);
    private final SpringSimulator tremorYaw   = new SpringSimulator(20f, 8f);
    private final SpringSimulator tremorRoll  = new SpringSimulator(18f, 8f);

    private float tremorPitchTarget = 0f;
    private float tremorYawTarget   = 0f;
    private float tremorRollTarget  = 0f;

    // Countdown to next tremor — initialised to a random interval
    private float tremorTimer = 4f;
    private final Random rng  = new Random();

    @Override
    public CameraOffset compute(PlayerState state, float time, float dt) {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.idleEnabled) return CameraOffset.ZERO;

        // Концентрация: при натяжении лука idle-дрожь гасится (steady aim).
        float concentration = 1f - state.bowDrawProgress * cfg.bowConcentration;
        if (concentration < 0f) concentration = 0f;

        float intensity = cfg.idleIntensity * cfg.masterIntensity * concentration;
        float t = time * cfg.idleFrequency;
        int oct = cfg.noiseOctaves;

        // ── Breath ─────────────────────────────────────────────────────────
        float breathP = noisePitch.get(t,        oct) * intensity;
        float breathY = noiseYaw  .get(t + 100f, oct) * intensity;
        float breathR = noiseRoll .get(t + 200f, oct) * intensity * 0.4f;

        // ── Hand tremor ────────────────────────────────────────────────────
        tremorTimer -= dt;
        if (tremorTimer <= 0f) {
            float mag = intensity * (cfg.idleTremorScale * (0.6f + rng.nextFloat() * 0.4f));
            tremorPitchTarget = (rng.nextFloat() * 2f - 1f) * mag;
            tremorYawTarget   = (rng.nextFloat() * 2f - 1f) * mag * 0.8f;
            tremorRollTarget  = (rng.nextFloat() * 2f - 1f) * mag * 0.5f;
            // Next tremor in 1.5–4 seconds
            tremorTimer = 1.5f + rng.nextFloat() * 2.5f;
        }

        float tp = tremorPitch.update(tremorPitchTarget, dt);
        float ty = tremorYaw  .update(tremorYawTarget,   dt);
        float tr = tremorRoll .update(tremorRollTarget,  dt);

        // Slow decay — tremor lingers before returning to zero (τ = 0.6s)
        tremorPitchTarget *= (float) Math.exp(-dt / 0.6f);
        tremorYawTarget   *= (float) Math.exp(-dt / 0.6f);
        tremorRollTarget  *= (float) Math.exp(-dt / 0.6f);

        return new CameraOffset(
            breathP + tp,
            breathY + ty,
            breathR + tr
        );
    }
}
