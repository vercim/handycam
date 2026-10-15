// SPDX-License-Identifier: LGPL-3.0-only
package dev.vercim.ui.widget;

/** Colors (ARGB) and spacing shared by the settings widgets. The one place to change how they look. */
public final class Theme {

    private Theme() {}

    // surfaces: a dark blue screen, panels a shade lighter, the 3D view drawn straight onto the screen color
    public static final int BACKGROUND = 0xFF0A1428;
    /** The first-person view's sky: the game's own sky blue, so it reads as what the player sees and not as the screen. */
    public static final int POV_BACKGROUND = 0xFF78A7FF;
    public static final int PANEL = 0xFF101C36;
    public static final int PANEL_BORDER = 0xFF2B4170;
    public static final int TRACK = 0xFF070E1E;

    // text
    public static final int TEXT = 0xFFE6ECFA;
    public static final int TEXT_DIM = 0xFF9DAACB;
    public static final int TEXT_DISABLED = 0xFF55627F;
    public static final int CATEGORY = 0xFF7FB0FF;
    public static final int WARNING = 0xFFFF9A6A;

    // accents and states
    public static final int ACCENT = 0xFFE8B040;
    public static final int ACCENT_DIM = 0xFF7A5E22;
    public static final int EDITED = 0xFFFFD35A;
    public static final int ROW_HOVER = 0x30FFFFFF;
    public static final int ROW_SELECTED = 0x40E8B040;
    public static final int TICK = 0xFF5F72A0;
    public static final int SCROLLBAR = 0xFF3A4C78;
    public static final int SCROLLBAR_HOVER = 0xFF6A80B8;

    // buttons
    public static final int BUTTON = 0xFF16264A;
    public static final int BUTTON_HOVER = 0xFF213A6E;
    public static final int BUTTON_PRESSED = 0xFF0E1A36;
    public static final int BUTTON_DISABLED = 0xFF0F1A32;
    public static final int BUTTON_PRIMARY = 0xFF4A3A12;
    public static final int BUTTON_PRIMARY_HOVER = 0xFF675018;
    public static final int BUTTON_PRIMARY_PRESSED = 0xFF352A0D;

    // spacing
    public static final int PADDING = 4;
    public static final int SCROLLBAR_WIDTH = 4;
}
