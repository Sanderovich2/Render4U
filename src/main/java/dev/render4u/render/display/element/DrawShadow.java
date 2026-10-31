package dev.render4u.render.display.element;

import dev.render4u.render.display.shape.Shadow;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class DrawShadow {
    public float blur = 8.0F;
    public float offsetX = 0.0F;
    public float offsetY = 0.0F;
    public float spread = 0.0F;
    public float strength = 1.0F;
    public int color = 0x80000000;
    public boolean enabled = true;

    public DrawShadow() {
    }

    public DrawShadow(float blur, float offsetX, float offsetY, int color) {
        this.blur = blur;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.color = color;
    }

    public static DrawShadow of(float blur, float offsetX, float offsetY, int color) {
        return new DrawShadow(blur, offsetX, offsetY, color);
    }

    public DrawShadow blur(float blur) {
        this.blur = blur;
        return this;
    }

    public DrawShadow offset(float offsetX, float offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        return this;
    }

    public DrawShadow spread(float spread) {
        this.spread = spread;
        return this;
    }

    public DrawShadow strength(float strength) {
        this.strength = strength;
        return this;
    }

    public DrawShadow color(int color) {
        this.color = color;
        return this;
    }

    public DrawShadow enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public void render(GuiGraphicsExtractor graphics, float x, float y, float width, float height, DrawRounding rounding, float elementAlpha) {
        if (!enabled || color == 0 || width <= 0.0F || height <= 0.0F || elementAlpha <= 0.0F) {
            return;
        }

        float rTL = rounding != null ? rounding.LeftUp : 0.0F;
        float rTR = rounding != null ? rounding.RightUp : 0.0F;
        float rBR = rounding != null ? rounding.RightDown : 0.0F;
        float rBL = rounding != null ? rounding.LeftDown : 0.0F;

        Shadow.of(x, y, width, height)
                .radius(rTL, rTR, rBR, rBL)
                .offset(offsetX, offsetY)
                .blur(blur)
                .spread(spread)
                .strength(strength)
                .color(color)
                .alpha(elementAlpha)
                .render(graphics);
    }
}
