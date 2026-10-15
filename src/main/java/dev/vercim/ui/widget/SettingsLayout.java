// SPDX-License-Identifier: LGPL-3.0-only
package dev.vercim.ui.widget;

/**
 * Where the parts of the settings screen go. Wide windows get three columns: the option list on the left, in the
 * middle the first-person view above the 3D view (with the buttons for the events below them) and the panel for the
 * selected option on the right. Narrow windows put the two views side by side across the top and the list and the
 * panel side by side underneath. A title sits above and the footer buttons below in both. Plain geometry, in GUI
 * pixels, so it can be checked without the game.
 */
public record SettingsLayout(boolean narrow, Rect title, Rect list, Rect pov, Rect view3d, Rect previewButtons,
                             Rect panel, Rect footer) {

    public static final int MARGIN = 8;
    public static final int GAP = 6;
    public static final int TITLE_TOP = 6;
    public static final int TITLE_HEIGHT = 12;
    public static final int BUTTON_HEIGHT = 20;

    /** Below this GUI width the preview moves to the top. */
    public static final int NARROW_BELOW = 380;

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
        int top = TITLE_TOP + TITLE_HEIGHT + 4;
        int footerY = Math.max(top, height - MARGIN - BUTTON_HEIGHT);
        int bottom = Math.max(top, footerY - GAP);
        int contentHeight = bottom - top;

        Rect title = new Rect(MARGIN, TITLE_TOP, inner, TITLE_HEIGHT);
        Rect footer = new Rect(MARGIN, footerY, inner, BUTTON_HEIGHT);

        if (width >= NARROW_BELOW) {
            int listWidth = clamp(inner * 34 / 100, LIST_MIN, LIST_MAX);
            int panelWidth = clamp(inner * 26 / 100, PANEL_MIN, PANEL_MAX);
            int viewWidth = Math.max(0, inner - listWidth - panelWidth - 2 * GAP);
            int viewX = MARGIN + listWidth + GAP;
            int viewsHeight = Math.max(0, contentHeight - BUTTON_HEIGHT - GAP);
            int povHeight = Math.max(0, (viewsHeight - VIEW_GAP) * 45 / 100);
            int view3dHeight = Math.max(0, viewsHeight - VIEW_GAP - povHeight);

            return new SettingsLayout(false, title,
                    new Rect(MARGIN, top, listWidth, contentHeight),
                    new Rect(viewX, top, viewWidth, povHeight),
                    new Rect(viewX, top + povHeight + VIEW_GAP, viewWidth, view3dHeight),
                    new Rect(viewX, top + viewsHeight + GAP, viewWidth, Math.min(BUTTON_HEIGHT, contentHeight)),
                    new Rect(viewX + viewWidth + GAP, top, panelWidth, contentHeight),
                    footer);
        }

        int viewsHeight = Math.min(Math.max(0, contentHeight - BUTTON_HEIGHT - GAP),
                Math.max(PREVIEW_MIN_HEIGHT, contentHeight * 34 / 100));
        int povWidth = Math.max(0, (inner - VIEW_GAP) / 2);
        int view3dWidth = Math.max(0, inner - VIEW_GAP - povWidth);
        int buttonsY = top + viewsHeight + GAP;
        int lowerTop = buttonsY + BUTTON_HEIGHT + GAP;
        int lowerHeight = Math.max(0, bottom - lowerTop);
        int listWidth = Math.max(0, (inner - GAP) * 55 / 100);
        int panelWidth = Math.max(0, inner - GAP - listWidth);

        return new SettingsLayout(true, title,
                new Rect(MARGIN, lowerTop, listWidth, lowerHeight),
                new Rect(MARGIN, top, povWidth, viewsHeight),
                new Rect(MARGIN + povWidth + VIEW_GAP, top, view3dWidth, viewsHeight),
                new Rect(MARGIN, buttonsY, inner, BUTTON_HEIGHT),
                new Rect(MARGIN + listWidth + GAP, lowerTop, panelWidth, lowerHeight),
                footer);
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
