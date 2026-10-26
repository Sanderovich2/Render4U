package dev.render4u.render.display.shape;

import dev.render4u.util.color.Color;
import dev.render4u.util.color.Gradient;

public abstract class RectBuilder<B extends RectBuilder<B>> {
    public static final float PADDING = 1.0F;

    protected float x;
    protected float y;
    protected float width;
    protected float height;
    protected float radiusTopLeft;
    protected float radiusTopRight;
    protected float radiusBottomRight;
    protected float radiusBottomLeft;
    protected int colorTopLeft = -1;
    protected int colorTopRight = -1;
    protected int colorBottomRight = -1;
    protected int colorBottomLeft = -1;
    protected float alpha = 1.0F;

    protected RectBuilder() {
    }

    @SuppressWarnings("unchecked")
    protected final B self() {
        return (B) this;
    }

    public B position(float x, float y) {
        this.x = x;
        this.y = y;
        return self();
    }

    public B size(float width, float height) {
        this.width = width;
        this.height = height;
        return self();
    }

    public B bounds(float x, float y, float width, float height) {
        return position(x, y).size(width, height);
    }

    public B radius(float radius) {
        return radius(radius, radius, radius, radius);
    }

    public B radius(float topLeft, float topRight, float bottomRight, float bottomLeft) {
        this.radiusTopLeft = topLeft;
        this.radiusTopRight = topRight;
        this.radiusBottomRight = bottomRight;
        this.radiusBottomLeft = bottomLeft;
        return self();
    }

    public B color(int argb) {
        return corners(argb, argb, argb, argb);
    }

    public B color(Color color) {
        return color(color.argb());
    }

    public B corners(int topLeft, int topRight, int bottomRight, int bottomLeft) {
        this.colorTopLeft = topLeft;
        this.colorTopRight = topRight;
        this.colorBottomRight = bottomRight;
        this.colorBottomLeft = bottomLeft;
        return self();
    }

    public B corners(Color topLeft, Color topRight, Color bottomRight, Color bottomLeft) {
        return corners(topLeft.argb(), topRight.argb(), bottomRight.argb(), bottomLeft.argb());
    }

    public B horizontalGradient(int left, int right) {
        return corners(left, right, right, left);
    }

    public B horizontalGradient(Color left, Color right) {
        return horizontalGradient(left.argb(), right.argb());
    }

    public B verticalGradient(int top, int bottom) {
        return corners(top, top, bottom, bottom);
    }

    public B verticalGradient(Color top, Color bottom) {
        return verticalGradient(top.argb(), bottom.argb());
    }

    public B gradient(Gradient gradient) {
        return corners(gradient.sample(0.0F, 0.0F), gradient.sample(1.0F, 0.0F), gradient.sample(1.0F, 1.0F), gradient.sample(0.0F, 1.0F));
    }

    public B alpha(float alpha) {
        this.alpha = Math.clamp(alpha, 0.0F, 1.0F);
        return self();
    }

    protected int applyAlpha(int color) {
        int a = Math.round((color >>> 24) * alpha);
        return a << 24 | color & 0xFFFFFF;
    }

    protected static int packRadius(float radius, float max, float scale, int bits) {
        return Math.clamp(Math.round(Math.clamp(radius, 0.0F, max) * scale), 0, (1 << bits) - 1);
    }
}
