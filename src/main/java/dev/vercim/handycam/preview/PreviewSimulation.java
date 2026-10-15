package dev.vercim.handycam.preview;

import dev.vercim.handycam.rig.CameraRig;
import dev.vercim.handycam.rig.RigOutput;
import dev.vercim.handycam.rig.RigParams;
import dev.vercim.handycam.rig.RigSimulator;
import dev.vercim.handycam.rig.RigView;
import dev.vercim.ui.scene.InputState;

import java.util.Random;

/**
 * The preview without any drawing: a {@link PreviewBody} feeding the same {@link CameraRig} the game uses, stepped at
 * 1/120 s with a rig tick every sixth step. The settings come from {@code params}, which the config screen backs with
 * the values being edited, so a slider changes the result on the very next step.
 */
public final class PreviewSimulation {

    /** Health taken by the "damage" button, out of 20. */
    public static final float DAMAGE_AMOUNT = 4f;

    private final PreviewBody body = new PreviewBody();
    private final RigSimulator simulator;
    private final RigParams params;

    public PreviewSimulation(RigParams params, Random rng) {
        this.params = params;
        this.simulator = new RigSimulator(new CameraRig(rng));
    }

    /** One fixed step of {@link RigSimulator#STEP_SECONDS} with these keys held. */
    public void step(InputState input) {
        body.setInput(input);
        simulator.step(body::tick, params, RigView.DEFAULT);
    }

    /** The camera's current tilt, interpolated by the fraction of the next step already elapsed. */
    public RigOutput output(float alpha) {
        return simulator.output(alpha);
    }

    /** A hit taken, as when a mob lands a blow. */
    public void damage() {
        body.damage(DAMAGE_AMOUNT);
    }

    /** A swing at something. */
    public void hit() {
        body.hit();
    }

    /** An explosion nearby; {@code severity} is 0 to 1 like the game's distance-scaled value. */
    public void explosion(float severity) {
        simulator.rig().onExplosion(severity);
    }
}
