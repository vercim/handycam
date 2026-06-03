package dev.vercim.handycam.camera.layers;

import dev.vercim.handycam.camera.CameraOffset;
import dev.vercim.handycam.camera.PlayerState;
import dev.vercim.handycam.camera.ShakeLayer;
import dev.vercim.handycam.camera.math.SpringSimulator;
import dev.vercim.handycam.config.HandycamConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class HitImpactLayer implements ShakeLayer {

    private final SpringSimulator pitchSpring = new SpringSimulator(180f, 26f);
    private final SpringSimulator yawSpring   = new SpringSimulator(100f, 20f);
    private final SpringSimulator rollSpring  = new SpringSimulator(100f, 20f);

    // Normalized targets [0..1], config values applied in compute()
    private float pitchTarget = 0f;
    private float yawTarget   = 0f;
    private float rollTarget  = 0f;

    // Alternates yaw/roll direction for natural variation
    private int side = 1;

    public void onHit() {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.hitEnabled) return;

        side = -side;

        // Forward punch: pitch forward (down), subtle yaw/roll wobble
        pitchTarget = -1.0f;
        yawTarget   =  side * 0.3f;
        rollTarget  =  side * 0.2f;
    }

    @Override
    public CameraOffset compute(PlayerState state, float time, float dt) {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.hitEnabled) return CameraOffset.ZERO;

        float pitch = pitchSpring.update(pitchTarget, dt);
        float yaw   = yawSpring  .update(yawTarget,   dt);
        float roll  = rollSpring .update(rollTarget,  dt);

        // Decay targets
        float decay = cfg.hitDecay;
        pitchTarget *= (float) Math.exp(-dt * decay);
        yawTarget   *= (float) Math.exp(-dt * decay);
        rollTarget  *= (float) Math.exp(-dt * decay);

        float i = cfg.hitIntensity * cfg.masterIntensity;
        return new CameraOffset(pitch * i, yaw * i, roll * i);
    }
}
