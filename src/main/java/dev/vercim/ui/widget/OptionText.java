// SPDX-License-Identifier: LGPL-3.0-only
package dev.vercim.ui.widget;

import dev.vercim.ui.config.ConfigModel;
import dev.vercim.ui.config.Option;
import dev.vercim.ui.config.ValueFormat;
import net.minecraft.network.chat.Component;

/** The texts of an option: all of them come from translation keys, except numbers. */
public final class OptionText {

    private OptionText() {}

    public static Component name(Option<?> option) {
        return Component.translatable(option.nameKey());
    }

    public static Component description(Option<?> option) {
        return Component.translatable(option.descriptionKey());
    }

    public static Component category(ConfigModel.Category category) {
        return Component.translatable(category.nameKey());
    }

    /** The value being edited. */
    public static Component value(Option<?> option) {
        return value(option, option.pending());
    }

    public static Component defaultValue(Option<?> option) {
        return value(option, option.defaultValue());
    }

    /** A value of this option as text: On/Off, the translated name of a choice, or the number. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Component value(Option<?> option, Object value) {
        if (value instanceof Boolean on) return Component.translatable(on ? "options.on" : "options.off");
        if (value instanceof Number number) return Component.literal(ValueFormat.format(option, number));
        return Component.translatable(((Option) option).valueKey(value));
    }
}
