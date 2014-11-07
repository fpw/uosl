package org.solhost.folko.uosl.slclient.views.util;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

// Inspired by http://goharsha.com/lwjgl-tutorial-series/matrix-transforms/
// and http://goharsha.com/lwjgl-tutorial-series/matrix-projections/

public class Transform {
    public final Matrix4f mat;
    private FloatBuffer buffer;

    public Transform() {
        mat = new Matrix4f();
    }

    public Transform(Transform other) {
        this.mat = Matrix4f.load(other.mat, null);
    }

    public void reset() {
        mat.setIdentity();
    }

    public void reset(Transform projection) {
        mat.load(projection.mat);
    }

    public Transform translate(float x, float y, float z) {
        Matrix4f.translate(new Vector3f(x, y, z), mat, mat);
        return this;
    }

    public Transform scale(float s) {
        return scale(s, s, s);
    }

    public Transform scale(float sx, float sy, float sz) {
        Matrix4f.scale(new Vector3f(sx, sy, sz), mat, mat);
        return this;
    }

    public Transform rotate(float rx, float ry, float rz) {
        Matrix4f.rotate((float) Math.toRadians(rx), new Vector3f(1, 0, 0), mat, mat);
        Matrix4f.rotate((float) Math.toRadians(ry), new Vector3f(0, 1, 0), mat, mat);
        Matrix4f.rotate((float) Math.toRadians(rz), new Vector3f(0, 0, 1), mat, mat);
        return this;
    }

    public Transform combine(Transform other) {
        Matrix4f.mul(other.mat, this.mat, this.mat);
        return this;
    }

    public FloatBuffer getFloatBuffer() {
        if(buffer == null) {
            buffer = BufferUtils.createFloatBuffer(16);
        } else {
            buffer.clear();
        }
        mat.store(buffer);
        buffer.rewind();
        return buffer;
    }

    public static Transform orthographic(float left, float top, float right, float bottom, float zNear, float zFar) {
        Transform t = new Transform();
        // Scale so that (left, top, near) is mapped to (-1, 1, 1) and (right, bottom, far) to (1, -1, -1)
        t.mat.m00 = 2 / (right - left);
        t.mat.m11 = -2 / (bottom - top);
        t.mat.m22 = -2 / (zFar - zNear);
        return t;
    }

    public static Transform UO(float gridDiameter, float pc) {
        Transform res = new Transform();
        // rotate 45°, scale to grid, project z by moving y
        res.mat.m00 =  gridDiameter / 2.0f;
        res.mat.m01 =  gridDiameter / 2.0f;
        res.mat.m10 = -gridDiameter / 2.0f;
        res.mat.m11 =  gridDiameter / 2.0f;
        res.mat.m21 = -pc;
        return res;
    }
}
