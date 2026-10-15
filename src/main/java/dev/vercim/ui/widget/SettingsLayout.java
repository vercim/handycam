// SPDX-License-Identifier: LGPL-3.0-only
package dev.vercim.ui.widget;

/**
 * Where the parts of the settings screen go. Wide windows get three columns. The option list is on the left with
 * Save and Cancel under it, the panel for the selected option is on the right with Reset All under it, and the two
 * views, the first-person view above the 3D view, take the whole height of the screen between them. The buttons are
 * never wider than the column they belong to. Narrow windows put the two views side by side across the top and the
 * list and the panel, with their buttons, underneath. The title is above the list and a status text above the panel
 * (across the top row in narrow windows). Plain geometry, in GUI pixels, so it can be checked without the game.
 */
public record SettingsLayout(boolean narrow, Rect title, Rect status, Rect list, Rect pov, Rect view3d, Rect panel,
                             Rect save, Rect cancel, Rect resetAll) {

    public static final int MARGIN = 8;
    public static final int GAP = 6;
    public static final int TITLE_HEIGHT = 12;
    public static final int BUTTON_HEIGHT = 20;

    /** Below this GUI width the views move to the top. */
    public static final int NARROW_BELOW = 380;

    private static final int HEADER_GAP = 2;
    private static final int LIST_MIN = 100;
    private static final int LIST_MAX = 210;
    private static final int PANEL_MIN = 96;
    private static final int PANEL_MAX = 200;
    private static final int PREVIEW_MIN_HEIGHT = 48;
    /** Space between the first-person view and the 3D view. */
    private static final int VIEW_GAP = 2;

    public record Rect(int x, int y, int width, int height) {
        public int right() { return x + width; }
        public int bottom() { return y + height; }

        public boolean contains(double px, double py) {
            return px >= x && px < right() && py >= y && py < bottom();
        }

        public boolean overlaps(Rect other) {
            return width > 0 && height > 0 && other.width > 0 && other.height > 0
                    && x < other.right() && other.x < right() && y < other.bottom() && other.y < bottom();
        }
    }

    public static SettingsLayout compute(int width, int height) {
        int inner = Math.max(0, width - 2 * MARGIN);
        int top = MARGIN;
        int bottom = Math.max(top, height - MARGIN);
        int headerHeight = TITLE_HEIGHT + HEADER_GAP;
        int buttonsY = Math.max(top, bottom - BUTTON_HEIGHT);

        if (width >= NARROW_BELOW) {
            int listWidth = clamp(inner * 34 / 100, LIST_MIN, LIST_MAX);
            int panelWidth = clamp(inner * 26 / 100, PANEL_MIN, PANEL_MAX);
            int viewWidth = Math.max(0, inner - listWidth - panelWidth - 2 * GAP);
            int viewX = MARGIN + listWidth + GAP;
            int panelX = viewX + viewWidth + GAP;
            int columnTop = Math.min(bottom, top + headerHeight);
            int columnHeight = Math.max(0, buttonsY - GAP - columnTop);

            int viewsHeight = bottom - top;
            int povHeight = Math.max(0, (viewsHeight - VIEW_GAP) * 45 / 100);
            int view3dHeight = Math.max(0, viewsHeight - VIEW_GAP - povHeight);

            return new SettingsLayout(false,
                    new Rect(MARGIN, top, listWidth, TITLE_HEIGHT),
                    new Rect(panelX, top, panelWidth, TITLE_HEIGHT),
                    new Rect(MARGIN, columnTop, listWidth, columnHeight),
                    new Rect(viewX, top, viewWidth, povHeight),
                    new Rect(viewX, top + povHeight + VIEW_GAP, viewWidth, view3dHeight),
                    new Rect(panelX, columnTop, panelWidth, columnHeight),
                    saveButton(MARGIN, buttonsY, listWidth), cancelButton(MARGIN, buttonsY, listWidth),
                    new Rect(panelX, buttonsY, panelWidth, BUTTON_HEIGHT));
        }

        int listWidth = Math.max(0, (inner - GAP) * 55 / 100);
        int panelWidth = Math.max(0, inner - GAP - listWidth);
        int panelX = MARGIN + listWidth + GAP;
        int viewsTop = Math.min(bottom, top + headerHeight);
        int available = Math.max(0, buttonsY - GAP - viewsTop);
        int viewsHeight = Math.min(available, Math.max(PREVIEW_MIN_HEIGHT, available * 40 / 100));
        int povWidth = Math.max(0, (inner - VIEW_GAP) / 2);
        int view3dWidth = Math.max(0, inner - VIEW_GAP - povWidth);
        int lowerTop = viewsTop + viewsHeight + GAP;
        int lowerHeight = Math.max(0, buttonsY - GAP - lowerTop);

        return new SettingsLayout(true,
                new Rect(MARGIN, top, listWidth, TITLE_HEIGHT),
                new Rect(panelX, top, panelWidth, TITLE_HEIGHT),
                new Rect(MARGIN, lowerTop, listWidth, lowerHeight),
                new Rect(MARGIN, viewsTop, povWidth, viewsHeight),
                new Rect(MARGIN + povWidth + VIEW_GAP, viewsTop, view3dWidth, viewsHeight),
                new Rect(panelX, lowerTop, panelWidth, lowerHeight),
                saveButton(MARGIN, buttonsY, listWidth), cancelButton(MARGIN, buttonsY, listWidth),
                new Rect(panelX, buttonsY, panelWidth, BUTTON_HEIGHT));
    }

    /** Save and Cancel share the width of the list between them. */
    private static Rect saveButton(int x, int y, int columnWidth) {
        return new Rect(x, y, Math.max(0, (columnWidth - GAP) / 2), BUTTON_HEIGHT);
    }

    private static Rect cancelButton(int x, int y, int columnWidth) {
        int saveWidth = Math.max(0, (columnWidth - GAP) / 2);
        return new Rect(x + saveWidth + GAP, y, Math.max(0, columnWidth - saveWidth - GAP), BUTTON_HEIGHT);
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
