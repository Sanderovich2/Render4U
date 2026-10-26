package dev.render4u.render.display.rounded;

import dev.render4u.render.display.batch.DisplayBatcher;
import dev.render4u.util.color.Color;
import dev.render4u.util.color.Gradient;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class RoundedRect {
    private float x;
    private float y;
    private float width;
    private float height;
    private float radiusTopLeft;
    private float radiusTopRight;
    private float radiusBottomRight;
    private float radiusBottomLeft;
    private int colorTopLeft = -1;
    private int colorTopRight = -1;
    private int colorBottomRight = -1;
    private int colorBottomLeft = -1;
    private float alpha = 1.0F;

    private RoundedRect() {
    }

    public static RoundedRect create() {
        return new RoundedRect();
    }

    public static RoundedRect of(float x, float y, float width, float height) {
        return new RoundedRect().position(x, y).size(width, height);
    }

    public static RoundedRect of(float x, float y, float width, float height, float radius) {
        return of(x, y, width, height).radius(radius);
    }

    public RoundedRect position(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public RoundedRect size(float width, float height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public RoundedRect radius(float radius) {
        return radius(radius, radius, radius, radius);
    }

    public RoundedRect radius(float topLeft, float topRight, float bottomRight, float bottomLeft) {
        this.radiusTopLeft = topLeft;
        this.radiusTopRight = topRight;
        this.radiusBottomRight = bottomRight;
        this.radiusBottomLeft = bottomLeft;
        return this;
    }

    public RoundedRect color(int argb) {
        return corners(argb, argb, argb, argb);
    }

    public RoundedRect color(Color color) {
        return color(color.argb());
    }

    public RoundedRect horizontalGradient(int left, int right) {
        return corners(left, right, right, left);
    }

    public RoundedRect horizontalGradient(Color left, Color right) {
        return horizontalGradient(left.argb(), right.argb());
    }

    public RoundedRect verticalGradient(int top, int bottom) {
        return corners(top, top, bottom, bottom);
    }

    public RoundedRect verticalGradient(Color top, Color bottom) {
        return verticalGradient(top.argb(), bottom.argb());
    }

    public RoundedRect corners(int topLeft, int topRight, int bottomRight, int bottomLeft) {
        this.colorTopLeft = topLeft;
        this.colorTopRight = topRight;
        this.colorBottomRight = bottomRight;
        this.colorBottomLeft = bottomLeft;
        return this;
    }

    public RoundedRect gradient(Gradient gradient) {
        return corners(gradient.sample(0.0F, 0.0F), gradient.sample(1.0F, 0.0F), gradient.sample(1.0F, 1.0F), gradient.sample(0.0F, 1.0F));
    }

    public RoundedRect alpha(float alpha) {
        this.alpha = Math.clamp(alpha, 0.0F, 1.0F);
        return this;
    }

    public RoundedRectRenderState build(GuiGraphicsExtractor graphics) {
        float maxRadius = Math.min(width, height) * 0.5F;
        int topLeft = packRadius(radiusTopLeft, maxRadius);
        int topRight = packRadius(radiusTopRight, maxRadius);
        int bottomRight = packRadius(radiusBottomRight, maxRadius);
        int bottomLeft = packRadius(radiusBottomLeft, maxRadius);
        return new RoundedRectRenderState(
                DisplayBatcher.pose(graphics), x, y, width, height,
                topLeft | topRight << 8, bottomLeft | bottomRight << 8,
                applyAlpha(colorTopLeft), applyAlpha(colorTopRight), applyAlpha(colorBottomRight), applyAlpha(colorBottomLeft),
                graphics.scissorStack.peek()
        );
    }

    public void render(GuiGraphicsExtractor graphics) {
        if (width <= 0.0F || height <= 0.0F || alpha <= 0.0F) {
            return;
        }
        DisplayBatcher.submit(graphics, build(graphics));
    }

    private int applyAlpha(int color) {
        int a = Math.round((color >>> 24) * alpha);
        return a << 24 | color & 0xFFFFFF;
    }

    private static int packRadius(float radius, float max) {
        return Math.clamp(Math.round(Math.clamp(radius, 0.0F, max) * 2.0F), 0, 255);
    }
}

