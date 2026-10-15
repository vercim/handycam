package dev.vercim.handycam.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.vercim.ui.config.ConfigModel;
import dev.vercim.ui.config.Option;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

class HandycamOptionsTest {

    private final HandycamConfig store = new HandycamConfig();
    private final ConfigModel model = HandycamOptions.create(store);

    private static List<String> configFields() {
        List<String> names = new ArrayList<>();
        for (Field f : HandycamConfig.class.getFields()) {
            if (!Modifier.isStatic(f.getModifiers()) && !f.getName().equals("configVersion")) names.add(f.getName());
        }
        return names;
    }

    @Test
    void everyConfigFieldIsAnOptionAndViceVersa() {
        Set<String> fields = new TreeSet<>(configFields());
        Set<String> options = new TreeSet<>();
        model.options().forEach(o -> options.add(o.id()));

        assertEquals(fields, options);
        assertEquals(66, options.size());
    }

    @Test
    void defaultsMatchAFreshConfigAndSavedValuesReadTheStore() throws ReflectiveOperationException {
        HandycamConfig fresh = new HandycamConfig();
        for (Option<?> option : model.options()) {
            Object expected = HandycamConfig.class.getField(option.id()).get(fresh);
            assertEquals(expected, option.defaultValue(), option.id());
            assertEquals(expected, option.saved(), option.id());
            assertTrue(option.isDefault(), option.id());
        }
    }

    @Test
    void shownNumbersHaveSaneRangesContainingTheDefault() {
        int ranged = 0;
        for (ConfigModel.Category category : model.visibleCategories()) {
            for (Option<?> option : category.options()) {
                if (option.type() != Float.class && option.type() != Integer.class) continue;
                assertTrue(option.hasRange(), option.id() + " has no range");
                ranged++;
                double value = ((Number) option.defaultValue()).doubleValue();
                assertTrue(option.min() <= value && value <= option.max(), option.id() + " default outside its range");
                assertTrue(option.step() > 0 && option.step() <= option.max() - option.min(), option.id());
            }
        }
        assertEquals(38, ranged);
    }

    @Test
    void sliderRangesFollowTheOldScreen() {
        // Percent of the default, as the Cloth sliders had it.
        assertRange("masterIntensity", 0, 3.2, 0.016);
        assertRange("forwardTiltDecay", 0.25, 3, 0.01);
        assertRange("idleFrequency", 0.125, 1, 0.005);
        assertRange("walkBobVerticalMult", 1, 4, 0.02);
        assertRange("explosionMaxDistance", 2, 40, 0.2);
        assertRange("bowConcentration", 0, 1, 0.009);
        assertRange("noiseOctaves", 2, 5, 1);
    }

    private void assertRange(String id, double min, double max, double step) {
        Option<?> option = model.option(id);
        assertEquals(min, option.min(), 1e-6, id + " min");
        assertEquals(max, option.max(), 1e-6, id + " max");
        assertEquals(step, option.step(), 1e-6, id + " step");
    }

    @Test
    void landingLimitsAreStoredButHidden() {
        List<String> hidden = model.categories().stream().filter(ConfigModel.Category::isHidden)
                .flatMap(c -> c.options().stream()).map(Option::id).toList();

        assertEquals(List.of("landingPitchMax", "landingRollMax", "landingYawMax"), hidden);
        assertEquals(10, model.visibleCategories().size());
    }

    @Test
    void dependentOptionsRequireToggles() {
        Option<?> shrink = model.option("bowCrosshairShrink");
        assertEquals(List.of("bowEnabled", "bowCrosshairShrinkEnabled"),
                shrink.requirements().stream().map(Option::id).toList());
        assertTrue(model.option("effectsEnabled").requirements().isEmpty());

        model.option("bowEnabled", Boolean.class).setPending(false);
        assertFalse(model.option("bowDrawTiltEnabled").enabled());
        assertFalse(shrink.enabled());
        assertTrue(model.option("forwardTiltIntensity").enabled());
    }

    @Test
    void editingDoesNotTouchTheStoreUntilApplied() {
        model.option("masterIntensity", Float.class).setPending(2.5f);
        model.option("cameraSwayMode", HandycamConfig.SwayMode.class).setPending(HandycamConfig.SwayMode.LAG);

        assertEquals(1.6f, store.masterIntensity);
        assertEquals(HandycamConfig.SwayMode.LEAD, store.cameraSwayMode);

        model.applyPending();

        assertEquals(2.5f, store.masterIntensity);
        assertEquals(HandycamConfig.SwayMode.LAG, store.cameraSwayMode);
    }

    // ---- translations

    private static JsonObject lang(String name) throws IOException {
        Path file = Path.of(System.getProperty("handycam.rootDir"))
                .resolve("src/main/resources/assets/handycam/lang").resolve(name + ".json");
        return JsonParser.parseString(Files.readString(file)).getAsJsonObject();
    }

    @Test
    void everyShownOptionAndCategoryHasEnglishAndRussianText() throws IOException {
        for (String language : List.of("en_us", "ru_ru")) {
            JsonObject lang = lang(language);
            List<String> missing = new ArrayList<>();
            for (ConfigModel.Category category : model.visibleCategories()) {
                if (!lang.has(category.nameKey())) missing.add(category.nameKey());
                for (Option<?> option : category.options()) {
                    if (!lang.has(option.nameKey())) missing.add(option.nameKey());
                    if (!lang.has(option.descriptionKey())) missing.add(option.descriptionKey());
                    if (option.type().isEnum()) {
                        for (Object value : option.enumConstants()) {
                            @SuppressWarnings({"unchecked", "rawtypes"})
                            String key = ((Option) option).valueKey(value);
                            if (!lang.has(key)) missing.add(key);
                        }
                    }
                }
            }
            assertEquals(List.of(), missing, language);
        }
    }

    @Test
    void screenTitleIsTranslated() throws IOException {
        assertTrue(lang("en_us").has("handycam.config.title"));
        assertTrue(lang("ru_ru").has("handycam.config.title"));
    }
}
