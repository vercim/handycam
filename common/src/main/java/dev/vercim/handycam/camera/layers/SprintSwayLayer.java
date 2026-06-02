package dev.vercim.handycam.camera.layers;

import dev.vercim.handycam.camera.CameraOffset;
import dev.vercim.handycam.camera.PlayerState;
import dev.vercim.handycam.camera.ShakeLayer;
import dev.vercim.handycam.camera.math.SpringSimulator;
import dev.vercim.handycam.config.HandycamConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class SprintSwayLayer implements ShakeLayer {

    private final SpringSimulator rollSpring = new SpringSimulator(50f, 14f);

    @Override
    public CameraOffset compute(PlayerState state, float time, float dt) {
        HandycamConfig cfg = HandycamConfig.get();
        if (!cfg.sprintEnabled) return CameraOffset.ZERO;

        float targetRoll = state.isSprinting ? cfg.sprintRoll : 0f;
        targetRoll += state.turnRate * cfg.turnSway;

        float roll = rollSpring.update(targetRoll, dt) * cfg.masterIntensity;
        return new CameraOffset(0f, 0f, roll);
    }
}
