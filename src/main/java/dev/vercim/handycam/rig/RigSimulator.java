package dev.vercim.handycam.rig;

import dev.vercim.ui.sim.FixedStepRunner;

/**
 * Drives a {@link CameraRig} from real frame times the way a host without a game loop (the config preview) needs:
 * fixed 1/120 s steps, a rig tick every 6th step (20 Hz, like the game), and an output interpolated between the last
 * two steps so rendering stays smooth at any frame rate.
 */
public final class RigSimulator {

    public static final double STEP_SECONDS = 1.0 / 120.0;
    public static final int STEPS_PER_TICK = 6;

    /** Supplies the player's state for each 20 Hz tick. */
    @FunctionalInterface
    public interface TickSource {
        RigInput nextTick();
    }

    private final CameraRig rig;
    private final FixedStepRunner runner = new FixedStepRunner(STEP_SECONDS);

    private RigInput currentInput = RigInput.IDLE;
    private int stepInTick = 0;
    private double clock = 0.0;
    private RigOutput previous = RigOutput.ZERO;
    private RigOutput current = RigOutput.ZERO;

    public RigSimulator(CameraRig rig) {
        this.rig = rig;
    }

    public CameraRig rig() {
        return rig;
    }

    /** Advances by a rendered frame of {@code frameSeconds}; returns how many fixed steps ran. */
    public int advance(double frameSeconds, TickSource source, RigParams params, RigView view) {
        return runner.advance(frameSeconds, dt -> step(source, params, view));
    }

    /**
     * Runs exactly one fixed step, for a host that already steps at {@link #STEP_SECONDS} (the preview widget);
     * {@link #output(float)} then takes that host's own interpolation fraction.
     */
    public void step(TickSource source, RigParams params, RigView view) {
        if (stepInTick == 0) {
            currentInput = source.nextTick();
            rig.tick(currentInput, params);
        }
        stepInTick = (stepInTick + 1) % STEPS_PER_TICK;

        clock += STEP_SECONDS;
        previous = current;
        current = rig.compute(currentInput, params, view, (float) clock, (float) STEP_SECONDS);
    }

    /** The output for the current moment, interpolated by the fraction of a step left over by {@link #advance}. */
    public RigOutput output() {
        return output(runner.alpha());
    }

    public RigOutput output(float alpha) {
        return previous.lerp(current, alpha);
    }

    /** Forgets the leftover frame time, e.g. after the preview was paused. */
    public void resetTiming() {
        runner.reset();
    }
}
