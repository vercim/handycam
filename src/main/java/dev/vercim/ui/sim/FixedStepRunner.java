// SPDX-License-Identifier: LGPL-3.0-only
package dev.vercim.ui.sim;

/**
 * Fixed-timestep accumulator: feed it real frame times, it runs the simulation in constant steps and reports how far
 * the next step is through ({@link #alpha()}) so the renderer can interpolate between the last two states.
 */
public final class FixedStepRunner {

    /** A frame longer than this is treated as this long, so a stall cannot trigger a burst of catch-up steps. */
    public static final double DEFAULT_MAX_FRAME_SECONDS = 0.25;

    private static final double EPSILON = 1.0e-9;

    @FunctionalInterface
    public interface Step {
        void run(double stepSeconds);
    }

    private final double stepSeconds;
    private final double maxFrameSeconds;
    private double accumulator;

    public FixedStepRunner(double stepSeconds) {
        this(stepSeconds, DEFAULT_MAX_FRAME_SECONDS);
    }

    public FixedStepRunner(double stepSeconds, double maxFrameSeconds) {
        if (!(stepSeconds > 0.0)) throw new IllegalArgumentException("stepSeconds must be positive");
        if (!(maxFrameSeconds >= stepSeconds)) {
            throw new IllegalArgumentException("maxFrameSeconds must be at least one step");
        }
        this.stepSeconds = stepSeconds;
        this.maxFrameSeconds = maxFrameSeconds;
    }

    /** Adds one frame and runs as many whole steps as fit. Non-positive or NaN frame times are ignored. */
    public int advance(double frameSeconds, Step step) {
        if (!(frameSeconds > 0.0)) return 0;
        accumulator += Math.min(frameSeconds, maxFrameSeconds);

        int steps = 0;
        while (accumulator + EPSILON >= stepSeconds) {
            accumulator -= stepSeconds;
            step.run(stepSeconds);
            steps++;
        }
        if (accumulator < 0.0) accumulator = 0.0;
        return steps;
    }

    /** Fraction of a step left over after the last {@link #advance}, in [0, 1). */
    public float alpha() {
        return (float) Math.min(accumulator / stepSeconds, 1.0);
    }

    public double stepSeconds() {
        return stepSeconds;
    }

    /** Drops the leftover time, e.g. after the preview was paused. */
    public void reset() {
        accumulator = 0.0;
    }
}
