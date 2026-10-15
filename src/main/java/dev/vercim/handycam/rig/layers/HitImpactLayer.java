package dev.vercim.handycam.rig.layers;

import dev.vercim.handycam.rig.CameraOffset;
import dev.vercim.handycam.rig.RigInput;
import dev.vercim.handycam.rig.RigParams;
import dev.vercim.handycam.rig.RigView;
import dev.vercim.handycam.rig.ShakeLayer;
import dev.vercim.ui.sim.FractalNoise;
import dev.vercim.ui.sim.Spring1D;

public class HitImpactLayer implements ShakeLayer {

    private final Spring1D pitchSpring = new Spring1D(300f, 34f);
    private final Spring1D yawSpring   = new Spring1D(200f, 28f);
    private final Spring1D rollSpring  = new Spring1D(200f, 28f);

    private final FractalNoise noiseP = new FractalNoise(0xAABBCCDDL, 3, 25f, 0.6f);
    private final FractalNoise noiseY = new FractalNoise(0x11223344L, 3, 20f, 0.6f);
    private final FractalNoise noiseR = new FractalNoise(0x55667788L, 3, 18f, 0.5f);

    private float pitchTarget = 0f;
    private float yawTarget   = 0f;
    private float rollTarget  = 0f;

    private float trauma = 0f;

    private int side = 1;

    public void onHit(RigParams params) {
        if (!params.hitEnabled()) return;

        side = -side;

        pitchTarget = -1.0f;
        yawTarget   =  side * 0.3f;
        rollTarget  =  side * 0.2f;

        trauma = Math.min(trauma + 0.8f, 1.0f);
    }

    @Override
    public CameraOffset compute(RigInput state, RigParams params, RigView view, float time, float dt) {
        if (!params.hitEnabled()) return CameraOffset.ZERO;

        float pitch = pitchSpring.update(pitchTarget, dt);
        float yaw   = yawSpring  .update(yawTarget,   dt);
        float roll  = rollSpring .update(rollTarget,  dt);

        float decay = params.hitDecay();
        pitchTarget *= (float) Math.exp(-dt * decay);
        yawTarget   *= (float) Math.exp(-dt * decay);
        rollTarget  *= (float) Math.exp(-dt * decay);

        int oct = params.noiseOctaves();
        float shake = trauma * trauma;
        float np = noiseP.get(time,       oct) * shake * 0.4f;
        float ny = noiseY.get(time + 33f, oct) * shake * 0.4f;
        float nr = noiseR.get(time + 66f, oct) * shake * 0.25f;

        trauma -= params.hitDecay() * 0.5f * dt;
        if (trauma < 0f) trauma = 0f;

        float i = params.hitIntensity() * params.masterIntensity();
        return new CameraOffset((pitch + np) * i, (yaw + ny) * i, (roll + nr) * i);
    }
}
