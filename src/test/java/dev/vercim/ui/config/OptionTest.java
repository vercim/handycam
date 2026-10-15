package dev.vercim.ui.config;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OptionTest {

    enum Mode { LEAD, LAG }

    /** An external store, like the game's config object. */
    private static final class Store {
        float intensity = 2f;
        boolean enabled = true;
        int octaves = 4;
        Mode mode = Mode.LEAD;
    }

    private static Option<Float> intensity(Store store) {
        return Option.bound("intensity", Float.class, 2f, () -> store.intensity, v -> store.intensity = v)
                .range(0, 4, 0.04);
    }

    @Test
    void pendingStartsAsTheSavedValue() {
        Store store = new Store();
        Option<Float> option = intensity(store);

        assertEquals(2f, option.pending());
        assertFalse(option.isDirty());
    }

    @Test
    void editingLeavesTheStoreAlone() {
        Store store = new Store();
        Option<Float> option = intensity(store);

        option.setPending(3f);

        assertEquals(3f, option.pending());
        assertEquals(2f, option.saved());
        assertEquals(2f, store.intensity);
        assertTrue(option.isDirty());
    }

    @Test
    void editingBackToTheSavedValueIsNotDirty() {
        Option<Float> option = intensity(new Store());
        option.setPending(3f);
        option.setPending(2f);

        assertFalse(option.isDirty());
    }

    @Test
    void numbersAreClampedToTheRange() {
        Option<Float> option = intensity(new Store());

        option.setPending(9f);
        assertEquals(4f, option.pending());
        option.setPending(-3f);
        assertEquals(0f, option.pending());
    }

    @Test
    void nonFiniteNumbersAreIgnored() {
        Option<Float> option = intensity(new Store());
        option.setPending(3f);

        option.setPending(Float.NaN);
        option.setPending(Float.POSITIVE_INFINITY);

        assertEquals(3f, option.pending());
    }

    @Test
    void integerOptionsClampToTheirRange() {
        Store store = new Store();
        Option<Integer> octaves = Option.bound("octaves", Integer.class, 4, () -> store.octaves, v -> store.octaves = v)
                .range(2, 5, 1);

        octaves.setPending(9);
        assertEquals(5, octaves.pending());
        octaves.setPending(0);
        assertEquals(2, octaves.pending());
    }

    @Test
    void discardGoesBackToTheSavedValue() {
        Option<Float> option = intensity(new Store());
        option.setPending(3f);

        option.discard();

        assertEquals(2f, option.pending());
        assertFalse(option.isDirty());
    }

    @Test
    void resetToDefaultEditsTowardTheDefault() {
        Store store = new Store();
        store.intensity = 3.5f;
        Option<Float> option = intensity(store);
        assertFalse(option.isDefault());

        option.resetToDefault();

        assertTrue(option.isDefault());
        assertEquals(2f, option.pending());
        assertEquals(3.5f, option.saved());
        assertTrue(option.isDirty());
    }

    @Test
    void commitWritesTheStore() {
        Store store = new Store();
        Option<Float> option = intensity(store);
        option.setPending(3f);

        assertTrue(option.commit());

        assertEquals(3f, store.intensity);
        assertEquals(3f, option.saved());
        assertFalse(option.isDirty());
        assertFalse(option.commit());
    }

    @Test
    void savedFollowsChangesMadeStraightToTheStore() {
        Store store = new Store();
        Option<Float> option = intensity(store);
        store.intensity = 1f;

        assertEquals(1f, option.saved());
        assertEquals(1f, option.pending());
    }

    @Test
    void listenersHearEffectiveChangesOnly() {
        Option<Float> option = intensity(new Store());
        List<Float> seen = new ArrayList<>();
        option.onChange(o -> seen.add(o.pending()));

        option.setPending(3f);
        option.setPending(3f);
        option.setPending(9f);
        option.discard();
        option.discard();

        assertEquals(List.of(3f, 4f, 2f), seen);
    }

    @Test
    void togglesGateOptionsThatRequireThem() {
        Store store = new Store();
        Option<Boolean> toggle = Option.bound("enabled", Boolean.class, true, () -> store.enabled, v -> store.enabled = v);
        Option<Float> dependent = intensity(store).requires(toggle);
        assertTrue(dependent.enabled());

        toggle.setPending(false);

        assertFalse(dependent.enabled());
        assertTrue(store.enabled);
    }

    @Test
    void enumOptionsKnowTheirValuesAndKeys() {
        Store store = new Store();
        Option<Mode> mode = Option.bound("mode", Mode.class, Mode.LEAD, () -> store.mode, v -> store.mode = v)
                .valueKeyPrefix("cfg.sway_mode");

        assertArrayEquals(Mode.values(), mode.enumConstants());
        assertEquals("cfg.sway_mode.lag", mode.valueKey(Mode.LAG));
        mode.setPending(Mode.LAG);
        assertEquals(Mode.LAG, mode.pending());
        assertEquals(Mode.LEAD, store.mode);
    }

    @Test
    void standaloneOptionsKeepTheirOwnValue() {
        Option<Boolean> option = Option.standalone("flag", Boolean.class, false);
        option.setPending(true);
        assertTrue(option.commit());
        assertTrue(option.saved());
    }

    @Test
    void rejectsUnsupportedTypesAndBadRanges() {
        assertThrows(IllegalArgumentException.class, () -> Option.standalone("x", String.class, "a"));
        assertThrows(IllegalStateException.class, () -> Option.standalone("x", Boolean.class, true).range(0, 1, 0.1));
        assertThrows(IllegalArgumentException.class, () -> Option.standalone("x", Float.class, 1f).range(2, 1, 0.1));
        assertThrows(IllegalArgumentException.class, () -> Option.standalone("x", Float.class, 1f).range(0, 1, 0));
    }
}
