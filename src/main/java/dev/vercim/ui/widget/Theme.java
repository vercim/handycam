// SPDX-License-Identifier: LGPL-3.0-only
package dev.vercim.ui.widget;

/** Colors (ARGB) and spacing shared by the settings widgets. The one place to change how they look. */
public final class Theme {

    private Theme() {}

    // surfaces
    public static final int PANEL = 0xFF14181F;
    public static final int PANEL_BORDER = 0xFF3A404C;
    public static final int TRACK = 0xFF0B0E13;

    // text
    public static final int TEXT = 0xFFE0E0E0;
    public static final int TEXT_DIM = 0xFF9A9FA8;
    public static final int TEXT_DISABLED = 0xFF5C616B;
    public static final int CATEGORY = 0xFF8FB4FF;
    public static final int WARNING = 0xFFFF9A6A;

    // accents and states
    public static final int ACCENT = 0xFFE8B040;
    public static final int ACCENT_DIM = 0xFF7A5E22;
    public static final int EDITED = 0xFFFFD35A;
    public static final int ROW_HOVER = 0x30FFFFFF;
    public static final int ROW_SELECTED = 0x40E8B040;
    public static final int TICK = 0xFF6B7280;
    public static final int SCROLLBAR = 0xFF4A5160;
    public static final int SCROLLBAR_HOVER = 0xFF7A8396;

    // spacing
    public static final int PADDING = 4;
    public static final int SCROLLBAR_WIDTH = 4;
}
