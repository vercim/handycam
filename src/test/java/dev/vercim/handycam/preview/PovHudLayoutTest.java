package dev.vercim.handycam.preview;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PovHudLayoutTest {

    @Test
    void oneSpritePixelIsOnePhysicalPixelWhenTheViewIsWideEnough() {
        for (double guiScale : new double[] {2, 3, 4}) {
            PovHudLayout layout = PovHudLayout.of(154, 99, guiScale);
            if (154 * guiScale / 1.0 >= 200) {
                assertEquals(1.0 / guiScale, layout.scale(), 1e-6, "gui scale " + guiScale);
            }
        }
        assertEquals(0.5f, PovHudLayout.of(154, 99, 2).scale(), 1e-6f);
        assertEquals(1f / 3f, PovHudLayout.of(154, 99, 3).scale(), 1e-6f);
    }

    @Test
    void theVirtualScreenIsTheViewDividedByTheScale() {
        PovHudLayout layout = PovHudLayout.of(154, 99, 2);

        assertEquals(308, layout.width());
        assertEquals(198, layout.height());
    }

    @Test
    void aNarrowViewShrinksTheHudSoTheHotbarStillFits() {
        PovHudLayout layout = PovHudLayout.of(150, 80, 1);   // at GUI scale 1 a 150 unit view is 150 pixels

        assertTrue(layout.width() >= 200, "width " + layout.width());
        assertTrue(layout.scale() < 1f);
        assertTrue(layout.hotbarX() >= 0);
        assertTrue(layout.hotbarX() + PovHudLayout.HOTBAR_WIDTH <= layout.width());
    }

    @Test
    void thePositionsAreTheGamesOwn() {
        PovHudLayout l = new PovHudLayout(0.5f, 400, 200);

        assertEquals(200 - 91, l.hotbarX());
        assertEquals(200 - 22, l.hotbarY());
        assertEquals(200 - 91 - 1, l.selectionX(0));
        assertEquals(200 - 91 - 1 + 40, l.selectionX(2));
        assertEquals(200 - 22 - 1, l.selectionY());
        assertEquals(200 - 90 + 2, l.itemX(0));
        assertEquals(200 - 16 - 3, l.itemY());
        assertEquals(200 - 32 + 3, l.experienceY());
        assertEquals(200 - 39, l.rowY());
        assertEquals((400 - 15) / 2, l.crosshairX());
        assertEquals((200 - 15) / 2, l.crosshairY());
    }

    @Test
    void heartsAreOnTheLeftFoodOnTheRightAndNeitherTouchesTheOther() {
        PovHudLayout l = PovHudLayout.of(300, 160, 1);

        int heartsEnd = l.heartX(PovHudLayout.ICONS - 1) + PovHudLayout.ICON;
        int foodStart = l.foodX(PovHudLayout.ICONS - 1);
        assertTrue(heartsEnd < foodStart, heartsEnd + " vs " + foodStart);
        assertEquals(l.hotbarX(), l.heartX(0), "the hearts start where the hotbar starts");
        assertEquals(l.hotbarX() + PovHudLayout.HOTBAR_WIDTH, l.foodX(0) + PovHudLayout.ICON, "the food ends where it ends");
    }

    @Test
    void theRowsStackAboveTheHotbarWithoutOverlap() {
        PovHudLayout l = new PovHudLayout(1f, 427, 240);

        int hotbarTop = l.hotbarY();
        int experienceBottom = l.experienceY() + PovHudLayout.EXPERIENCE_HEIGHT;
        int rowBottom = l.rowY() + PovHudLayout.ICON;
        assertTrue(experienceBottom <= hotbarTop, "the experience bar is above the hotbar");
        assertTrue(rowBottom <= l.experienceY(), "the hearts and food are above the experience bar");
        assertTrue(l.levelY() < l.experienceY(), "the level number is above the bar");
    }

    @Test
    void everythingStaysInsideTheViewForRealSizes() {
        for (int[] view : new int[][] {{154, 99}, {154, 70}, {304, 62}, {300, 200}, {640, 360}}) {
            for (double guiScale : new double[] {1, 2, 3, 4}) {
                PovHudLayout l = PovHudLayout.of(view[0], view[1], guiScale);
                String where = view[0] + "x" + view[1] + " at " + guiScale;

                assertTrue(l.hotbarX() >= 0 && l.hotbarX() + PovHudLayout.HOTBAR_WIDTH <= l.width(), where);
                assertTrue(l.heartX(0) >= 0, where);
                assertTrue(l.foodX(0) + PovHudLayout.ICON <= l.width(), where);
                assertTrue(l.hotbarY() + PovHudLayout.HOTBAR_HEIGHT <= l.height(), where);
            }
        }
    }
}
