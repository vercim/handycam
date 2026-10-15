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
        return List.of(layout.title(), layout.list(), layout.pov(), layout.view3d(), layout.previewButtons(),
                layout.panel(), layout.footer());
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
    void wideWindowsPutTheListLeftThePanelRightAndTheTwoViewsStackedBetween() {
        SettingsLayout layout = SettingsLayout.compute(854, 480);

        assertFalse(layout.narrow());
        assertTrue(layout.list().right() <= layout.pov().x());
        assertTrue(layout.pov().right() <= layout.panel().x());
        assertEquals(layout.pov().x(), layout.view3d().x());
        assertEquals(layout.pov().width(), layout.view3d().width());
        assertTrue(layout.pov().bottom() <= layout.view3d().y(), "the first-person view is above the 3D view");
        assertEquals(layout.view3d().x(), layout.previewButtons().x());
        assertEquals(layout.view3d().width(), layout.previewButtons().width());
        assertTrue(layout.previewButtons().y() >= layout.view3d().bottom());
        assertEquals(layout.list().y(), layout.panel().y());
        assertEquals(layout.list().y(), layout.pov().y());
        assertTrue(layout.pov().width() > layout.list().width(), "the views are the big part");
    }

    @Test
    void theTwoViewsShareTheColumnHeightAboutEvenly() {
        for (int[] size : new int[][] {{427, 240}, {854, 480}, {1920, 1080}}) {
            SettingsLayout layout = SettingsLayout.compute(size[0], size[1]);
            int pov = layout.pov().height();
            int d3 = layout.view3d().height();
            assertTrue(pov > 0 && d3 > 0, size[0] + "x" + size[1]);
            assertTrue(Math.abs(pov - d3) <= (pov + d3) / 4, size[0] + "x" + size[1] + ": " + pov + " and " + d3);
        }
    }

    @Test
    void narrowWindowsPutTheTwoViewsSideBySideOnTop() {
        SettingsLayout layout = SettingsLayout.compute(320, 240);

        assertTrue(layout.narrow());
        assertTrue(layout.pov().right() <= layout.view3d().x());
        assertEquals(layout.pov().y(), layout.view3d().y());
        assertEquals(layout.pov().height(), layout.view3d().height());
        assertTrue(layout.pov().bottom() <= layout.previewButtons().y());
        assertTrue(layout.previewButtons().bottom() <= layout.list().y());
        assertTrue(layout.previewButtons().bottom() <= layout.panel().y());
        assertTrue(layout.list().right() <= layout.panel().x());
        assertEquals(layout.list().y(), layout.panel().y());
        assertTrue(layout.pov().height() >= 48);
        assertTrue(layout.list().height() >= 80, "the lower half stays usable: " + layout.list());
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
        assertTrue(layout.pov().width() >= 120);
        assertTrue(layout.pov().height() >= 60);
        assertTrue(layout.view3d().height() >= 60);
        assertEquals(20, layout.footer().height());
    }

    @Test
    void theFooterIsAtTheBottomAndTheTitleAtTheTop() {
        for (int[] size : SIZES) {
            SettingsLayout layout = SettingsLayout.compute(size[0], size[1]);
            assertEquals(size[1] - SettingsLayout.MARGIN, layout.footer().bottom());
            assertEquals(SettingsLayout.TITLE_TOP, layout.title().y());
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
