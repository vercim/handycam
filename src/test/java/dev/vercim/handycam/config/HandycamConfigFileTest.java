package dev.vercim.handycam.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * handycam.json keeps its name, keys and migration. The oracle below is the way the file was read and written
 * before the config model took over: Gson straight onto the config object.
 */
class HandycamConfigFileTest {

    private static final Gson OLD_GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(HandycamConfig.class, (InstanceCreator<HandycamConfig>) t -> new HandycamConfig())
            .create();

    private static HandycamConfig oldLoad(String json) {
        HandycamConfig config = OLD_GSON.fromJson(json, HandycamConfig.class);
        config.migrate();
        return config;
    }

    private static HandycamConfig newLoad(Path dir, String json) throws IOException {
        Files.writeString(dir.resolve("handycam.json"), json);
        HandycamConfig.load(dir);
        return HandycamConfig.get();
    }

    private static void assertSameConfig(HandycamConfig expected, HandycamConfig actual) throws IllegalAccessException {
        for (Field f : HandycamConfig.class.getFields()) {
            if (Modifier.isStatic(f.getModifiers())) continue;
            assertEquals(f.get(expected), f.get(actual), f.getName());
        }
    }

    private static Map<String, String> flat(String json) {
        Map<String, String> map = new TreeMap<>();
        for (Map.Entry<String, JsonElement> e : JsonParser.parseString(json).getAsJsonObject().entrySet()) {
            map.put(e.getKey(), e.getValue().getAsString());
        }
        return map;
    }

    private static HandycamConfig customised() {
        HandycamConfig c = new HandycamConfig();
        c.masterIntensity = 2.3f;
        c.noiseOctaves = 2;
        c.walkBobEnabled = true;
        c.walkBobIntensity = 3.1f;
        c.cameraSwayMode = HandycamConfig.SwayMode.LAG;
        c.eatSwayDirection = HandycamConfig.EatSwayDirection.RIGHT;
        c.bowCrosshairShrinkEnabled = true;
        c.landingPitchMax = 12.5f;
        c.landingRollMax = 4.25f;
        c.explosionMaxDistance = 33f;
        c.effectsEnabled = false;
        c.configVersion = 4;
        return c;
    }

    @Test
    void aFileWrittenTheOldWayLoadsIdentically(@TempDir Path tmp) throws Exception {
        String old = OLD_GSON.toJson(customised());

        assertSameConfig(oldLoad(old), newLoad(tmp, old));
    }

    @Test
    void theDefaultFileLoadsIdentically(@TempDir Path tmp) throws Exception {
        String old = OLD_GSON.toJson(new HandycamConfig());

        assertSameConfig(oldLoad(old), newLoad(tmp, old));
    }

    @Test
    void savedFilesHaveTheSameKeysAndValuesAsBefore(@TempDir Path tmp) throws Exception {
        for (HandycamConfig config : List.of(new HandycamConfig(), customised())) {
            String old = OLD_GSON.toJson(config);
            newLoad(tmp, old);

            String saved = Files.readString(tmp.resolve("handycam.json"));

            // The old code loaded, migrated and wrote the object back with Gson.
            assertEquals(flat(OLD_GSON.toJson(oldLoad(old))), flat(saved));
        }
    }

    @Test
    void migrationsStillRunFromEveryOlderVersion(@TempDir Path tmp) throws Exception {
        List<String> files = List.of(
                // no configVersion at all (the earliest files)
                "{\"masterIntensity\": 2.0, \"strafeTiltIntensity\": 3.0, \"forwardTiltIntensity\": 3.0,"
                        + " \"turnSway\": 0.08, \"maxTurnRoll\": 2.5}",
                "{\"configVersion\": 1, \"masterIntensity\": 2.0, \"turnSway\": 0.08, \"maxTurnRoll\": 2.5}",
                "{\"configVersion\": 2, \"masterIntensity\": 2.0, \"forwardTiltIntensity\": 3.0}",
                "{\"configVersion\": 3, \"masterIntensity\": 2.0, \"jumpIntensity\": 1.25}",
                "{\"configVersion\": 3, \"masterIntensity\": 1.9}");

        for (String json : files) {
            HandycamConfig expected = oldLoad(json);
            HandycamConfig actual = newLoad(tmp, json);

            assertSameConfig(expected, actual);
            assertEquals(4, actual.configVersion);
        }
        assertEquals(1.6f, oldLoad(files.get(0)).masterIntensity);
    }

