package dev.render4u.render.display.outline;

import dev.render4u.render.display.batch.DisplayBatcher;
import dev.render4u.util.color.Color;
import dev.render4u.util.color.Gradient;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class Outline {
    public static final float DEFAULT_THICKNESS = 1.0F;

    private float x;
    private float y;
    private float width;
    private float height;
    private float thickness = DEFAULT_THICKNESS;
    private float radiusTopLeft;
    private float radiusTopRight;
    private float radiusBottomRight;
    private float radiusBottomLeft;
    private int colorTopLeft = -1;
    private int colorTopRight = -1;
    private int colorBottomRight = -1;
    private int colorBottomLeft = -1;
    private float alpha = 1.0F;

    private Outline() {
    }

    public static Outline create() {
        return new Outline();
    }

    public static Outline of(float x, float y, float width, float height) {
        return new Outline().position(x, y).size(width, height);
    }

    public Outline position(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Outline size(float width, float height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public Outline thickness(float thickness) {
        this.thickness = Math.clamp(thickness, 0.0625F, 16.0F);
        return this;
    }

    public Outline radius(float radius) {
        return radius(radius, radius, radius, radius);
    }

    public Outline radius(float topLeft, float topRight, float bottomRight, float bottomLeft) {
        this.radiusTopLeft = topLeft;
        this.radiusTopRight = topRight;
        this.radiusBottomRight = bottomRight;
        this.radiusBottomLeft = bottomLeft;
        return this;
    }

    public Outline color(int argb) {
        return corners(argb, argb, argb, argb);
    }

    public Outline color(Color color) {
        return color(color.argb());
    }

    public Outline horizontalGradient(int left, int right) {
        return corners(left, right, right, left);
    }

    public Outline horizontalGradient(Color left, Color right) {
        return horizontalGradient(left.argb(), right.argb());
    }

    public Outline verticalGradient(int top, int bottom) {
        return corners(top, top, bottom, bottom);
    }

    public Outline verticalGradient(Color top, Color bottom) {
        return verticalGradient(top.argb(), bottom.argb());
    }

    public Outline corners(int topLeft, int topRight, int bottomRight, int bottomLeft) {
        this.colorTopLeft = topLeft;
        this.colorTopRight = topRight;
        this.colorBottomRight = bottomRight;
        this.colorBottomLeft = bottomLeft;
        return this;
    }

    public Outline corners(Color topLeft, Color topRight, Color bottomRight, Color bottomLeft) {
        return corners(topLeft.argb(), topRight.argb(), bottomRight.argb(), bottomLeft.argb());
    }

    public Outline gradient(Gradient gradient) {
        return corners(gradient.sample(0.0F, 0.0F), gradient.sample(1.0F, 0.0F), gradient.sample(1.0F, 1.0F), gradient.sample(0.0F, 1.0F));
    }

    public Outline alpha(float alpha) {
        this.alpha = Math.clamp(alpha, 0.0F, 1.0F);
        return this;
    }

    public OutlineRenderState build(GuiGraphicsExtractor graphics) {
        float maxRadius = Math.min(width, height) * 0.5F;
        int topLeft = packRadius(radiusTopLeft, maxRadius);
        int topRight = packRadius(radiusTopRight, maxRadius);
        int bottomRight = packRadius(radiusBottomRight, maxRadius);
        int bottomLeft = packRadius(radiusBottomLeft, maxRadius);
        int packedThickness = Math.clamp(Math.round(thickness * 16.0F), 1, 255);
        int packedTop = topLeft | topRight << 6 | (packedThickness & 15) << 12;
        int packedBottom = bottomLeft | bottomRight << 6 | (packedThickness >> 4) << 12;
        return new OutlineRenderState(
                DisplayBatcher.pose(graphics), x, y, width, height,
                packedTop, packedBottom,
                applyAlpha(colorTopLeft), applyAlpha(colorTopRight), applyAlpha(colorBottomRight), applyAlpha(colorBottomLeft),
                graphics.scissorStack.peek()
        );
    }

    public void render(GuiGraphicsExtractor graphics) {
        if (width <= 0.0F || height <= 0.0F || thickness <= 0.0F || alpha <= 0.0F) {
            return;
        }
        DisplayBatcher.submit(graphics, build(graphics));
    }

    private int applyAlpha(int color) {
        int a = Math.round((color >>> 24) * alpha);
        return a << 24 | color & 0xFFFFFF;
    }

    private static int packRadius(float radius, float max) {
        return Math.clamp(Math.round(Math.clamp(radius, 0.0F, max) * 2.0F), 0, 63);
    }
}

