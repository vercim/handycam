// SPDX-License-Identifier: LGPL-3.0-only
package dev.vercim.ui.widget;

/** Colors (ARGB) and spacing shared by the settings widgets. The one place to change how they look. */
public final class Theme {

    private Theme() {}

    // surfaces: neutral dark grays, panels a shade lighter, the 3D view drawn straight onto the screen color
    public static final int BACKGROUND = 0xFF141414;
    public static final int PANEL = 0xFF1D1D1D;
    public static final int PANEL_BORDER = 0xFF3A3A3A;
    public static final int TRACK = 0xFF0E0E0E;

    // text
    public static final int TEXT = 0xFFEBEBEB;
    public static final int TEXT_DIM = 0xFFA3A3A3;
    public static final int TEXT_DISABLED = 0xFF5E5E5E;
    public static final int CATEGORY = 0xFFD9B766;
    public static final int WARNING = 0xFFFF9A6A;

    // accents and states: the only color on the screen is the amber
    public static final int ACCENT = 0xFFE8B040;
    public static final int ACCENT_DIM = 0xFF7A5E22;
    public static final int EDITED = 0xFFFFD35A;
    public static final int ROW_HOVER = 0x30FFFFFF;
    public static final int ROW_SELECTED = 0x40E8B040;
    public static final int TICK = 0xFF7A7A7A;
    public static final int SCROLLBAR = 0xFF474747;
    public static final int SCROLLBAR_HOVER = 0xFF777777;

    // buttons
    public static final int BUTTON = 0xFF2A2A2A;
    public static final int BUTTON_HOVER = 0xFF3B3B3B;
    public static final int BUTTON_PRESSED = 0xFF1B1B1B;
    public static final int BUTTON_DISABLED = 0xFF1A1A1A;
    public static final int BUTTON_PRIMARY = 0xFF4A3A12;
    public static final int BUTTON_PRIMARY_HOVER = 0xFF675018;
    public static final int BUTTON_PRIMARY_PRESSED = 0xFF352A0D;

    // spacing
    public static final int PADDING = 4;
    public static final int SCROLLBAR_WIDTH = 4;
}