    @Test
    void theMigratedVersionIsWrittenBack(@TempDir Path tmp) throws Exception {
        newLoad(tmp, "{\"masterIntensity\": 2.0}");

        JsonObject saved = JsonParser.parseString(Files.readString(tmp.resolve("handycam.json"))).getAsJsonObject();

        assertEquals(4, saved.get("configVersion").getAsInt());
        assertEquals(1.6f, saved.get("masterIntensity").getAsFloat());
    }

    @Test
    void aFirstRunCreatesTheFileWithEveryKey(@TempDir Path tmp) throws Exception {
        HandycamConfig.load(tmp);

        String saved = Files.readString(tmp.resolve("handycam.json"));

        assertEquals(flat(OLD_GSON.toJson(oldLoad("{}"))), flat(saved));
        assertSameConfig(new HandycamConfig() {{ configVersion = 4; }}, HandycamConfig.get());
    }

    @Test
    void keysThisVersionDoesNotKnowSurviveASave(@TempDir Path tmp) throws Exception {
        newLoad(tmp, "{\"configVersion\": 4, \"fromTheFuture\": [1, 2, 3], \"masterIntensity\": 1.1}");
        HandycamConfig.get().masterIntensity = 1.2f;
        HandycamConfig.save(tmp);

        JsonObject saved = JsonParser.parseString(Files.readString(tmp.resolve("handycam.json"))).getAsJsonObject();

        assertEquals("[1,2,3]", saved.get("fromTheFuture").toString());
        assertEquals(1.2f, saved.get("masterIntensity").getAsFloat());
    }

    @Test
    void anUnreadableFileFallsBackToDefaultsAndIsKept(@TempDir Path tmp) throws Exception {
        for (String broken : List.of("{ masterIntensity: ", "", "[1, 2]", "null")) {
            Files.deleteIfExists(tmp.resolve("handycam.json.bak"));

            HandycamConfig config = newLoad(tmp, broken);

            assertSameConfig(new HandycamConfig() {{ configVersion = 4; }}, config);
            assertEquals(broken, Files.readString(tmp.resolve("handycam.json.bak")), "backup of: " + broken);
            assertEquals(4, JsonParser.parseString(Files.readString(tmp.resolve("handycam.json")))
                    .getAsJsonObject().get("configVersion").getAsInt());
        }
    }

    @Test
    void valuesOfTheWrongTypeKeepTheirDefault(@TempDir Path tmp) throws Exception {
        HandycamConfig config = newLoad(tmp, "{\"configVersion\": 4, \"masterIntensity\": \"loud\","
                + " \"noiseOctaves\": 3.5, \"cameraSwayMode\": \"SIDEWAYS\", \"jumpIntensity\": 2.0,"
                + " \"effectsEnabled\": 7}");

        assertEquals(1.6f, config.masterIntensity);
        assertEquals(4, config.noiseOctaves);
        assertEquals(HandycamConfig.SwayMode.LEAD, config.cameraSwayMode);
        assertEquals(2.0f, config.jumpIntensity);
        assertTrue(config.effectsEnabled);
    }

    @Test
    void hiddenLandingLimitsAreReadFromTheFile(@TempDir Path tmp) throws Exception {
        HandycamConfig config = newLoad(tmp, "{\"configVersion\": 4, \"landingPitchMax\": 12.5, \"landingYawMax\": 1.0}");

        assertEquals(12.5f, config.landingPitchMax);
        assertEquals(1.0f, config.landingYawMax);
        assertEquals(3.5f, config.landingRollMax);
    }

    @Test
    void theFileHasNoLeftoverTempFile(@TempDir Path tmp) throws Exception {
        newLoad(tmp, "{}");

        List<String> names = new ArrayList<>();
        try (var stream = Files.list(tmp)) {
            stream.forEach(p -> names.add(p.getFileName().toString()));
        }
        assertEquals(List.of("handycam.json"), names);
    }
}
