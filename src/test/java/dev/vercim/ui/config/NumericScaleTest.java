package dev.vercim.ui.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NumericScaleTest {

    /** Like handycam's global intensity: 0 to 200 % of a default of 1.6, one step per percent. */
    private static Option<Float> intensity() {
        return Option.standalone("masterIntensity", Float.class, 1.6f).range(0f, 3.2f, 0.016f);
    }

    private static Option<Integer> octaves() {
        return Option.standalone("noiseOctaves", Integer.class, 4).range(2, 5, 1);
    }

    @Test
    void endsAreExactAndTheDefaultSitsWhereItBelongs() {
        NumericScale scale = NumericScale.of(intensity());

        assertEquals(0.0, scale.valueAt(0.0));
        assertEquals(3.2f, (float) scale.valueAt(1.0));
        assertEquals(0.5, scale.defaultFraction(), 1e-6);
        assertEquals(200, scale.steps());
    }

    @Test
    void theDefaultIsASnapPointEvenWhenTheGridMissesIt() {
        Option<Float> option = intensity();
        NumericScale scale = NumericScale.of(option);

        // 100 steps of the float step 0.016f is not exactly the float 1.6f; the default must still come out exact.
        double onGrid = 0.0 + 100 * (double) 0.016f;
        assertNotEquals((double) 1.6f, onGrid, "the premise of this test: the grid misses the default");

        assertEquals((double) 1.6f, scale.valueAt(0.5));
        NumericScale.set(option, scale.valueAt(0.5));
        assertTrue(option.isDefault());
        assertEquals(1.6f, option.pending());
    }

    @Test
    void theMagnetMakesTheDefaultReachableOnAShortSlider() {
        NumericScale scale = NumericScale.of(intensity());

        // 107 positions for 200 steps: no position falls within half a step of the default
        double span = 107.0;
        boolean reachedWithout = false;
        boolean reachedWith = false;
        for (int y = 0; y <= 107; y++) {
            double fraction = y / span;
            reachedWithout |= scale.valueAt(fraction) == (double) 1.6f;
            reachedWith |= scale.valueAt(fraction, 2.0 / span) == (double) 1.6f;
        }
        assertFalse(reachedWithout, "the premise: without a magnet the default is out of reach");
        assertTrue(reachedWith);
    }

    @Test
    void theMagnetOnlyReachesAsFarAsItsWidth() {
        NumericScale scale = NumericScale.of(intensity());

        assertEquals((double) 1.6f, scale.valueAt(0.5 + 0.01, 0.02));
        assertNotEquals((double) 1.6f, scale.valueAt(0.5 + 0.03, 0.02));
        assertEquals(0.0, scale.valueAt(0.0, 0.02), "the ends are not pulled to the default");
        assertEquals(3.2f, (float) scale.valueAt(1.0, 0.02));
    }

    @Test
    void aDefaultOutsideTheRangeIsNeverReturned() {
        Option<Float> option = Option.standalone("odd", Float.class, 5f).range(0f, 2f, 0.1f);
        NumericScale scale = NumericScale.of(option);

        assertEquals(2.0, scale.valueAt(1.0, 1.0));
        assertEquals(2.0, scale.snap(2.0));
        assertTrue(scale.valueAt(0.7, 1.0) <= 2.0);
    }

    @Test
    void valuesSnapToTheStepGrid() {
        NumericScale scale = NumericScale.of(intensity());

        double v = scale.valueAt(0.2013);
        double steps = v / 0.016;
        assertEquals(Math.rint(steps), steps, 1e-4, "a whole number of steps");
        assertEquals(0.2013, scale.fractionOf(v), 0.003);
    }

    @Test
    void positionsAndValuesAreClamped() {
        NumericScale scale = NumericScale.of(intensity());

        assertEquals(0.0, scale.valueAt(-3.0));
        assertEquals(3.2f, (float) scale.valueAt(9.0));
        assertEquals(0.0, scale.fractionOf(-1.0));
        assertEquals(1.0, scale.fractionOf(99.0));
        assertEquals(0.0, scale.snap(-5.0));
    }

    @Test
    void steppingMovesOneStepAndStopsAtTheEnds() {
        NumericScale scale = NumericScale.of(intensity());

        assertEquals(0.016, scale.stepFrom(0.0, 1), 1e-6);
        assertEquals(0.0, scale.stepFrom(0.016, -1), 1e-6);
        assertEquals(0.0, scale.stepFrom(0.0, -1));
        assertEquals(3.2f, (float) scale.stepFrom(3.2f, 1));
        assertEquals(3.2f, (float) scale.stepFrom(3.2f - 0.016, 50));
        // from the default, one step up is a real step away from it
        assertEquals(1.6 + 0.016, scale.stepFrom(1.6f, 1), 1e-5);
    }

    @Test
    void integerOptionsStepByWholeNumbers() {
        Option<Integer> option = octaves();
        NumericScale scale = NumericScale.of(option);

        assertEquals(3, scale.steps());
        assertEquals(2.0, scale.valueAt(0.0));
        assertEquals(3.0, scale.valueAt(0.34));
        assertEquals(4.0, scale.valueAt(0.66));
        assertEquals(5.0, scale.valueAt(1.0));

        NumericScale.set(option, scale.valueAt(0.0));
        assertEquals(2, option.pending());
        assertEquals(2.0, NumericScale.current(option));
    }

    @Test
    void setWritesAFloatOptionAndRespectsItsRange() {
        Option<Float> option = intensity();

        NumericScale.set(option, 1.0);
        assertEquals(1.0f, option.pending());
        assertTrue(option.isDirty());

        NumericScale.set(option, 99.0);
        assertEquals(3.2f, option.pending(), "the option clamps to its range");
    }

    @Test
    void ticksIncludeBothEnds() {
        NumericScale scale = NumericScale.of(intensity());

        double[] ticks = scale.tickFractions(10);
        assertEquals(11, ticks.length);
        assertEquals(0.0, ticks[0]);
        assertEquals(1.0, ticks[10]);
        assertEquals(0.5, ticks[5], 1e-12);
        assertEquals(2, scale.tickFractions(0).length, "at least one segment");
    }

    @Test
    void onlyNumericOptionsWithARangeHaveAScale() {
        assertThrows(IllegalArgumentException.class,
                () -> NumericScale.of(Option.standalone("effectsEnabled", Boolean.class, true)));
        assertThrows(IllegalArgumentException.class,
                () -> NumericScale.of(Option.standalone("noRange", Float.class, 1f)));
    }

    @Test
    void aZeroWidthRangeIsHarmless() {
        Option<Float> option = Option.standalone("fixed", Float.class, 1f).range(1f, 1f, 0.1f);
        NumericScale scale = NumericScale.of(option);

        assertEquals(0.0, scale.fractionOf(1.0));
        assertEquals(1.0, scale.valueAt(0.7));
    }
}
