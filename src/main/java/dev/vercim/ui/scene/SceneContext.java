// SPDX-License-Identifier: LGPL-3.0-only
package dev.vercim.ui.scene;

/** What a scene is given when it starts. */
public interface SceneContext {

    /** The orbit camera the user steers; a scene can place it, and reads its yaw to move relative to the view. */
    OrbitCamera camera();
}
