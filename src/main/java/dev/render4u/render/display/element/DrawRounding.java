package dev.render4u.render.display.element;

public class DrawRounding {
    public float LeftUp = 0.0F;
    public float RightUp = 0.0F;
    public float RightDown = 0.0F;
    public float LeftDown = 0.0F;

    public DrawRounding() {
    }

    public DrawRounding(float all) {
        all(all);
    }

    public DrawRounding(float leftUp, float rightUp, float rightDown, float leftDown) {
        this.LeftUp = leftUp;
        this.RightUp = rightUp;
        this.RightDown = rightDown;
        this.LeftDown = leftDown;
    }

    public static DrawRounding of(float all) {
        return new DrawRounding(all);
    }

    public static DrawRounding of(float leftUp, float rightUp, float rightDown, float leftDown) {
        return new DrawRounding(leftUp, rightUp, rightDown, leftDown);
    }

    public DrawRounding all(float radius) {
        this.LeftUp = radius;
        this.RightUp = radius;
        this.RightDown = radius;
        this.LeftDown = radius;
        return this;
    }

    public DrawRounding leftUp(float r) {
        this.LeftUp = r;
        return this;
    }

    public DrawRounding rightUp(float r) {
        this.RightUp = r;
        return this;
    }

    public DrawRounding rightDown(float r) {
        this.RightDown = r;
        return this;
    }

    public DrawRounding leftDown(float r) {
        this.LeftDown = r;
        return this;
    }

    public DrawRounding topLeft(float r) {
        return leftUp(r);
    }

    public DrawRounding topRight(float r) {
        return rightUp(r);
    }

    public DrawRounding bottomRight(float r) {
        return rightDown(r);
    }

    public DrawRounding bottomLeft(float r) {
        return leftDown(r);
    }

    public float topLeft() {
        return LeftUp;
    }

    public float topRight() {
        return RightUp;
    }

    public float bottomRight() {
        return RightDown;
    }

    public float bottomLeft() {
        return LeftDown;
    }

    public boolean isZero() {
        return LeftUp <= 0.0F && RightUp <= 0.0F && RightDown <= 0.0F && LeftDown <= 0.0F;
    }

    public boolean isUniform() {
        return LeftUp == RightUp && RightUp == RightDown && RightDown == LeftDown;
    }

    public float maxRadius() {
        return Math.max(Math.max(LeftUp, RightUp), Math.max(RightDown, LeftDown));
    }
}
