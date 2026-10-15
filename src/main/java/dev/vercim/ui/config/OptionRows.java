// SPDX-License-Identifier: LGPL-3.0-only
package dev.vercim.ui.config;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link ConfigModel} laid out as the rows of a list: a header for each shown category followed by its options.
 * Hidden categories and categories without options do not appear.
 */
public final class OptionRows {

    public sealed interface Row permits Header, Entry {}

    /** A category's title. */
    public record Header(ConfigModel.Category category) implements Row {}

    /** One option, with the category it belongs to. */
    public record Entry(Option<?> option, ConfigModel.Category category) implements Row {}

    private OptionRows() {}

    public static List<Row> of(ConfigModel model) {
        List<Row> rows = new ArrayList<>();
        for (ConfigModel.Category category : model.visibleCategories()) {
            if (category.options().isEmpty()) continue;
            rows.add(new Header(category));
            for (Option<?> option : category.options()) rows.add(new Entry(option, category));
        }
        return List.copyOf(rows);
    }

    /** The options of the rows, in order. */
    public static List<Option<?>> options(List<Row> rows) {
        List<Option<?>> options = new ArrayList<>();
        for (Row row : rows) {
            if (row instanceof Entry entry) options.add(entry.option());
        }
        return options;
    }
}
