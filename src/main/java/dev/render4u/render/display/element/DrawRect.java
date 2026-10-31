package dev.render4u.render.display.element;

import dev.render4u.render.display.blur.Blur;
import dev.render4u.render.display.shape.RoundedRect;
import dev.render4u.render.display.shape.Shadow;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class DrawRect implements DrawElement {
    public float x = 0.0F;
    public float y = 0.0F;
    public float width = 0.0F;
    public float height = 0.0F;

    public int bg = 0;
    public int bgTopLeft = 0;
    public int bgTopRight = 0;
    public int bgBottomRight = 0;
    public int bgBottomLeft = 0;
    public boolean hasBg = false;

    public int blur = 0;
    public int blurTint = 0;

    public DrawOutline outline;
    public DrawRounding rounding;
    public DrawShadow shadow;

    public float alpha = 1.0F;

    public DrawRect() {
    }

    public DrawRect(float width, float height) {
        size(width, height);
    }

    public static DrawRect create() {
        return new DrawRect();
    }

    public static DrawRect of(float width, float height) {
        return new DrawRect(width, height);
    }

    public static DrawRect of(float x, float y, float width, float height) {
        return new DrawRect(width, height).position(x, y);
    }

    public DrawRect size(float width, float height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public DrawRect position(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public DrawRect bounds(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        return this;
    }

    public DrawRect bg(int color) {
        this.bg = color;
        this.bgTopLeft = color;
        this.bgTopRight = color;
        this.bgBottomRight = color;
        this.bgBottomLeft = color;
        this.hasBg = color != 0;
        return this;
    }

    public DrawRect bgGradientH(int left, int right) {
        this.bgTopLeft = left;
        this.bgBottomLeft = left;
        this.bgTopRight = right;
        this.bgBottomRight = right;
        this.hasBg = true;
        return this;
    }

    public DrawRect bgGradientV(int top, int bottom) {
        this.bgTopLeft = top;
        this.bgTopRight = top;
        this.bgBottomLeft = bottom;
        this.bgBottomRight = bottom;
        this.hasBg = true;
        return this;
    }

    public DrawRect bgCorners(int topLeft, int topRight, int bottomRight, int bottomLeft) {
        this.bgTopLeft = topLeft;
        this.bgTopRight = topRight;
        this.bgBottomRight = bottomRight;
        this.bgBottomLeft = bottomLeft;
        this.hasBg = true;
        return this;
    }

    public DrawRect blur(int strength) {
        this.blur = strength;
        return this;
    }

    public DrawRect blur(int strength, int tint) {
        this.blur = strength;
        this.blurTint = tint;
        return this;
    }

    public DrawRect outline(DrawOutline outline) {
        this.outline = outline;
        return this;
    }

    public DrawRect outline(float thickness, int color) {
        this.outline = new DrawOutline(thickness, color);
        return this;
    }

    public DrawRect outline(float up, float down, float left, float right, int color) {
        this.outline = new DrawOutline().sides(up, down, left, right).color(color);
        return this;
    }

    public DrawRect rounding(DrawRounding rounding) {
        this.rounding = rounding;
        return this;
    }

    public DrawRect rounding(float radius) {
        this.rounding = new DrawRounding(radius);
        return this;
    }

    public DrawRect rounding(float leftUp, float rightUp, float rightDown, float leftDown) {
        this.rounding = new DrawRounding(leftUp, rightUp, rightDown, leftDown);
        return this;
    }

    public DrawRect shadow(DrawShadow shadow) {
        this.shadow = shadow;
        return this;
    }

    public DrawRect shadow(float blur, float offsetX, float offsetY, int color) {
        this.shadow = new DrawShadow(blur, offsetX, offsetY, color);
        return this;
    }

    public DrawRect alpha(float alpha) {
        this.alpha = Math.clamp(alpha, 0.0F, 1.0F);
        return this;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, float posX, float posY) {
        render(graphics, posX, posY, this.width, this.height);
    }

    public void render(GuiGraphicsExtractor graphics) {
        render(graphics, this.x, this.y, this.width, this.height);
    }

    public void render(GuiGraphicsExtractor graphics, float posX, float posY, float overrideWidth, float overrideHeight) {
        this.x = posX;
        this.y = posY;
        if (overrideWidth <= 0.0F || overrideHeight <= 0.0F || alpha <= 0.0F) {
            return;
        }

        float rTL = rounding != null ? rounding.LeftUp : 0.0F;
        float rTR = rounding != null ? rounding.RightUp : 0.0F;
        float rBR = rounding != null ? rounding.RightDown : 0.0F;
        float rBL = rounding != null ? rounding.LeftDown : 0.0F;

        if (shadow != null && shadow.enabled) {
            shadow.render(graphics, posX, posY, overrideWidth, overrideHeight, rounding, alpha);
        }

        if (blur > 0) {
            Blur b = Blur.of(posX, posY, overrideWidth, overrideHeight)
                    .radius(rTL, rTR, rBR, rBL)
                    .strength(blur)
                    .alpha(alpha);
            if (blurTint != 0) {
                b.tint(blurTint);
            }
            b.render(graphics);
        }

        boolean shouldRenderBg = hasBg || bg != 0;
        if (shouldRenderBg) {
            int tl = bgTopLeft != 0 ? bgTopLeft : bg;
            int tr = bgTopRight != 0 ? bgTopRight : bg;
            int br = bgBottomRight != 0 ? bgBottomRight : bg;
            int bl = bgBottomLeft != 0 ? bgBottomLeft : bg;

            RoundedRect.of(posX, posY, overrideWidth, overrideHeight)
                    .radius(rTL, rTR, rBR, rBL)
                    .corners(tl, tr, br, bl)
                    .alpha(alpha)
                    .render(graphics);
        }

        if (outline != null && outline.isEnabled()) {
            outline.render(graphics, posX, posY, overrideWidth, overrideHeight, rounding, alpha);
        }
    }

    public void drawShadow(GuiGraphicsExtractor graphics, float blur, float offsetX, float offsetY) {
        drawShadow(graphics, this.x, this.y, blur, offsetX, offsetY, 0x80000000);
    }

    public void drawShadow(GuiGraphicsExtractor graphics, float blur, float offsetX, float offsetY, int color) {
        drawShadow(graphics, this.x, this.y, blur, offsetX, offsetY, color);
    }

    public void drawShadow(GuiGraphicsExtractor graphics, float posX, float posY, float blur, float offsetX, float offsetY) {
        drawShadow(graphics, posX, posY, blur, offsetX, offsetY, 0x80000000);
    }

    public void drawShadow(GuiGraphicsExtractor graphics, float posX, float posY, float blur, float offsetX, float offsetY, int color) {
        if (width <= 0.0F || height <= 0.0F || alpha <= 0.0F) {
            return;
        }

        float rTL = rounding != null ? rounding.LeftUp : 0.0F;
        float rTR = rounding != null ? rounding.RightUp : 0.0F;
        float rBR = rounding != null ? rounding.RightDown : 0.0F;
        float rBL = rounding != null ? rounding.LeftDown : 0.0F;

        Shadow.of(posX, posY, width, height)
                .radius(rTL, rTR, rBR, rBL)
                .offset(offsetX, offsetY)
                .blur(blur)
                .color(color)
                .alpha(alpha)
                .render(graphics);
    }
}
