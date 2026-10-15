// SPDX-License-Identifier: LGPL-3.0-only
package dev.vercim.ui.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

/**
 * Breaks text into lines that fit a pixel width, given a function that measures a string (the game's font, or
 * anything in a test). Words are kept whole where they fit, a word wider than a line is cut, and when the text needs
 * more lines than allowed the last one ends in an ellipsis.
 */
public final class TextWrap {

    private static final String ELLIPSIS = "…";

    private TextWrap() {}

    public static List<String> wrap(String text, int maxWidth, ToIntFunction<String> width, int maxLines) {
        List<String> lines = new ArrayList<>();
        if (text == null || maxLines <= 0 || maxWidth <= 0) return lines;

        boolean truncated = false;
        for (String paragraph : text.split("\n", -1)) {
            if (!fill(paragraph, maxWidth, width, lines, maxLines)) {
                truncated = true;
                break;
            }
        }
        if (truncated && !lines.isEmpty()) {
            int last = lines.size() - 1;
            lines.set(last, withEllipsis(lines.get(last), maxWidth, width));
        }
        return lines;
    }

    /** Adds the lines of one paragraph; false once the line limit is hit with text left over. */
    private static boolean fill(String paragraph, int maxWidth, ToIntFunction<String> width,
                                List<String> lines, int maxLines) {
        StringBuilder line = new StringBuilder();
        for (String word : paragraph.trim().split("\\s+")) {
            if (word.isEmpty()) continue;
            String candidate = line.length() == 0 ? word : line + " " + word;
            if (width.applyAsInt(candidate) <= maxWidth) {
                line.setLength(0);
                line.append(candidate);
                continue;
            }
            if (line.length() > 0) {
                if (lines.size() == maxLines) return false;
                lines.add(line.toString());
                line.setLength(0);
            }
            // The word starts a fresh line; cut it while it is still too wide.
            String rest = word;
            while (width.applyAsInt(rest) > maxWidth && rest.length() > 1) {
                int cut = fitting(rest, maxWidth, width);
                if (lines.size() == maxLines) return false;
                lines.add(rest.substring(0, cut));
                rest = rest.substring(cut);
            }
            line.append(rest);
        }
        if (line.length() > 0) {
            if (lines.size() == maxLines) return false;
            lines.add(line.toString());
        }
        return true;
    }

    /** How many leading characters of {@code s} fit, at least one. */
    private static int fitting(String s, int maxWidth, ToIntFunction<String> width) {
        int n = 1;
        while (n < s.length() && width.applyAsInt(s.substring(0, n + 1)) <= maxWidth) n++;
        return n;
    }

    private static String withEllipsis(String line, int maxWidth, ToIntFunction<String> width) {
        String s = line;
        while (!s.isEmpty() && width.applyAsInt(s + ELLIPSIS) > maxWidth) {
            s = s.substring(0, s.length() - 1).stripTrailing();
        }
        return s + ELLIPSIS;
    }
}
