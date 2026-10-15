package dev.vercim.ui.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConfigModelTest {

    enum Mode { LEAD, LAG }

    private static final class Store {
        boolean enabled = true;
        float intensity = 2f;
        int octaves = 4;
        Mode mode = Mode.LEAD;
        float hiddenValue = 7f;
    }

    private final Store store = new Store();
    private final ConfigModel model = new ConfigModel("test.config");
    private final Option<Boolean> enabled;
    private final Option<Float> intensity;
    private final Option<Integer> octaves;
    private final Option<Mode> mode;
    private final Option<Float> hidden;

    ConfigModelTest() {
        ConfigModel.Category general = model.category("general");
        enabled = general.add(Option.bound("effectsEnabled", Boolean.class, true,
                () -> store.enabled, v -> store.enabled = v));
        intensity = general.add(Option.bound("masterIntensity", Float.class, 2f,
                () -> store.intensity, v -> store.intensity = v).range(0, 4, 0.02).name("test.config.global_intensity"));
        octaves = general.add(Option.bound("noiseOctaves", Integer.class, 4,
                () -> store.octaves, v -> store.octaves = v).range(2, 5, 1));
        mode = model.category("motion").add(Option.bound("swayMode", Mode.class, Mode.LEAD,
                () -> store.mode, v -> store.mode = v).valueKeyPrefix("test.config.sway_mode"));
        hidden = model.category("advanced").hidden().add(Option.bound("landingPitchMax", Float.class, 7f,
                () -> store.hiddenValue, v -> store.hiddenValue = v));
    }

    @Test
    void keepsCategoriesAndOptionsInOrder() {
        assertEquals(List.of("general", "motion", "advanced"), model.categories().stream().map(ConfigModel.Category::id).toList());
        assertEquals(List.of("general", "motion"), model.visibleCategories().stream().map(ConfigModel.Category::id).toList());
        assertEquals(List.of("effectsEnabled", "masterIntensity", "noiseOctaves", "swayMode", "landingPitchMax"),
                model.options().stream().map(Option::id).toList());
        assertSame(intensity, model.option("masterIntensity"));
        assertNull(model.option("missing"));
    }

    @Test
    void derivesTranslationKeysUnlessGiven() {
        assertEquals("test.config.effects_enabled", enabled.nameKey());
        assertEquals("test.config.effects_enabled.tooltip", enabled.descriptionKey());
        assertEquals("test.config.global_intensity", intensity.nameKey());
        assertEquals("test.config.global_intensity.tooltip", intensity.descriptionKey());
        assertEquals("test.config.category.general", model.category("general").nameKey());
    }

    @Test
    void rejectsDuplicateIdsAndWrongTypedLookups() {
        assertThrows(IllegalArgumentException.class,
                () -> model.category("other").add(Option.standalone("effectsEnabled", Boolean.class, true)));
        assertThrows(IllegalArgumentException.class, () -> model.option("effectsEnabled", Float.class));
        assertSame(enabled, model.option("effectsEnabled", Boolean.class));
    }

    @Test
    void applyPendingWritesOnlyEditedOptions() {
        intensity.setPending(3f);
        enabled.setPending(false);
        assertTrue(model.hasPending());
        assertEquals(List.of(enabled, intensity), model.dirtyOptions());

        assertTrue(model.applyPending());

        assertEquals(3f, store.intensity);
        assertFalse(store.enabled);
        assertEquals(4, store.octaves);
        assertFalse(model.hasPending());
        assertFalse(model.applyPending());
    }

    @Test
    void discardPendingLeavesTheStores() {
        intensity.setPending(3f);
        mode.setPending(Mode.LAG);

        model.discardPending();

        assertEquals(2f, store.intensity);
        assertEquals(Mode.LEAD, store.mode);
        assertEquals(2f, intensity.pending());
        assertFalse(model.hasPending());
    }

    @Test
    void resetToDefaultIsAnEditNotASave() {
        store.intensity = 3.5f;

        model.resetToDefault(intensity);

        assertEquals(2f, intensity.pending());
        assertEquals(3.5f, store.intensity);
        assertTrue(model.hasPending());
        model.applyPending();
        assertEquals(2f, store.intensity);
    }

    @Test
    void resetAllToDefaultEditsEverythingBack() {
        store.intensity = 3.5f;
        store.octaves = 2;
        store.mode = Mode.LAG;

        model.resetAllToDefault();

        assertEquals(List.of(intensity, octaves, mode), model.dirtyOptions());
        model.applyPending();
        assertEquals(2f, store.intensity);
        assertEquals(4, store.octaves);
        assertEquals(Mode.LEAD, store.mode);
    }

    @Test
    void modelListenersHearEdits() {
        List<String> seen = new ArrayList<>();
        model.addChangeListener(o -> seen.add(o.id()));

        intensity.setPending(3f);
        mode.setPending(Mode.LAG);
        intensity.setPending(3f);

        assertEquals(List.of("masterIntensity", "swayMode"), seen);
    }

    @Test
    void writesEveryOptionAndTheExtrasToJson() {
        model.extras().addProperty("configVersion", 4);
        store.intensity = 1.6f;
        store.mode = Mode.LAG;

        JsonObject json = model.toJson();

        assertEquals(List.of("configVersion", "effectsEnabled", "masterIntensity", "noiseOctaves", "swayMode", "landingPitchMax"),
                List.copyOf(json.keySet()));
        assertEquals("1.6", json.get("masterIntensity").toString());
        assertEquals("\"LAG\"", json.get("swayMode").toString());
        assertEquals("true", json.get("effectsEnabled").toString());
        assertEquals("4", json.get("noiseOctaves").toString());
    }

    @Test
    void loadsValuesAndKeepsUnknownKeys() {
        model.loadJson(JsonParser.parseString("""
                {"configVersion": 3, "masterIntensity": 1.25, "swayMode": "LAG", "noiseOctaves": 5,
                 "effectsEnabled": false, "futureOption": {"a": 1}}""").getAsJsonObject());

        assertEquals(1.25f, store.intensity);
        assertEquals(Mode.LAG, store.mode);
        assertEquals(5, store.octaves);
        assertFalse(store.enabled);
        assertEquals(3, model.extras().get("configVersion").getAsInt());
        assertTrue(model.extras().has("futureOption"));
        assertTrue(model.toJson().has("futureOption"));
    }

    @Test
    void missingKeysKeepTheCurrentValue() {
        store.intensity = 3f;
        model.loadJson(JsonParser.parseString("{\"noiseOctaves\": 3}").getAsJsonObject());

        assertEquals(3f, store.intensity);
        assertEquals(3, store.octaves);
    }

    @Test
    void badValuesAreSkippedNotFatal() {
        model.loadJson(JsonParser.parseString("""
                {"masterIntensity": "fast", "noiseOctaves": 3.5, "swayMode": "SIDEWAYS", "effectsEnabled": 1,
                 "landingPitchMax": null}""").getAsJsonObject());

        assertEquals(2f, store.intensity);
        assertEquals(4, store.octaves);
        assertEquals(Mode.LEAD, store.mode);
        assertTrue(store.enabled);
        assertEquals(7f, store.hiddenValue);
    }

    @Test
    void acceptsTolerantSpellingsAndKeepsOutOfRangeNumbers() {
        model.loadJson(JsonParser.parseString("""
                {"masterIntensity": "2.5", "swayMode": "lag", "effectsEnabled": "false", "noiseOctaves": 9.0}""").getAsJsonObject());

        assertEquals(2.5f, store.intensity);
        assertEquals(Mode.LAG, store.mode);
        assertFalse(store.enabled);
        assertEquals(9, store.octaves);
    }

    @Test
    void loadingDropsAPendingEdit() {
        intensity.setPending(3.3f);

        model.loadJson(JsonParser.parseString("{\"masterIntensity\": 1.0}").getAsJsonObject());

        assertEquals(1f, intensity.pending());
        assertFalse(intensity.isDirty());
    }

    @Test
    void savesAndLoadsAFile(@TempDir Path tmp) throws IOException {
        Path file = tmp.resolve("nested/config.json");
        store.intensity = 1.6f;
        store.mode = Mode.LAG;
        model.extras().addProperty("configVersion", 4);
        model.save(file);

        ConfigModelTest second = new ConfigModelTest();
        assertTrue(second.model.load(file));

        assertEquals(1.6f, second.store.intensity);
        assertEquals(Mode.LAG, second.store.mode);
        assertEquals(4, second.model.extras().get("configVersion").getAsInt());
        assertEquals(model.toJson(), second.model.toJson());
        assertFalse(Files.exists(tmp.resolve("nested/config.json.tmp")));
    }

    @Test
    void savingReplacesAnExistingFile(@TempDir Path tmp) throws IOException {
        Path file = tmp.resolve("config.json");
        Files.writeString(file, "old");

        model.save(file);

        assertTrue(Files.readString(file).contains("masterIntensity"));
    }

    @Test
    void loadReportsAMissingFileAsFalse(@TempDir Path tmp) throws IOException {
        assertFalse(model.load(tmp.resolve("none.json")));
    }

    @Test
    void loadRejectsGarbageWithAnIOException(@TempDir Path tmp) throws IOException {
        Path broken = tmp.resolve("broken.json");
        Files.writeString(broken, "{ not json");
        assertThrows(IOException.class, () -> model.load(broken));

        Path array = tmp.resolve("array.json");
        Files.writeString(array, "[1, 2]");
        assertThrows(IOException.class, () -> model.load(array));

        Path empty = tmp.resolve("empty.json");
        Files.writeString(empty, "");
        assertThrows(IOException.class, () -> model.load(empty));
    }
}
