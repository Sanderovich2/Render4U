package dev.render4u.render.font.msdf;

import dev.render4u.util.color.Color;

public record TextEffect(int color, float offsetX, float offsetY, float spread, float blur) {
    public static TextEffect shadow(int argb, float offsetX, float offsetY) {
        return shadow(argb, offsetX, offsetY, 0.0F);
    }

    public static TextEffect shadow(int argb, float offsetX, float offsetY, float blur) {
        return new TextEffect(argb, offsetX, offsetY, 0.0F, Math.max(blur, 0.0F));
    }

    public static TextEffect shadow(Color color, float offsetX, float offsetY) {
        return shadow(color.argb(), offsetX, offsetY);
    }

    public static TextEffect shadow(Color color, float offsetX, float offsetY, float blur) {
        return shadow(color.argb(), offsetX, offsetY, blur);
    }

    public static TextEffect outline(int argb, float width) {
        return new TextEffect(argb, 0.0F, 0.0F, Math.max(width, 0.0F), 0.0F);
    }

    public static TextEffect outline(Color color, float width) {
        return outline(color.argb(), width);
    }

    public static TextEffect glow(int argb, float blur) {
        float safe = Math.max(blur, 0.0F);
        return new TextEffect(argb, 0.0F, 0.0F, safe * 0.5F, safe);
    }

    public static TextEffect glow(Color color, float blur) {
        return glow(color.argb(), blur);
    }

    public TextEffect withColor(int argb) {
        return new TextEffect(argb, offsetX, offsetY, spread, blur);
    }

    public TextEffect withColor(Color color) {
        return withColor(color.argb());
    }

    public float reach() {
        return Math.max(Math.abs(offsetX), Math.abs(offsetY)) + spread + blur;
    }
}
