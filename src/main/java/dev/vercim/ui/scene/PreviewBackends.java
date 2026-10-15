// SPDX-License-Identifier: LGPL-3.0-only
package dev.vercim.ui.scene;

/** Picks the backend for the Minecraft version this build targets. */
final class PreviewBackends {

    private PreviewBackends() {}

    static PreviewBackend create() {
        // Minecraft 1.21.1: the GUI has no picture-in-picture renderers yet, so the scene is drawn into an
        // offscreen framebuffer and that texture is drawn into the GUI. Newer versions get another backend here.
        return new FboBackend();
    }
}
