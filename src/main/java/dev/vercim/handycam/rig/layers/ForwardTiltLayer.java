package dev.vercim.handycam.rig.layers;

import dev.vercim.handycam.rig.CameraOffset;
import dev.vercim.handycam.rig.RigInput;
import dev.vercim.handycam.rig.RigParams;
import dev.vercim.handycam.rig.RigView;
import dev.vercim.handycam.rig.ShakeLayer;
import dev.vercim.ui.sim.FractalNoise;
import dev.vercim.ui.sim.Spring1D;

public class ForwardTiltLayer implements ShakeLayer {

    private final Spring1D pitchSpring = new Spring1D(450f, 42f);

    private final Spring1D rollSpring  = new Spring1D(350f, 37f);

    private float smoothForward = 0f;

    private final FractalNoise pitchNoise = new FractalNoise(0x3C4D5E6FL, 4, 0.35f, 0.5f);
    private final FractalNoise rollNoise  = new FractalNoise(0x6F5E4D3CL, 3, 0.28f, 0.5f);

    @Override
    public CameraOffset compute(RigInput state, RigParams params, RigView view, float time, float dt) {
        if (!params.forwardTiltEnabled()) {
            pitchSpring.reset();
            rollSpring.reset();
            smoothForward = 0f;
            return CameraOffset.ZERO;
        }

        float tau = 0.05f / Math.max(params.forwardTiltDecay(), 0.1f);
        smoothForward += (state.forwardSpeed() - smoothForward) * (1f - (float) Math.exp(-dt / tau));

        float sprintMult = state.sprinting() ? 1.3f : 1.0f;

        float i = params.forwardTiltIntensity() * sprintMult * 0.6f;

        float targetPitch = -smoothForward * i;

        float targetRoll  =  smoothForward * i * 0.3f;

        float speed = params.forwardTiltDecay();
        float pitch = pitchSpring.update(targetPitch, dt, speed);
        float roll  = rollSpring .update(targetRoll,  dt, speed);

        int oct = params.noiseOctaves();
        float nAbs = Math.abs(smoothForward);
        float nPitch = pitchNoise.get(time,       oct) * nAbs * i * 0.12f;
        float nRoll  = rollNoise .get(time + 77f, oct) * nAbs * i * 0.08f;

        float m = params.masterIntensity();
        return new CameraOffset((pitch + nPitch) * m, 0f, (roll + nRoll) * m);
    }
}
