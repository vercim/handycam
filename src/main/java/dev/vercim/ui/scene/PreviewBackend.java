// SPDX-License-Identifier: LGPL-3.0-only
package dev.vercim.ui.scene;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Draws a {@link Scene} into a rectangle of the GUI. Backends differ per Minecraft version; this one is chosen in
 * {@link PreviewBackends} and everything else only sees this interface.
 */
interface PreviewBackend extends AutoCloseable {

    /**
     * Renders the scene seen through the camera into the rectangle x, y, width, height (GUI coordinates) of the
     * current screen. Leaves the GL state as it found it.
     */
    void render(Scene scene, ViewCamera camera, float alpha, GuiGraphics graphics, int x, int y, int width, int height,
                int backgroundArgb);

    /** Frees GPU resources. Must be called on the render thread. */
    @Override
    void close();
}
