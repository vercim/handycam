// SPDX-License-Identifier: LGPL-3.0-only
package dev.vercim.ui.scene;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;

/** The player's own movement key bindings from the options, not fixed key codes. */
final class MinecraftBindings implements InputCapture.Bindings {

    private final Minecraft minecraft;

    MinecraftBindings(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    private KeyMapping mapping(Control control) {
        Options o = minecraft.options;
        return switch (control) {
            case FORWARD -> o.keyUp;
            case BACK -> o.keyDown;
            case LEFT -> o.keyLeft;
            case RIGHT -> o.keyRight;
            case JUMP -> o.keyJump;
            case SPRINT -> o.keySprint;
            case CROUCH -> o.keyShift;
        };
    }

    @Override
    public Control match(int keyCode, int scanCode) {
        for (Control control : Control.values()) {
            if (mapping(control).matches(keyCode, scanCode)) return control;
        }
        return null;
    }

    private InputConstants.Key keyOf(Control control) {
        KeyMapping mapping = mapping(control);
        return mapping.isUnbound() ? null : InputConstants.getKey(mapping.saveString());
    }

    @Override
    public boolean canPoll(Control control) {
        InputConstants.Key key = keyOf(control);
        return key != null && key.getType() == InputConstants.Type.KEYSYM;
    }

    @Override
    public boolean isDown(Control control) {
        InputConstants.Key key = keyOf(control);
        return key != null && InputConstants.isKeyDown(minecraft.getWindow().getWindow(), key.getValue());
    }
}
