// SPDX-License-Identifier: LGPL-3.0-only
package dev.vercim.ui.config;

/**
 * Maps a numeric {@link Option} onto a 0 to 1 scale, the way a slider needs it: position to value with the option's
 * step, and value to position. The default value is a snap point of its own, because with a step such as 1 % of a
 * float default the grid does not always hit the default exactly, and dragging a slider onto "100 %" must give the
 * default back so that "is this the default" stays true.
 */
public final class NumericScale {

    private final double min;
    private final double max;
    private final double step;
    private final double defaultValue;

    private NumericScale(Option<?> option) {
        this.min = option.min();
        this.max = option.max();
        this.step = option.step();
        this.defaultValue = ((Number) option.defaultValue()).doubleValue();
    }

    /** The scale of an integer or float option that has a range. */
    public static NumericScale of(Option<?> option) {
        boolean numeric = option.type() == Integer.class || option.type() == Float.class;
        if (!numeric || !option.hasRange()) {
            throw new IllegalArgumentException(option.id() + " is not a numeric option with a range");
        }
        return new NumericScale(option);
    }

    public double min() { return min; }
    public double max() { return max; }
    public double step() { return step; }
    public double defaultValue() { return defaultValue; }

    /** Number of steps from the minimum to the maximum. */
    public int steps() {
        return (int) Math.max(1L, Math.round((max - min) / step));
    }

    /** Position of a value, 0 at the minimum and 1 at the maximum. */
    public double fractionOf(double value) {
        double span = max - min;
        if (!(span > 0.0)) return 0.0;
        return clamp((value - min) / span, 0.0, 1.0);
    }

    public double defaultFraction() {
        return fractionOf(defaultValue);
    }

    /** The value at a position, on the step grid; the ends and the default are exact. */
    public double valueAt(double fraction) {
        return valueAt(fraction, 0.0);
    }

    /**
     * The value at a position, with the default as a magnet: a position within {@code magnet} (a fraction of the whole
     * scale) of the default's gives the default. A slider only a hundred pixels tall has fewer positions than steps,
     * so without a magnet the default could fall between two pixels and be unreachable with the mouse.
     */
    public double valueAt(double fraction, double magnet) {
        double f = clamp(fraction, 0.0, 1.0);
        if (defaultInRange() && Math.abs(f - defaultFraction()) <= magnet) return defaultValue;
        return snap(min + f * (max - min));
    }

    /** The nearest allowed value: on the step grid, inside the range, the default when it is within half a step. */
    public double snap(double raw) {
        double v = clamp(raw, min, max);
        if (defaultInRange() && Math.abs(v - defaultValue) <= step / 2.0) return defaultValue;
        if (max - v <= step / 2.0) return max;
        double snapped = min + Math.rint((v - min) / step) * step;
        return clamp(snapped, min, max);
    }

    /** The value {@code steps} steps above (negative: below) {@code current}. */
    public double stepFrom(double current, int steps) {
        return snap(current + steps * step);
    }

    /** Positions of {@code segments + 1} evenly spaced marks, both ends included. */
    public double[] tickFractions(int segments) {
        int n = Math.max(1, segments);
        double[] ticks = new double[n + 1];
        for (int i = 0; i <= n; i++) ticks[i] = (double) i / n;
        return ticks;
    }

    // ---- reading and writing the option

    /** The edited value of a numeric option as a double. */
    public static double current(Option<?> option) {
        return ((Number) option.pending()).doubleValue();
    }

    /** Edits a numeric option to {@code value}, converted to the option's own type. */
    @SuppressWarnings("unchecked")
    public static void set(Option<?> option, double value) {
        if (option.type() == Integer.class) {
            ((Option<Integer>) option).setPending((int) Math.round(value));
        } else {
            ((Option<Float>) option).setPending((float) value);
        }
    }

    private boolean defaultInRange() {
        return defaultValue >= min && defaultValue <= max;
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
