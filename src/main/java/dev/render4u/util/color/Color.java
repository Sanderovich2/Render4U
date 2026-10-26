package dev.render4u.util.color;

public record Color(int argb) {
    public static final Color WHITE = of(0xFFFFFFFF);
    public static final Color BLACK = of(0xFF000000);
    public static final Color TRANSPARENT = of(0x00000000);

    public static Color hex(String hex) {
        return ColorUtil.hex(hex);
    }

    public static Color of(int argb) {
        return new Color(argb);
    }

    public static Color rgb(int rgb) {
        return of(0xFF000000 | rgb);
    }

    public static Color rgb(int r, int g, int b) {
        return rgba(r, g, b, 255);
    }

    public static Color rgba(int r, int g, int b, int a) {
        return of((a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF));
    }

    public static Color rgba(float r, float g, float b, float a) {
        return rgba(Math.round(r * 255.0F), Math.round(g * 255.0F), Math.round(b * 255.0F), Math.round(a * 255.0F));
    }

    public int alpha() {
        return argb >>> 24;
    }

    public int red() {
        return argb >> 16 & 0xFF;
    }

    public int green() {
        return argb >> 8 & 0xFF;
    }

    public int blue() {
        return argb & 0xFF;
    }

    public Color alpha(float alpha) {
        return of(ColorMath.multiplyAlpha(argb, alpha));
    }

    public String toHex() {
        return ColorUtil.toHex(argb);
    }

    public String toRgbHex() {
        return ColorUtil.toRgbHex(argb);
    }
}

