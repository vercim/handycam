// SPDX-License-Identifier: LGPL-3.0-only
package dev.vercim.ui.sim;

import org.joml.Vector3f;
import org.joml.Vector3fc;

/** Three independent {@link Spring1D}s with the same stiffness and damping, one per axis. */
public final class Spring3D {

    private final Spring1D x;
    private final Spring1D y;
    private final Spring1D z;

    public Spring3D(float stiffness, float damping) {
        x = new Spring1D(stiffness, damping);
        y = new Spring1D(stiffness, damping);
        z = new Spring1D(stiffness, damping);
    }

    /** Advances all axes toward {@code target} and writes the new position to {@code dest}. */
    public Vector3f update(Vector3fc target, float dt, Vector3f dest) {
        return dest.set(x.update(target.x(), dt), y.update(target.y(), dt), z.update(target.z(), dt));
    }

    public void addVelocity(Vector3fc v) {
        x.addVelocity(v.x());
        y.addVelocity(v.y());
        z.addVelocity(v.z());
    }

    public void reset() {
        x.reset();
        y.reset();
        z.reset();
    }

    public Vector3f position(Vector3f dest) {
        return dest.set(x.position(), y.position(), z.position());
    }

    public Vector3f velocity(Vector3f dest) {
        return dest.set(x.velocity(), y.velocity(), z.velocity());
    }
}
