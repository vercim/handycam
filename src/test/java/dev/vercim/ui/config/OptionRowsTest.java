package dev.vercim.ui.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OptionRowsTest {

    private static Option<Boolean> toggle(String id) {
        return Option.standalone(id, Boolean.class, true);
    }

    @Test
    void eachShownCategoryGetsAHeaderThenItsOptionsInOrder() {
        ConfigModel model = new ConfigModel("test.config");
        ConfigModel.Category general = model.category("general");
        Option<Boolean> a = general.add(toggle("a"));
        Option<Boolean> b = general.add(toggle("b"));
        ConfigModel.Category motion = model.category("motion");
        Option<Boolean> c = motion.add(toggle("c"));

        List<OptionRows.Row> rows = OptionRows.of(model);

        assertEquals(List.of(
                new OptionRows.Header(general),
                new OptionRows.Entry(a, general),
                new OptionRows.Entry(b, general),
                new OptionRows.Header(motion),
                new OptionRows.Entry(c, motion)), rows);
    }

    @Test
    void hiddenAndEmptyCategoriesDoNotAppear() {
        ConfigModel model = new ConfigModel("test.config");
        model.category("empty");
        model.category("advanced").hidden().add(toggle("secret"));
        Option<Boolean> shown = model.category("general").add(toggle("shown"));

        List<OptionRows.Row> rows = OptionRows.of(model);

        assertEquals(2, rows.size());
        assertEquals(List.of(shown), OptionRows.options(rows));
    }

    @Test
    void anEmptyModelHasNoRows() {
        assertEquals(List.of(), OptionRows.of(new ConfigModel("test.config")));
    }
}
