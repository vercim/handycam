// SPDX-License-Identifier: LGPL-3.0-only
package dev.vercim.ui.config;

import java.util.Locale;

/**
 * How a numeric option's value is shown. A float is a percentage of its default ("100 %" is the default), the way the
 * old slider screen showed it, because the raw numbers (0.016, 4.8) mean nothing to a player; an integer is shown as
 * it is. A float whose default is zero has no percentage and is shown as a plain number.
 */
public final class ValueFormat {

    private ValueFormat() {}

    /** The edited value of a numeric option. */
    public static String pending(Option<?> option) {
        return format(option, (Number) option.pending());
    }

    public static String format(Option<?> option, Number value) {
        if (option.type() == Integer.class) return Integer.toString(value.intValue());
        if (option.type() != Float.class) throw new IllegalArgumentException(option.id() + " is not numeric");

        double def = ((Number) option.defaultValue()).doubleValue();
        if (Math.abs(def) > 1e-6) {
            return Math.round(value.doubleValue() / def * 100.0) + " %";
        }
        return plain(value.doubleValue());
    }

    /** Up to two decimals, no trailing zeros. */
    static String plain(double value) {
        String text = String.format(Locale.ROOT, "%.2f", value);
        if (text.indexOf('.') >= 0) {
            text = text.replaceAll("0+$", "").replaceAll("\\.$", "");
        }
        return text.equals("-0") ? "0" : text;
    }
}
