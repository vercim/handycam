// SPDX-License-Identifier: LGPL-3.0-only
package dev.vercim.ui.scene;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

import java.util.ArrayDeque;
import java.util.Deque;

/** A stack of model transforms, outermost first: {@code translate} then {@code rotate} moves the rotated thing. */
public final class TransformStack {

    private final Deque<Matrix4f> stack = new ArrayDeque<>();
    private Matrix4f current = new Matrix4f();

    /** Back to the identity with nothing saved. */
    public void reset() {
        stack.clear();
        current = new Matrix4f();
    }

    public void push() {
        stack.push(new Matrix4f(current));
    }

    public void pop() {
        if (stack.isEmpty()) throw new IllegalStateException("pop without a matching push");
        current = stack.pop();
    }

    public int depth() {
        return stack.size();
    }

    public void translate(float x, float y, float z) {
        current.translate(x, y, z);
    }

    public void rotateX(float degrees) {
        current.rotateX((float) Math.toRadians(degrees));
    }

    public void rotateY(float degrees) {
        current.rotateY((float) Math.toRadians(degrees));
    }

    public void rotateZ(float degrees) {
        current.rotateZ((float) Math.toRadians(degrees));
    }

    public void scale(float x, float y, float z) {
        current.scale(x, y, z);
    }

    public Matrix4fc current() {
        return current;
    }

    public Vector3f transform(float x, float y, float z, Vector3f dest) {
        return current.transformPosition(x, y, z, dest);
    }
}
