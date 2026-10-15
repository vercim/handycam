// SPDX-License-Identifier: LGPL-3.0-only
package dev.vercim.ui.sim;

import java.util.function.DoubleUnaryOperator;

/** Easing curves on {@code t} in [0, 1]; input outside that range is clamped. All map 0 to 0 and 1 to 1. */
public enum Easing {
    LINEAR(t -> t),
    IN_QUAD(t -> t * t),
    OUT_QUAD(t -> 1 - (1 - t) * (1 - t)),
    IN_OUT_QUAD(t -> t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2),
    IN_CUBIC(t -> t * t * t),
    OUT_CUBIC(t -> 1 - Math.pow(1 - t, 3)),
    IN_OUT_CUBIC(t -> t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2),
    IN_SINE(t -> 1 - Math.cos(t * Math.PI / 2)),
    OUT_SINE(t -> Math.sin(t * Math.PI / 2)),
    IN_OUT_SINE(t -> -(Math.cos(Math.PI * t) - 1) / 2),
    IN_EXPO(t -> t == 0 ? 0 : Math.pow(2, 10 * t - 10)),
    OUT_EXPO(t -> t == 1 ? 1 : 1 - Math.pow(2, -10 * t)),
    /** Overshoots 1 slightly before settling. */
    OUT_BACK(t -> {
        double c1 = 1.70158;
        double c3 = c1 + 1;
        return 1 + c3 * Math.pow(t - 1, 3) + c1 * Math.pow(t - 1, 2);
    });

    private final DoubleUnaryOperator curve;

    Easing(DoubleUnaryOperator curve) {
        this.curve = curve;
    }

    public float apply(float t) {
        if (!(t > 0f)) return 0f;
        if (t >= 1f) return 1f;
        return (float) curve.applyAsDouble(t);
    }

    public float lerp(float from, float to, float t) {
        return from + (to - from) * apply(t);
    }
}
