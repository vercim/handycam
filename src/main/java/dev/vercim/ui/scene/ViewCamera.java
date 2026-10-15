// SPDX-License-Identifier: LGPL-3.0-only
package dev.vercim.ui.scene;

import org.joml.Matrix4f;
import org.joml.Vector3f;

/** What a preview needs of a camera to draw a scene through it. */
public interface ViewCamera {

    /** The world to view transform. */
    Matrix4f view(Matrix4f dest);

    /** The perspective projection for a view {@code aspect} (width over height) wide. */
    Matrix4f projection(float aspect, Matrix4f dest);

    /** Where the camera is, in the world. */
    Vector3f eye(Vector3f dest);
}
