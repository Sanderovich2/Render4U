package dev.render4u.render.display.shadow;

import dev.render4u.render.display.batch.DisplayBatcher;
import dev.render4u.util.color.Color;
import dev.render4u.util.color.Gradient;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class Shadow {
    public static final float DEFAULT_BLUR = 8.0F;

    private float x;
    private float y;
    private float width;
    private float height;
    private float offsetX;
    private float offsetY;
    private float spread;
    private float blur = DEFAULT_BLUR;
    private float strength = 1.0F;
    private float radiusTopLeft;
    private float radiusTopRight;
    private float radiusBottomRight;
    private float radiusBottomLeft;
    private int colorTopLeft = 0x80000000;
    private int colorTopRight = 0x80000000;
    private int colorBottomRight = 0x80000000;
    private int colorBottomLeft = 0x80000000;
    private float alpha = 1.0F;

    private Shadow() {
    }

    public static Shadow create() {
        return new Shadow();
    }

    public static Shadow of(float x, float y, float width, float height) {
        return new Shadow().position(x, y).size(width, height);
    }

    public Shadow position(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Shadow size(float width, float height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public Shadow offset(float offsetX, float offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        return this;
    }

    public Shadow spread(float spread) {
        this.spread = spread;
        return this;
    }

    public Shadow blur(float blur) {
        this.blur = Math.max(blur, 0.0F);
        return this;
    }

    public Shadow strength(float strength) {
        this.strength = Math.clamp(strength, 1.0F, 4.0F);
        return this;
    }

    public Shadow radius(float radius) {
        return radius(radius, radius, radius, radius);
    }

    public Shadow radius(float topLeft, float topRight, float bottomRight, float bottomLeft) {
        this.radiusTopLeft = topLeft;
        this.radiusTopRight = topRight;
        this.radiusBottomRight = bottomRight;
        this.radiusBottomLeft = bottomLeft;
        return this;
    }

    public Shadow color(int argb) {
        return corners(argb, argb, argb, argb);
    }

    public Shadow color(Color color) {
        return color(color.argb());
    }

    public Shadow horizontalGradient(int left, int right) {
        return corners(left, right, right, left);
    }

    public Shadow horizontalGradient(Color left, Color right) {
        return horizontalGradient(left.argb(), right.argb());
    }

    public Shadow verticalGradient(int top, int bottom) {
        return corners(top, top, bottom, bottom);
    }

    public Shadow verticalGradient(Color top, Color bottom) {
        return verticalGradient(top.argb(), bottom.argb());
    }

    public Shadow corners(int topLeft, int topRight, int bottomRight, int bottomLeft) {
        this.colorTopLeft = topLeft;
        this.colorTopRight = topRight;
        this.colorBottomRight = bottomRight;
        this.colorBottomLeft = bottomLeft;
        return this;
    }

    public Shadow gradient(Gradient gradient) {
        return corners(gradient.sample(0.0F, 0.0F), gradient.sample(1.0F, 0.0F), gradient.sample(1.0F, 1.0F), gradient.sample(0.0F, 1.0F));
    }

    public Shadow alpha(float alpha) {
        this.alpha = Math.clamp(alpha, 0.0F, 1.0F);
        return this;
    }

    public ShadowRenderState build(GuiGraphicsExtractor graphics) {
        float drawX = x + offsetX - spread;
        float drawY = y + offsetY - spread;
        float drawWidth = width + spread * 2.0F;
        float drawHeight = height + spread * 2.0F;
        float maxRadius = Math.min(drawWidth, drawHeight) * 0.5F;
        int topLeft = packRadius(radiusTopLeft + spread, maxRadius);
        int topRight = packRadius(radiusTopRight + spread, maxRadius);
        int bottomRight = packRadius(radiusBottomRight + spread, maxRadius);
        int bottomLeft = packRadius(radiusBottomLeft + spread, maxRadius);
        int packedBlur = Math.clamp(Math.round(blur * 2.0F), 0, 127);
        int packedStrength = Math.clamp(Math.round((strength - 1.0F) * 5.0F), 0, 15);
        int paramsTop = topLeft | topRight << 5 | bottomRight << 10;
        int paramsBottom = bottomLeft | packedBlur << 5 | packedStrength << 12;
        return new ShadowRenderState(
                DisplayBatcher.pose(graphics), drawX, drawY, drawWidth, drawHeight, blur,
                paramsTop, paramsBottom,
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
        return Math.clamp(Math.round(Math.clamp(radius, 0.0F, max)), 0, 31);
    }
}

