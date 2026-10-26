package dev.render4u.util.color;

public final class ColorUtil {

    private ColorUtil() {
    }

    public static int parseHex(String hex) {
        if (hex == null || hex.isEmpty()) {
            throw new IllegalArgumentException("Hex color string cannot be null or empty");
        }

        String clean = hex.trim();
        if (clean.startsWith("#")) {
            clean = clean.substring(1);
        } else if (clean.startsWith("0x") || clean.startsWith("0X")) {
            clean = clean.substring(2);
        }

        int len = clean.length();
        if (len == 3) {
            char r = clean.charAt(0);
            char g = clean.charAt(1);
            char b = clean.charAt(2);
            clean = "" + r + r + g + g + b + b;
            len = 6;
        } else if (len == 4) {
            char a = clean.charAt(0);
            char r = clean.charAt(1);
            char g = clean.charAt(2);
            char b = clean.charAt(3);
            clean = "" + a + a + r + r + g + g + b + b;
            len = 8;
        }

        if (len == 6) {
            int rgb = (int) Long.parseLong(clean, 16);
            return 0xFF000000 | (rgb & 0x00FFFFFF);
        } else if (len == 8) {
            return (int) Long.parseLong(clean, 16);
        }

        throw new IllegalArgumentException("Invalid hex color length (" + len + "): " + hex);
    }

    public static Color hex(String hex) {
        return new Color(parseHex(hex));
    }

    public static String toHex(int argb) {
        return String.format("#%08X", argb);
    }

    public static String toRgbHex(int argb) {
        return String.format("#%06X", argb & 0x00FFFFFF);
    }

    public static String toHex(Color color) {
        return toHex(color.argb());
    }

    public static String toRgbHex(Color color) {
        return toRgbHex(color.argb());
    }

    public static int rgb(int r, int g, int b) {
        return 0xFF000000 | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    public static int rgba(int r, int g, int b, int a) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    public static int rgb(float r, float g, float b) {
        return rgb(Math.round(r * 255.0F), Math.round(g * 255.0F), Math.round(b * 255.0F));
    }

    public static int rgba(float r, float g, float b, float a) {
        return rgba(Math.round(r * 255.0F), Math.round(g * 255.0F), Math.round(b * 255.0F), Math.round(a * 255.0F));
    }

    public static Color colorRgb(int r, int g, int b) {
        return Color.rgb(r, g, b);
    }

    public static Color colorRgba(int r, int g, int b, int a) {
        return Color.rgba(r, g, b, a);
    }

    public static int getAlpha(int argb) {
        return argb >>> 24;
    }

    public static int getRed(int argb) {
        return (argb >> 16) & 0xFF;
    }

    public static int getGreen(int argb) {
        return (argb >> 8) & 0xFF;
    }

    public static int getBlue(int argb) {
        return argb & 0xFF;
    }

    public static int withAlpha(int argb, int alpha) {
        return (argb & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }

    public static int multiplyAlpha(int argb, float factor) {
        if (factor >= 1.0F) {
            return argb;
        }
        int a = Math.round((argb >>> 24) * Math.clamp(factor, 0.0F, 1.0F));
        return a << 24 | argb & 0xFFFFFF;
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

    private static int mix(int from, int to, float progress) {
        return Math.round(from + (to - from) * progress);
    }

    public static int hsb(float hue, float saturation, float brightness) {
        return hsb(hue, saturation, brightness, 1.0F);
    }

    public static int hsb(float hue, float saturation, float brightness, float alpha) {
        int r = 0, g = 0, b = 0;
        if (saturation == 0) {
            r = g = b = (int) (brightness * 255.0F + 0.5F);
        } else {
            float h = (hue - (float) Math.floor(hue)) * 6.0F;
            float f = h - (float) Math.floor(h);
            float p = brightness * (1.0F - saturation);
            float q = brightness * (1.0F - saturation * f);
            float t = brightness * (1.0F - (saturation * (1.0F - f)));
            switch ((int) h) {
                case 0 -> {
                    r = (int) (brightness * 255.0F + 0.5F);
                    g = (int) (t * 255.0F + 0.5F);
                    b = (int) (p * 255.0F + 0.5F);
                }
                case 1 -> {
                    r = (int) (q * 255.0F + 0.5F);
                    g = (int) (brightness * 255.0F + 0.5F);
                    b = (int) (p * 255.0F + 0.5F);
                }
                case 2 -> {
                    r = (int) (p * 255.0F + 0.5F);
                    g = (int) (brightness * 255.0F + 0.5F);
                    b = (int) (t * 255.0F + 0.5F);
                }
                case 3 -> {
                    r = (int) (p * 255.0F + 0.5F);
                    g = (int) (q * 255.0F + 0.5F);
                    b = (int) (brightness * 255.0F + 0.5F);
                }
                case 4 -> {
                    r = (int) (t * 255.0F + 0.5F);
                    g = (int) (p * 255.0F + 0.5F);
                    b = (int) (brightness * 255.0F + 0.5F);
                }
                case 5 -> {
                    r = (int) (brightness * 255.0F + 0.5F);
                    g = (int) (p * 255.0F + 0.5F);
                    b = (int) (q * 255.0F + 0.5F);
                }
            }
        }
        int a = Math.round(alpha * 255.0F);
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    public static int rainbow(float speed, float saturation, float brightness, long offsetMs) {
        float hue = ((System.currentTimeMillis() + offsetMs) % (long) (speed * 1000.0F)) / (speed * 1000.0F);
        return hsb(hue, saturation, brightness);
    }
}
