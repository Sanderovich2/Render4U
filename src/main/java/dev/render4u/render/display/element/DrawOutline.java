package dev.render4u.render.display.element;

import dev.render4u.render.display.shape.Outline;
import dev.render4u.render.display.shape.RoundedRect;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class DrawOutline {
    public float Up = 0.0F;
    public float Down = 0.0F;
    public float Left = 0.0F;
    public float Right = 0.0F;

    public int color = -1;
    public int colorTopLeft = -1;
    public int colorTopRight = -1;
    public int colorBottomRight = -1;
    public int colorBottomLeft = -1;
    public float alpha = 1.0F;

    public DrawOutline() {
    }

    public DrawOutline(float thickness, int color) {
        all(thickness);
        color(color);
    }

    public static DrawOutline of(float thickness, int color) {
        return new DrawOutline(thickness, color);
    }

    public static DrawOutline of(float up, float down, float left, float right, int color) {
        return new DrawOutline().sides(up, down, left, right).color(color);
    }

    public DrawOutline all(float thickness) {
        this.Up = thickness;
        this.Down = thickness;
        this.Left = thickness;
        this.Right = thickness;
        return this;
    }

    public DrawOutline sides(float up, float down, float left, float right) {
        this.Up = up;
        this.Down = down;
        this.Left = left;
        this.Right = right;
        return this;
    }

    public DrawOutline up(float up) {
        this.Up = up;
        return this;
    }

    public DrawOutline down(float down) {
        this.Down = down;
        return this;
    }

    public DrawOutline left(float left) {
        this.Left = left;
        return this;
    }

    public DrawOutline right(float right) {
        this.Right = right;
        return this;
    }

    public DrawOutline top(float top) {
        return up(top);
    }

    public DrawOutline bottom(float bottom) {
        return down(bottom);
    }

    public DrawOutline color(int color) {
        this.color = color;
        this.colorTopLeft = color;
        this.colorTopRight = color;
        this.colorBottomRight = color;
        this.colorBottomLeft = color;
        return this;
    }

    public DrawOutline corners(int topLeft, int topRight, int bottomRight, int bottomLeft) {
        this.colorTopLeft = topLeft;
        this.colorTopRight = topRight;
        this.colorBottomRight = bottomRight;
        this.colorBottomLeft = bottomLeft;
        return this;
    }

    public DrawOutline horizontalGradient(int left, int right) {
        return corners(left, right, right, left);
    }

    public DrawOutline verticalGradient(int top, int bottom) {
        return corners(top, top, bottom, bottom);
    }

    public DrawOutline alpha(float alpha) {
        this.alpha = Math.clamp(alpha, 0.0F, 1.0F);
        return this;
    }

    public boolean isEnabled() {
        return (Up > 0.0F || Down > 0.0F || Left > 0.0F || Right > 0.0F) && alpha > 0.0F;
    }

    public boolean isUniform() {
        return Up > 0.0F && Up == Down && Down == Left && Left == Right;
    }

    public void render(GuiGraphicsExtractor graphics, float x, float y, float width, float height, DrawRounding rounding, float elementAlpha) {
        if (!isEnabled() || width <= 0.0F || height <= 0.0F) {
            return;
        }

        float totalAlpha = alpha * elementAlpha;
        if (totalAlpha <= 0.0F) {
            return;
        }

        int c = color;
        int tl = colorTopLeft != -1 ? colorTopLeft : c;
        int tr = colorTopRight != -1 ? colorTopRight : c;
        int br = colorBottomRight != -1 ? colorBottomRight : c;
        int bl = colorBottomLeft != -1 ? colorBottomLeft : c;

        float rTL = rounding != null ? rounding.LeftUp : 0.0F;
        float rTR = rounding != null ? rounding.RightUp : 0.0F;
        float rBR = rounding != null ? rounding.RightDown : 0.0F;
        float rBL = rounding != null ? rounding.LeftDown : 0.0F;

        if (isUniform()) {
            Outline.of(x, y, width, height)
                    .radius(rTL, rTR, rBR, rBL)
                    .thickness(Up)
                    .corners(tl, tr, br, bl)
                    .alpha(totalAlpha)
                    .render(graphics);
            return;
        }

        if (Up > 0.0F) {
            RoundedRect.of(x, y, width, Up)
                    .radius(rTL, rTR, 0.0F, 0.0F)
                    .corners(tl, tr, tr, tl)
                    .alpha(totalAlpha)
                    .render(graphics);
        }
        if (Down > 0.0F) {
            RoundedRect.of(x, y + height - Down, width, Down)
                    .radius(0.0F, 0.0F, rBR, rBL)
                    .corners(bl, br, br, bl)
                    .alpha(totalAlpha)
                    .render(graphics);
        }
        if (Left > 0.0F) {
            float topOffset = Up > 0.0F ? Up : 0.0F;
            float bottomOffset = Down > 0.0F ? Down : 0.0F;
            float sideH = Math.max(0.0F, height - topOffset - bottomOffset);
            if (sideH > 0.0F) {
                RoundedRect.of(x, y + topOffset, Left, sideH)
                        .radius(0.0F)
                        .corners(tl, tl, bl, bl)
                        .alpha(totalAlpha)
                        .render(graphics);
            }
        }
        if (Right > 0.0F) {
            float topOffset = Up > 0.0F ? Up : 0.0F;
            float bottomOffset = Down > 0.0F ? Down : 0.0F;
            float sideH = Math.max(0.0F, height - topOffset - bottomOffset);
            if (sideH > 0.0F) {
                RoundedRect.of(x + width - Right, y + topOffset, Right, sideH)
                        .radius(0.0F)
                        .corners(tr, tr, br, br)
                        .alpha(totalAlpha)
                        .render(graphics);
            }
        }
    }
}
