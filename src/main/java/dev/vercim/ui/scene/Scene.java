// SPDX-License-Identifier: LGPL-3.0-only
package dev.vercim.ui.scene;

/**
 * Something to show in a {@link PreviewWidget}: it simulates in fixed steps and draws itself through the primitives
 * of {@link SceneRenderContext}, so it never touches the renderer.
 */
public interface Scene {

    /** Called once, before the first tick. */
    void init(SceneContext context);

    /** Advances the simulation by one fixed step ({@code dt} is always the same, 1/120 s). */
    void tick(double dt, InputState input);

    /**
     * Draws the current state.
     *
     * @param alpha how far the next step is through, 0 to 1, for interpolating between the last two states
     */
    void render(SceneRenderContext context, float alpha);

    /** Called when the preview is closed. */
    default void close() {}
}
