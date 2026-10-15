package dev.vercim;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Keeps the reusable packages free of what they must not know about, by scanning the shared source tree.
 *
 * <ul>
 *   <li>{@code dev.vercim.ui.sim}: JDK and JOML only;</li>
 *   <li>{@code dev.vercim.ui.config}: no Minecraft or loaders (Gson and the JDK only);</li>
 *   <li>{@code dev.vercim.ui}: nothing from {@code dev.vercim.handycam};</li>
 *   <li>{@code dev.vercim.handycam.rig}: no Minecraft, no loaders, no game-side handycam packages.</li>
 * </ul>
 */
class PackageBoundariesTest {

    private static final String MINECRAFT_AND_LOADERS =
            "net\\.minecraft|com\\.mojang|net\\.fabricmc|net\\.neoforged|net\\.minecraftforge|org\\.spongepowered"
            + "|me\\.shedaniel|com\\.terraformersmc";

    private static Path sourceRoot() {
        return Path.of(System.getProperty("handycam.rootDir")).resolve("src/main/java");
    }

    private static List<String> violations(String packageDir, String forbidden) throws IOException {
        Path dir = sourceRoot().resolve(packageDir);
        if (!Files.isDirectory(dir)) return List.of();

        Pattern pattern = Pattern.compile("(?m)^\\s*(?:import\\s+(?:static\\s+)?)?(?:" + forbidden + ")[.\\w*]*"
                + "|\\b(?:" + forbidden + ")\\.[A-Za-z]");
        List<String> found = new ArrayList<>();
        try (Stream<Path> files = Files.walk(dir)) {
            for (Path file : (Iterable<Path>) files.filter(p -> p.toString().endsWith(".java"))::iterator) {
                String text = Files.readString(file);
                var matcher = pattern.matcher(text);
                if (matcher.find()) found.add(sourceRoot().relativize(file) + ": " + matcher.group().trim());
            }
        }
        return found;
    }

    @Test
    void simPackageIsPureJava() throws IOException {
        assertTrue(Files.isDirectory(sourceRoot().resolve("dev/vercim/ui/sim")), "sim package missing");
        assertEquals(List.of(), violations("dev/vercim/ui/sim",
                MINECRAFT_AND_LOADERS + "|dev\\.vercim\\.handycam|dev\\.vercim\\.ui\\.(?!sim)\\w+"));
    }

    @Test
    void uiConfigPackageIsFreeOfMinecraft() throws IOException {
        assertTrue(Files.isDirectory(sourceRoot().resolve("dev/vercim/ui/config")), "ui.config package missing");
        assertEquals(List.of(), violations("dev/vercim/ui/config", MINECRAFT_AND_LOADERS));
    }

    @Test
    void uiPackagesDoNotDependOnHandycam() throws IOException {
        assertEquals(List.of(), violations("dev/vercim/ui", "dev\\.vercim\\.handycam"));
    }

    @Test
    void rigIsPureJava() throws IOException {
        assertTrue(Files.isDirectory(sourceRoot().resolve("dev/vercim/handycam/rig")), "rig package missing");
        assertEquals(List.of(), violations("dev/vercim/handycam/rig",
                MINECRAFT_AND_LOADERS + "|dev\\.vercim\\.handycam\\.(?:camera|config|mixin|fabric|neoforge)"));
    }
}
