package dev.vercim.handycam.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The settings screen is built from translation keys written in its source. Nothing at build time connects a key in
 * the code to the lang file, and a missing key shows as the raw key in game, so this does.
 */
class ScreenTextTest {

    private static final Pattern KEY = Pattern.compile("\"(handycam\\.config\\.[a-z_.]+)\"");

    private static Path root() {
        return Path.of(System.getProperty("handycam.rootDir"));
    }

    private static JsonObject lang(String name) throws IOException {
        Path file = root().resolve("src/main/resources/assets/handycam/lang").resolve(name + ".json");
        return JsonParser.parseString(Files.readString(file)).getAsJsonObject();
    }

    private static List<Path> screenSources() throws IOException {
        List<Path> sources = new ArrayList<>();
        sources.add(root().resolve("src/main/java/dev/vercim/handycam/config/HandycamConfigScreen.java"));
        try (Stream<Path> widgets = Files.walk(root().resolve("src/main/java/dev/vercim/ui/widget"))) {
            widgets.filter(p -> p.toString().endsWith(".java")).forEach(sources::add);
        }
        return sources;
    }

    private static int placeholders(String text) {
        Matcher m = Pattern.compile("%s").matcher(text);
        int count = 0;
        while (m.find()) count++;
        return count;
    }

    @Test
    void everyKeyTheScreenUsesIsTranslatedInBothLanguages() throws IOException {
        TreeSet<String> used = new TreeSet<>();
        for (Path source : screenSources()) {
            Matcher m = KEY.matcher(Files.readString(source));
            while (m.find()) used.add(m.group(1));
        }
        assertFalse(used.isEmpty(), "the scan found no keys at all");

        for (String language : List.of("en_us", "ru_ru")) {
            JsonObject lang = lang(language);
            List<String> missing = used.stream().filter(key -> !lang.has(key)).toList();
            assertEquals(List.of(), missing, language);
        }
    }

    @Test
    void bothLanguageFilesHaveTheSameKeys() throws IOException {
        TreeSet<String> english = new TreeSet<>(lang("en_us").keySet());
        TreeSet<String> russian = new TreeSet<>(lang("ru_ru").keySet());

        TreeSet<String> onlyEnglish = new TreeSet<>(english);
        onlyEnglish.removeAll(russian);
        TreeSet<String> onlyRussian = new TreeSet<>(russian);
        onlyRussian.removeAll(english);

        assertEquals(new TreeSet<String>(), onlyEnglish, "keys missing from ru_ru");
        assertEquals(new TreeSet<String>(), onlyRussian, "keys missing from en_us");
    }

    @Test
    void translationsTakeTheSameNumberOfArguments() throws IOException {
        JsonObject english = lang("en_us");
        JsonObject russian = lang("ru_ru");

        List<String> mismatched = new ArrayList<>();
        for (String key : english.keySet()) {
            if (!russian.has(key)) continue;
            int en = placeholders(english.get(key).getAsString());
            int ru = placeholders(russian.get(key).getAsString());
            if (en != ru) mismatched.add(key + " en=" + en + " ru=" + ru);
        }
        assertEquals(List.of(), mismatched);
    }

    @Test
    void theFormattedTextsHaveTheArgumentsTheScreenPasses() throws IOException {
        // hint: up, left, down, right, jump, sprint; the panel lines: one value each
        JsonObject english = lang("en_us");
        assertEquals(6, placeholders(english.get("handycam.config.preview.hint").getAsString()));
        assertEquals(1, placeholders(english.get("handycam.config.panel.default").getAsString()));
        assertEquals(1, placeholders(english.get("handycam.config.panel.requires").getAsString()));
    }
}
