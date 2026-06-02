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

    // Stiffness=250, damping=31.6 ≈ critical (2*sqrt(250)=31.6)
    private final SpringSimulator spring = new SpringSimulator(250f, 32f);
    private float impactTarget = 0f;

    /**
     * @param fallDistance net downward displacement in blocks.
     *                     3-block fall → mild dip; 10-block fall → strong dip.
     */
    public void onLand(float fallDistance) {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.landingEnabled) return;

        // strength: nonlinear — small falls barely register, high falls are dramatic
        // 3 blocks → strength≈0.5, 6 blocks → strength≈1.0
        float strength = (float) Math.sqrt(Math.min(fallDistance / 6f, 1f));
        // Negative pitch = camera dips downward on impact
        impactTarget = -strength * cfg.landingIntensity * 4f;
    }

    @Override
    public CameraOffset compute(PlayerState state, float time, float dt) {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.landingEnabled) return CameraOffset.ZERO;

        float pitchVal = spring.update(impactTarget, dt);

        // Exponentially decay the target toward 0 so the spring has something to
        // return to.  τ ≈ 0.15 s  →  pow(0.0001, dt/0.15) ≈ e^(-dt/0.015)
        impactTarget *= (float) Math.exp(-dt / 0.15f);

        return new CameraOffset(pitchVal * cfg.masterIntensity, 0f, 0f);
    }
}
