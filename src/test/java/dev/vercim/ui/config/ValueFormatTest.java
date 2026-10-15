package dev.vercim.ui.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValueFormatTest {

    @Test
    void floatsAreAPercentageOfTheirDefault() {
        Option<Float> option = Option.standalone("masterIntensity", Float.class, 1.6f).range(0f, 3.2f, 0.016f);

        assertEquals("100 %", ValueFormat.format(option, 1.6f));
        assertEquals("50 %", ValueFormat.format(option, 0.8f));
        assertEquals("200 %", ValueFormat.format(option, 3.2f));
        assertEquals("0 %", ValueFormat.format(option, 0f));
    }

    @Test
    void theEditedValueIsWhatIsShown() {
        Option<Float> option = Option.standalone("masterIntensity", Float.class, 1.6f).range(0f, 3.2f, 0.016f);
        option.setPending(0.4f);

        assertEquals("25 %", ValueFormat.pending(option));
    }

    @Test
    void integersAreShownAsTheyAre() {
        Option<Integer> option = Option.standalone("noiseOctaves", Integer.class, 4).range(2, 5, 1);

        assertEquals("4", ValueFormat.format(option, 4));
        assertEquals("2", ValueFormat.format(option, 2));
    }

    @Test
    void aFloatWithAZeroDefaultHasNoPercentage() {
        Option<Float> option = Option.standalone("offset", Float.class, 0f).range(-1f, 1f, 0.01f);

        assertEquals("0", ValueFormat.format(option, 0f));
        assertEquals("0.25", ValueFormat.format(option, 0.25f));
        assertEquals("-0.5", ValueFormat.format(option, -0.5f));
        assertEquals("1", ValueFormat.format(option, 1f));
        assertEquals("0", ValueFormat.format(option, -0.001f), "no negative zero");
    }

    @Test
    void togglesAndChoicesAreNotNumbers() {
        assertThrows(IllegalArgumentException.class,
                () -> ValueFormat.format(Option.standalone("effectsEnabled", Boolean.class, true), 1));
    }
}
