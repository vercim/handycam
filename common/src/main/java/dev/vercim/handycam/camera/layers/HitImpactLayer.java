package dev.vercim.handycam.camera.layers;

import dev.vercim.handycam.camera.CameraOffset;
import dev.vercim.handycam.camera.PlayerState;
import dev.vercim.handycam.camera.ShakeLayer;
import dev.vercim.handycam.camera.math.FractalNoise;
import dev.vercim.handycam.camera.math.SpringSimulator;
import dev.vercim.handycam.config.HandycamConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class HitImpactLayer implements ShakeLayer {

    // Spring: directional punch impulse
    private final SpringSimulator pitchSpring = new SpringSimulator(300f, 34f);
    private final SpringSimulator yawSpring   = new SpringSimulator(200f, 28f);
    private final SpringSimulator rollSpring  = new SpringSimulator(200f, 28f);

    // Noise: chaotic shake layered on top of the impulse
    // 3 octaves + fast frequency for high-frequency "rattling" feel
    private final FractalNoise noiseP = new FractalNoise(0xAABBCCDDL, 3, 25f, 0.6f);
    private final FractalNoise noiseY = new FractalNoise(0x11223344L, 3, 20f, 0.6f);
    private final FractalNoise noiseR = new FractalNoise(0x55667788L, 3, 18f, 0.5f);

    // Normalized targets [0..1] for spring impulse
    private float pitchTarget = 0f;
    private float yawTarget   = 0f;
    private float rollTarget  = 0f;

    // Trauma [0..1] drives noise amplitude
    private float trauma = 0f;

    private int side = 1;

    public void onHit() {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.hitEnabled) return;

        side = -side;

        pitchTarget = -1.0f;
        yawTarget   =  side * 0.3f;
        rollTarget  =  side * 0.2f;

        // Each hit adds trauma, capped at 1
        trauma = Math.min(trauma + 0.8f, 1.0f);
    }

    @Override
    public CameraOffset compute(PlayerState state, float time, float dt) {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.hitEnabled) return CameraOffset.ZERO;

        // Spring impulse
        float pitch = pitchSpring.update(pitchTarget, dt);
        float yaw   = yawSpring  .update(yawTarget,   dt);
        float roll  = rollSpring .update(rollTarget,  dt);

        float decay = cfg.hitDecay;
        pitchTarget *= (float) Math.exp(-dt * decay);
        yawTarget   *= (float) Math.exp(-dt * decay);
        rollTarget  *= (float) Math.exp(-dt * decay);

        // Noise layer: trauma² for nonlinear falloff (same as DamageShakeLayer)
        float shake = trauma * trauma;
        float np = noiseP.get(time) * shake * 0.4f;
        float ny = noiseY.get(time + 33f) * shake * 0.4f;
        float nr = noiseR.get(time + 66f) * shake * 0.25f;

        trauma -= cfg.hitDecay * 0.5f * dt;
        if (trauma < 0f) trauma = 0f;

        float i = cfg.hitIntensity * cfg.masterIntensity;
        return new CameraOffset((pitch + np) * i, (yaw + ny) * i, (roll + nr) * i);
    }
}
