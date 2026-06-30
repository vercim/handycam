package dev.vercim.handycam.camera.layers;

import dev.vercim.handycam.camera.CameraOffset;
import dev.vercim.handycam.camera.PlayerState;
import dev.vercim.handycam.camera.ShakeLayer;
import dev.vercim.handycam.camera.math.FractalNoise;
import dev.vercim.handycam.config.HandycamConfig;

public class BreathLayer implements ShakeLayer {

    private static final float TWO_PI = (float) (2.0 * Math.PI);
    
    private static final float BREATH_HZ = 0.40f;

    
    
    private final FractalNoise phaseDrift = new FractalNoise(0xF001F002L, 1, 0.09f, 0.5f);

    @Override
    public CameraOffset compute(PlayerState state, float time, float dt) {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.breathEnabled) return CameraOffset.ZERO;

        
        float concentration = 1f - state.bowDrawProgress * cfg.bowConcentration;
        if (concentration < 0f) concentration = 0f;

        float intensity = cfg.breathIntensity * cfg.masterIntensity * concentration;
        if (intensity < 1e-4f) return CameraOffset.ZERO;

        
        float drift = phaseDrift.get(time, 1) * 0.35f;
        float y = (float) Math.sin(time * BREATH_HZ * TWO_PI + drift) * intensity * 0.015f;

        return new CameraOffset(0f, 0f, 0f, 0f, y);
    }
}
