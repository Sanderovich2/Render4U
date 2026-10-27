package dev.render4u.util.color;

public enum BlendMode {
    NORMAL,
    ADD,
    MULTIPLY,
    SCREEN,
    OVERLAY,
    DARKEN,
    LIGHTEN,
    DIFFERENCE;

    public int blend(int baseColor, int blendColor) {
        int a1 = baseColor >>> 24;
        int r1 = baseColor >> 16 & 0xFF;
        int g1 = baseColor >> 8 & 0xFF;
        int b1 = baseColor & 0xFF;

        int a2 = blendColor >>> 24;
        int r2 = blendColor >> 16 & 0xFF;
        int g2 = blendColor >> 8 & 0xFF;
        int b2 = blendColor & 0xFF;

        int r, g, b;
        switch (this) {
            case ADD -> {
                r = Math.min(255, r1 + r2);
                g = Math.min(255, g1 + g2);
                b = Math.min(255, b1 + b2);
            }
            case MULTIPLY -> {
                r = (r1 * r2) / 255;
                g = (g1 * g2) / 255;
                b = (b1 * b2) / 255;
            }
            case SCREEN -> {
                r = 255 - ((255 - r1) * (255 - r2)) / 255;
                g = 255 - ((255 - g1) * (255 - g2)) / 255;
                b = 255 - ((255 - b1) * (255 - b2)) / 255;
            }
            case OVERLAY -> {
                r = r1 < 128 ? (2 * r1 * r2) / 255 : 255 - (2 * (255 - r1) * (255 - r2)) / 255;
                g = g1 < 128 ? (2 * g1 * g2) / 255 : 255 - (2 * (255 - g1) * (255 - g2)) / 255;
                b = b1 < 128 ? (2 * b1 * b2) / 255 : 255 - (2 * (255 - b1) * (255 - b2)) / 255;
            }
            case DARKEN -> {
                r = Math.min(r1, r2);
                g = Math.min(g1, g2);
                b = Math.min(b1, b2);
            }
            case LIGHTEN -> {
                r = Math.max(r1, r2);
                g = Math.max(g1, g2);
                b = Math.max(b1, b2);
            }
            case DIFFERENCE -> {
                r = Math.abs(r1 - r2);
                g = Math.abs(g1 - g2);
                b = Math.abs(b1 - b2);
            }
            default -> {
                float f = (float) a2 / 255.0F;
                r = Math.round(r1 + (r2 - r1) * f);
                g = Math.round(g1 + (g2 - g1) * f);
                b = Math.round(b1 + (b2 - b1) * f);
            }
        }
        int a = Math.max(a1, a2);
        return a << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
    }
}
