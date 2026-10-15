package dev.vercim.ui.sim;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FixedStepRunnerTest {

    private static final double STEP = 1.0 / 120.0;

    @Test
    void runsWholeStepsAndKeepsTheRemainder() {
        FixedStepRunner runner = new FixedStepRunner(STEP);
        double[] total = {0};

        int steps = runner.advance(0.0375, dt -> total[0] += dt); // 4.5 steps

        assertEquals(4, steps);
        assertEquals(4 * STEP, total[0], 1e-12);
        assertEquals(0.5f, runner.alpha(), 1e-4f);
    }

    @Test
    void shortFramesAccumulateUntilAStepFits() {
        FixedStepRunner runner = new FixedStepRunner(STEP);
        int[] count = {0};

        assertEquals(0, runner.advance(0.004, dt -> count[0]++));
        assertEquals(0, runner.advance(0.004, dt -> count[0]++));
        assertEquals(1, runner.advance(0.004, dt -> count[0]++));
        assertEquals(1, count[0]);
    }

    @Test
    void stepSizeIsConstant() {
        FixedStepRunner runner = new FixedStepRunner(STEP);
        runner.advance(0.1, dt -> assertEquals(STEP, dt));
    }

    @Test
    void framesAtExactlyTheStepRateRunOneStepEach() {
        FixedStepRunner runner = new FixedStepRunner(STEP);
        int[] count = {0};
        for (int i = 0; i < 1200; i++) {
            assertEquals(1, runner.advance(STEP, dt -> count[0]++), "frame " + i);
        }
        assertEquals(1200, count[0]);
    }

    @Test
    void longStallIsClampedToTheMaximumFrame() {
        FixedStepRunner runner = new FixedStepRunner(STEP, 0.25);
        int[] count = {0};

        runner.advance(5.0, dt -> count[0]++);

        assertEquals(30, count[0]); // 0.25 s at 1/120 s
    }

    @Test
    void defaultMaximumFrameIsTwoHundredFiftyMilliseconds() {
        FixedStepRunner runner = new FixedStepRunner(STEP);
        int[] count = {0};
        runner.advance(10.0, dt -> count[0]++);
        assertEquals(30, count[0]);
    }

    @Test
    void ignoresNonPositiveAndNaNFrames() {
        FixedStepRunner runner = new FixedStepRunner(STEP);
        int[] count = {0};
        assertEquals(0, runner.advance(0.0, dt -> count[0]++));
        assertEquals(0, runner.advance(-1.0, dt -> count[0]++));
        assertEquals(0, runner.advance(Double.NaN, dt -> count[0]++));
        assertEquals(0, count[0]);
        assertEquals(0f, runner.alpha());
    }

    @Test
    void alphaStaysInRange() {
        FixedStepRunner runner = new FixedStepRunner(STEP);
        for (int i = 0; i < 1000; i++) {
            runner.advance(0.003 + (i % 7) * 0.002, dt -> {});
            float alpha = runner.alpha();
            assertTrue(alpha >= 0f && alpha <= 1f, "alpha " + alpha);
        }
    }

    @Test
    void resetDropsTheRemainder() {
        FixedStepRunner runner = new FixedStepRunner(STEP);
        runner.advance(0.005, dt -> {});
        assertTrue(runner.alpha() > 0f);

        runner.reset();

        assertEquals(0f, runner.alpha());
    }

    @Test
    void rejectsInvalidConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> new FixedStepRunner(0.0));
        assertThrows(IllegalArgumentException.class, () -> new FixedStepRunner(-1.0));
        assertThrows(IllegalArgumentException.class, () -> new FixedStepRunner(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> new FixedStepRunner(0.1, 0.05));
    }
}
