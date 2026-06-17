package dev.vercim.handycam.camera.layers;

import dev.vercim.handycam.camera.CameraOffset;
import dev.vercim.handycam.camera.PlayerState;
import dev.vercim.handycam.camera.ShakeLayer;
import dev.vercim.handycam.camera.math.SpringSimulator;
import dev.vercim.handycam.config.HandycamConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class LandingImpactLayer implements ShakeLayer {

    // Pitch: fast stiff spring — slams down, snaps back quickly.
    private final SpringSimulator pitchSpring = new SpringSimulator(200f, 28f);
    // Roll/yaw: slightly looser — wobbles a beat longer after impact.
    private final SpringSimulator rollSpring  = new SpringSimulator(140f, 24f);
    private final SpringSimulator yawSpring   = new SpringSimulator(140f, 24f);

    // Normalized targets (0..1 range), config values applied in compute()
    private float pitchTarget = 0f;
    private float rollTarget  = 0f;
    private float yawTarget   = 0f;

    // Alternates roll/yaw direction each landing for natural variation.
    private int side = 1;

    /**
     * @param fallDistance blocks fallen (peakY − landY).
     *
     * Strength formula: 1 − exp(−d / 7)
     *   1 block  → 0.13   (barely noticeable)
     *   3 blocks → 0.35   (mild)
     *   7 blocks → 0.63   (strong)
     *  14 blocks → 0.86   (very strong)
     *  25 blocks → 0.97   (near max)
     *  ∞         → 1.0    (hard cap)
     */
    public void onLand(float fallDistance) {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.landingEnabled) return;

        // Normalized strength [0..1] — config values applied each frame in compute()
        float strength = 1f - (float) Math.exp(-fallDistance / 7f);
        strength = Math.min(strength, 1f);

        side = -side;

        pitchTarget = -strength;
        rollTarget  =  side * strength;
        yawTarget   =  side * strength * 0.5f;
    }

    @Override
    public CameraOffset compute(PlayerState state, float time, float dt) {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.landingEnabled) return CameraOffset.ZERO;

        float pitch = pitchSpring.update(pitchTarget, dt);
        float roll  = rollSpring .update(rollTarget,  dt);
        float yaw   = yawSpring  .update(yawTarget,   dt);

        // Decay targets toward 0 so springs have a destination to return to.
        float expP  = (float) Math.exp(-dt / 0.12f);
        float expRY = (float) Math.exp(-dt / 0.18f);
        pitchTarget *= expP;
        rollTarget  *= expRY;
        yawTarget   *= expRY;

        // Config values applied every frame — слайдеры работают мгновенно
        float i = cfg.landingIntensity * cfg.masterIntensity;
        return new CameraOffset(
            pitch * i * cfg.landingPitchMax,
            yaw   * i * cfg.landingYawMax,
            roll  * i * cfg.landingRollMax
        );
    }
}
