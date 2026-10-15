// SPDX-License-Identifier: LGPL-3.0-only
package dev.vercim.ui.scene;

/** Which controls are held right now. All false while the preview is not capturing the keyboard. */
public record InputState(boolean forward, boolean back, boolean left, boolean right, boolean jump, boolean sprint,
                         boolean crouch) {

    public static final InputState NONE = new InputState(false, false, false, false, false, false, false);

    /** Without the crouch control: for scenes that do not use it. */
    public InputState(boolean forward, boolean back, boolean left, boolean right, boolean jump, boolean sprint) {
        this(forward, back, left, right, jump, sprint, false);
    }

    /** -1, 0 or 1: forward minus back. */
    public int moveForward() {
        return (forward ? 1 : 0) - (back ? 1 : 0);
    }

    /** -1, 0 or 1: right minus left. */
    public int moveRight() {
        return (right ? 1 : 0) - (left ? 1 : 0);
    }

    public boolean isHeld(Control control) {
        return switch (control) {
            case FORWARD -> forward;
            case BACK -> back;
            case LEFT -> left;
            case RIGHT -> right;
            case JUMP -> jump;
            case SPRINT -> sprint;
            case CROUCH -> crouch;
        };
    }
}
