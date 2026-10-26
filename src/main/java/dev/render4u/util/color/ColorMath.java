package dev.render4u.util.color;

public final class ColorMath {
    private ColorMath() {
    }

    public static int lerp(int from, int to, float progress) {
        if (progress <= 0.0F) return from;
        if (progress >= 1.0F) return to;
        int a = mix(from >>> 24, to >>> 24, progress);
        int r = mix(from >> 16 & 0xFF, to >> 16 & 0xFF, progress);
        int g = mix(from >> 8 & 0xFF, to >> 8 & 0xFF, progress);
        int b = mix(from & 0xFF, to & 0xFF, progress);
        return a << 24 | r << 16 | g << 8 | b;
    }

    public static int bilerp(int topLeft, int topRight, int bottomRight, int bottomLeft, float x, float y) {
        return lerp(lerp(topLeft, topRight, x), lerp(bottomLeft, bottomRight, x), y);
    }

    public static int multiplyAlpha(int color, float factor) {
        if (factor >= 1.0F) {
            return color;
        }
        int a = Math.round((color >>> 24) * Math.clamp(factor, 0.0F, 1.0F));
        return a << 24 | color & 0xFFFFFF;
    }

    private static int mix(int from, int to, float progress) {
        return Math.round(from + (to - from) * progress);
    }
}

