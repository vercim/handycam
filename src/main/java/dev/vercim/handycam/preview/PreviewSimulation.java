package dev.vercim.handycam.preview;

import dev.vercim.handycam.rig.CameraRig;
import dev.vercim.handycam.rig.RigInput;
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

    // The body moves once per 20 Hz tick; its height is interpolated between the last two ticks so a jump is smooth.
    private float previousHeight;
    private float currentHeight;
    private int stepsIntoTick = RigSimulator.STEPS_PER_TICK;

    public PreviewSimulation(RigParams params, Random rng) {
        this.params = params;
        this.simulator = new RigSimulator(new CameraRig(rng));
    }

    /** One fixed step of {@link RigSimulator#STEP_SECONDS} with these keys held. */
    public void step(InputState input) {
        body.setInput(input);
        stepsIntoTick = stepsIntoTick % RigSimulator.STEPS_PER_TICK + 1;
        simulator.step(this::tickBody, params, RigView.DEFAULT);
    }

    private RigInput tickBody() {
        previousHeight = currentHeight;
        RigInput input = body.tick();
        currentHeight = body.y();
        return input;
    }

    /**
     * How high the player is above the ground in blocks. It follows the real jump, about 1.25 blocks at the top, and
     * is what lifts the camera in the scene. The body moves once per tick, so the height glides from the previous
     * tick's value to the current one over the steps of the current tick (and {@code alpha}, the fraction of the next
     * step already elapsed), a little behind the body but without 20 Hz jumps.
     */
    public float height(float alpha) {
        float phase = (stepsIntoTick - 1 + alpha) / RigSimulator.STEPS_PER_TICK;
        return previousHeight + (currentHeight - previousHeight) * phase;
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
