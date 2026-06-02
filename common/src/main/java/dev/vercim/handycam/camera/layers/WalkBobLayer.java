package dev.vercim.handycam.camera.layers;

import dev.vercim.handycam.camera.CameraOffset;
import dev.vercim.handycam.camera.PlayerState;
import dev.vercim.handycam.camera.ShakeLayer;
import dev.vercim.handycam.camera.math.FractalNoise;
import dev.vercim.handycam.config.HandycamConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class WalkBobLayer implements ShakeLayer {

    private static final float TWO_PI = (float) (2.0 * Math.PI);
    private static final float TICK_DT = 1f / 20f;

    private final FractalNoise walkNoise = new FractalNoise(0xDEADBEEFL, 3, 0.5f, 0.5f);

    // bobPhase is advanced in tick() at a fixed rate — keeps frequency stable
    // regardless of frame rate
    private float bobPhase = 0f;

    @Override
    public void tick(PlayerState state) {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.walkBobEnabled) return;

        float speed = state.horizontalSpeed;
        if (speed < 0.05f) return;

        // walkBobFrequency is in Hz (cycles per second).
        // Multiply by 2π to convert to rad/s, then by TICK_DT for radians/tick.
        bobPhase += speed * cfg.walkBobFrequency * TWO_PI * TICK_DT;
    }

    @Override
    public CameraOffset compute(PlayerState state, float time, float dt) {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.walkBobEnabled) return CameraOffset.ZERO;

        float speed = state.horizontalSpeed;
        if (speed < 0.05f) return CameraOffset.ZERO;

        // Vertical: head dips on each footfall (abs-sine = two dips per cycle)
        float verticalBob = -(float) Math.abs(Math.sin(bobPhase))
                            * cfg.walkBobIntensity * speed;

        // Lateral: sway left-right at 2x frequency, smaller amplitude
        float lateralBob  =  (float) Math.sin(bobPhase * 2f)
                            * cfg.walkBobIntensity * speed * 0.35f;

        // Noise deformation — irregular footfall feel
        float noiseDeform = walkNoise.get(bobPhase * 0.5f)
                            * cfg.walkBobIntensity * speed * cfg.walkNoiseAmount;

        float master = cfg.masterIntensity;
        return new CameraOffset(
            (verticalBob + noiseDeform) * master,
            lateralBob                  * master,
            lateralBob * 0.25f          * master
        );
    }
}
