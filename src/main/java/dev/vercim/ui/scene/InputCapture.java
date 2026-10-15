// SPDX-License-Identifier: LGPL-3.0-only
package dev.vercim.ui.scene;

import java.util.EnumSet;
import java.util.Set;

/**
 * Keyboard capture for a preview. A click turns it on; Escape, a click elsewhere or losing focus turns it off. While
 * on, the keys the user bound for movement are tracked from press and release events; the real key state is polled
 * when the window regains focus, so a release that never arrived (key let go over another window) cannot leave a
 * control stuck.
 */
public final class InputCapture {

    public static final int KEY_ESCAPE = 256;

    /** Where the user's key bindings come from. */
    public interface Bindings {
        /** The control bound to this key, or null. */
        Control match(int keyCode, int scanCode);

        /** Whether the control's key is polled: false for mouse buttons and unbound controls. */
        boolean canPoll(Control control);

        /** Whether the control's keyboard key is physically down right now. */
        boolean isDown(Control control);
    }

    private final Bindings bindings;
    private final Set<Control> held = EnumSet.noneOf(Control.class);
    private boolean captured;

    public InputCapture(Bindings bindings) {
        this.bindings = bindings;
    }

    public boolean isCaptured() {
        return captured;
    }

    public void capture() {
        captured = true;
    }

    /** Stops capturing and lets go of every control. */
    public void release() {
        captured = false;
        held.clear();
    }

    /**
     * @return true if the event was used and the screen must not act on it (so Escape does not close the screen)
     */
    public boolean keyPressed(int keyCode, int scanCode) {
        if (!captured) return false;
        if (keyCode == KEY_ESCAPE) {
            release();
            return true;
        }
        Control control = bindings.match(keyCode, scanCode);
        if (control == null) return false;
        held.add(control);
        return true;
    }

    public boolean keyReleased(int keyCode, int scanCode) {
        if (!captured) return false;
        Control control = bindings.match(keyCode, scanCode);
        if (control == null) return false;
        held.remove(control);
        return true;
    }

    /**
     * Drops controls whose key is no longer physically down. Call when focus comes back to the window: a key let go
     * while another window had focus never sent its release.
     */
    public void poll() {
        if (held.isEmpty()) return;
        held.removeIf(control -> bindings.canPoll(control) && !bindings.isDown(control));
    }

    public InputState state() {
        if (!captured) return InputState.NONE;
        return new InputState(held.contains(Control.FORWARD), held.contains(Control.BACK),
                held.contains(Control.LEFT), held.contains(Control.RIGHT),
                held.contains(Control.JUMP), held.contains(Control.SPRINT));
    }
}
