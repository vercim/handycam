package dev.vercim.handycam.preview;

/**
 * Where the game's HUD elements go in the first-person view, in the game's own coordinates: the numbers are the ones
 * the game's HUD code uses, for a screen {@code width} by {@code height} sprite pixels. The view is a miniature of a
 * game window, so the HUD is drawn at one sprite pixel per physical pixel ({@code scale} is 1 over the GUI scale):
 * crisp, instead of a pixel-art sprite squeezed by a fraction and left to drop pixels. When that would make the view
 * too narrow for the hotbar, the scale gives way.
 */
record PovHudLayout(float scale, int width, int height) {

    static final int HOTBAR_WIDTH = 182;
    static final int HOTBAR_HEIGHT = 22;
    static final int SLOTS = 9;
    static final int SLOT_PITCH = 20;
    static final int ICON = 9;
    static final int ICONS = 10;
    static final int ICON_PITCH = 8;
    static final int CROSSHAIR = 15;
    static final int EXPERIENCE_WIDTH = 182;
    static final int EXPERIENCE_HEIGHT = 5;

    /** The narrowest virtual screen the hotbar and the rows beside the crosshair fit on. */
    private static final int MIN_WIDTH = 200;

    /**
     * @param viewWidth  width of the view in GUI units
     * @param viewHeight height of the view in GUI units
     * @param guiScale   the game's GUI scale: physical pixels per GUI unit
     */
    static PovHudLayout of(int viewWidth, int viewHeight, double guiScale) {
        float scale = (float) (1.0 / Math.max(1.0, guiScale));
        if (viewWidth / scale < MIN_WIDTH) scale = viewWidth / (float) MIN_WIDTH;
        scale = Math.max(scale, 1e-3f);
        return new PovHudLayout(scale, Math.round(viewWidth / scale), Math.round(viewHeight / scale));
    }

    int hotbarX() { return width / 2 - HOTBAR_WIDTH / 2; }
    int hotbarY() { return height - HOTBAR_HEIGHT; }

    int selectionX(int slot) { return hotbarX() - 1 + slot * SLOT_PITCH; }
    int selectionY() { return hotbarY() - 1; }

    int itemX(int slot) { return width / 2 - 90 + slot * SLOT_PITCH + 2; }
    int itemY() { return height - 16 - 3; }

    int experienceX() { return width / 2 - EXPERIENCE_WIDTH / 2; }
    int experienceY() { return height - 32 + 3; }
    int levelY() { return height - 31 - 4; }

    int heartX(int index) { return width / 2 - 91 + index * ICON_PITCH; }
    int foodX(int index) { return width / 2 + 91 - index * ICON_PITCH - ICON; }
    int rowY() { return height - 39; }

    int crosshairX() { return (width - CROSSHAIR) / 2; }
    int crosshairY() { return (height - CROSSHAIR) / 2; }
}
