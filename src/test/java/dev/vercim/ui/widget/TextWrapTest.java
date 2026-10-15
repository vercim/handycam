package dev.vercim.ui.widget;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.ToIntFunction;

import static org.junit.jupiter.api.Assertions.*;

class TextWrapTest {

    /** Every character is 6 pixels wide, as in a monospaced font. */
    private static final ToIntFunction<String> MONO = s -> s.length() * 6;

    @Test
    void shortTextIsOneLine() {
        assertEquals(List.of("Camera shake"), TextWrap.wrap("Camera shake", 120, MONO, 5));
    }

    @Test
    void wordsWrapAtTheWidth() {
        // 60 px = 10 characters
        assertEquals(List.of("Allow", "vanilla FOV", "modifiers"),
                TextWrap.wrap("Allow vanilla FOV modifiers", 66, MONO, 5));
        assertEquals(List.of("Allow", "vanilla", "FOV", "modifiers"),
                TextWrap.wrap("Allow vanilla FOV modifiers", 54, MONO, 5));
    }

    @Test
    void everyLineFitsTheWidth() {
        String text = "How strongly the camera leans when you strafe to the left or to the right";
        for (int width : new int[] {40, 60, 100, 180}) {
            for (String line : TextWrap.wrap(text, width, MONO, 50)) {
                assertTrue(MONO.applyAsInt(line) <= width, "'" + line + "' at " + width);
            }
        }
    }

    @Test
    void noWordIsLostWhenThereIsRoom() {
        String text = "How strongly the camera leans when you strafe";
        String joined = String.join(" ", TextWrap.wrap(text, 70, MONO, 50));
        assertEquals(text, joined);
    }

    @Test
    void aWordWiderThanALineIsCut() {
        List<String> lines = TextWrap.wrap("Supercalifragilistic", 60, MONO, 5);

        assertEquals(List.of("Supercalif", "ragilistic"), lines);
    }

    @Test
    void tooMuchTextEndsInAnEllipsis() {
        List<String> lines = TextWrap.wrap("one two three four five six seven eight", 60, MONO, 2);

        assertEquals(2, lines.size());
        assertTrue(lines.get(1).endsWith("…"), lines.toString());
        assertTrue(MONO.applyAsInt(lines.get(1)) <= 60, lines.get(1));
    }

    @Test
    void textThatJustFitsTheLineLimitIsNotTruncated() {
        List<String> lines = TextWrap.wrap("one two three four", 60, MONO, 2);

        assertEquals(List.of("one two", "three four"), lines);
    }

    @Test
    void explicitNewlinesBreakLines() {
        assertEquals(List.of("first", "second"), TextWrap.wrap("first\nsecond", 120, MONO, 5));
    }

    @Test
    void extraSpacesAreCollapsed() {
        assertEquals(List.of("a b c"), TextWrap.wrap("  a   b  c ", 120, MONO, 5));
    }

    @Test
    void degenerateInputGivesNoLines() {
        assertEquals(List.of(), TextWrap.wrap("", 100, MONO, 3));
        assertEquals(List.of(), TextWrap.wrap(null, 100, MONO, 3));
        assertEquals(List.of(), TextWrap.wrap("text", 0, MONO, 3));
        assertEquals(List.of(), TextWrap.wrap("text", 100, MONO, 0));
    }

    @Test
    void cyrillicWrapsLikeAnythingElse() {
        List<String> lines = TextWrap.wrap("Насколько сильно уменьшается прицел при полном натяжении", 120, MONO, 5);

        assertTrue(lines.size() >= 3);
        assertEquals("Насколько сильно уменьшается прицел при полном натяжении", String.join(" ", lines));
    }
}
