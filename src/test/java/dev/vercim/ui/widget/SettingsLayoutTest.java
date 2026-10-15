package dev.vercim.ui.widget;

import dev.vercim.ui.widget.SettingsLayout.Rect;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SettingsLayoutTest {

    /** GUI sizes a player really gets: small windows at high scale up to large ones at scale 2. */
    private static final int[][] SIZES = {
            {427, 240}, {320, 240}, {360, 200}, {400, 225}, {480, 270}, {640, 360}, {854, 480}, {960, 540}, {1920, 1080},
    };

    private static List<Rect> parts(SettingsLayout layout) {
        return List.of(layout.title(), layout.status(), layout.list(), layout.view3d(), layout.panel(),
                layout.save(), layout.cancel(), layout.resetAll());
    }

    @Test
    void everythingIsInsideTheScreen() {
        for (int[] size : SIZES) {
            SettingsLayout layout = SettingsLayout.compute(size[0], size[1]);
            for (Rect r : parts(layout)) {
                String where = size[0] + "x" + size[1] + " " + r;
                assertTrue(r.x() >= 0 && r.y() >= 0, where);
                assertTrue(r.right() <= size[0] && r.bottom() <= size[1], where);
                assertTrue(r.width() >= 0 && r.height() >= 0, where);
            }
        }
    }

    @Test
    void nothingOverlaps() {
        for (int[] size : SIZES) {
            List<Rect> parts = parts(SettingsLayout.compute(size[0], size[1]));
            for (int i = 0; i < parts.size(); i++) {
                for (int j = i + 1; j < parts.size(); j++) {
                    assertFalse(parts.get(i).overlaps(parts.get(j)),
                            size[0] + "x" + size[1] + ": " + parts.get(i) + " overlaps " + parts.get(j));
                }
            }
        }
    }

    @Test
    void theButtonsNeverGoBeyondTheColumnTheyBelongTo() {
        for (int[] size : SIZES) {
            SettingsLayout layout = SettingsLayout.compute(size[0], size[1]);
            String where = size[0] + "x" + size[1];

            assertTrue(layout.save().x() >= layout.list().x(), where);
            assertTrue(layout.cancel().right() <= layout.list().right(), where);
            assertTrue(layout.save().right() <= layout.cancel().x(), where);
            assertTrue(layout.resetAll().x() >= layout.panel().x(), where);
            assertTrue(layout.resetAll().right() <= layout.panel().right(), where);
            // and they sit under the column, not beside it
            assertTrue(layout.save().y() >= layout.list().bottom(), where);
            assertTrue(layout.resetAll().y() >= layout.panel().bottom(), where);
        }
    }

    @Test
    void wideWindowsPutTheListLeftThePanelRightAndTheViewBetween() {
        SettingsLayout layout = SettingsLayout.compute(854, 480);

        assertFalse(layout.narrow());
        assertTrue(layout.list().right() <= layout.view3d().x());
        assertTrue(layout.view3d().right() <= layout.panel().x());
        assertTrue(layout.view3d().width() > layout.list().width(), "the view is the big part");
        assertTrue(layout.view3d().width() > layout.panel().width(), "the view is the big part");
    }

    @Test
    void theViewTakesTheWholeHeightOfTheScreenBetweenTheMargins() {
        for (int[] size : new int[][] {{427, 240}, {480, 270}, {854, 480}, {1920, 1080}}) {
            SettingsLayout layout = SettingsLayout.compute(size[0], size[1]);
            String where = size[0] + "x" + size[1];

            assertEquals(SettingsLayout.MARGIN, layout.view3d().y(), where);
            assertEquals(size[1] - SettingsLayout.MARGIN, layout.view3d().bottom(), where);
        }
    }

    @Test
    void narrowWindowsPutTheViewAcrossTheTop() {
        SettingsLayout layout = SettingsLayout.compute(320, 240);

        assertTrue(layout.narrow());
        assertEquals(SettingsLayout.MARGIN, layout.view3d().x());
        assertEquals(320 - SettingsLayout.MARGIN, layout.view3d().right());
        assertTrue(layout.view3d().bottom() <= layout.list().y());
        assertTrue(layout.view3d().bottom() <= layout.panel().y());
        assertTrue(layout.list().right() <= layout.panel().x());
        assertEquals(layout.list().y(), layout.panel().y());
        assertTrue(layout.view3d().height() >= 48);
        assertTrue(layout.list().height() >= 60, "the lower half stays usable: " + layout.list());
    }

    @Test
    void theSwitchHappensAtTheNarrowLimit() {
        assertTrue(SettingsLayout.compute(SettingsLayout.NARROW_BELOW - 1, 240).narrow());
        assertFalse(SettingsLayout.compute(SettingsLayout.NARROW_BELOW, 240).narrow());
    }

    @Test
    void theDefaultWindowKeepsUsableColumns() {
        // A 854x480 window at GUI scale 2 is 427x240.
        SettingsLayout layout = SettingsLayout.compute(427, 240);

        assertTrue(layout.list().width() >= 100);
        assertTrue(layout.panel().width() >= 96);
        assertTrue(layout.view3d().width() >= 150);
        assertTrue(layout.view3d().height() >= 150);
        assertTrue(layout.list().height() >= 150);
        assertTrue(layout.panel().height() >= 150);
        assertEquals(20, layout.save().height());
        assertTrue(layout.save().width() >= 60, "room for a label: " + layout.save());
        assertTrue(layout.resetAll().width() >= 90, "room for a label: " + layout.resetAll());
    }

    @Test
    void theButtonsAreAtTheBottomAndTheTitleAtTheTop() {
        for (int[] size : SIZES) {
            SettingsLayout layout = SettingsLayout.compute(size[0], size[1]);
            assertEquals(size[1] - SettingsLayout.MARGIN, layout.save().bottom());
            assertEquals(size[1] - SettingsLayout.MARGIN, layout.cancel().bottom());
            assertEquals(size[1] - SettingsLayout.MARGIN, layout.resetAll().bottom());
            assertEquals(SettingsLayout.MARGIN, layout.title().y());
            assertEquals(SettingsLayout.MARGIN, layout.status().y());
        }
    }

    @Test
    void aTinyScreenDoesNotThrowOrGoNegative() {
        for (int[] size : new int[][] {{0, 0}, {50, 50}, {100, 60}, {1, 1}}) {
            SettingsLayout layout = SettingsLayout.compute(size[0], size[1]);
            for (Rect r : parts(layout)) {
                assertTrue(r.width() >= 0 && r.height() >= 0, size[0] + "x" + size[1] + " " + r);
            }
        }
    }

    @Test
    void rectContainsUsesAHalfOpenRange() {
        Rect r = new Rect(10, 20, 5, 4);

        assertTrue(r.contains(10, 20));
        assertTrue(r.contains(14.9, 23.9));
        assertFalse(r.contains(15, 20));
        assertFalse(r.contains(10, 24));
        assertFalse(r.contains(9.9, 20));
    }
}
