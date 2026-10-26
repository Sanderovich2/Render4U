package dev.render4u.util.color;

import java.util.Arrays;

public final class Gradient {
    public enum Mode {
        CLAMP,
        REPEAT,
        MIRROR
    }

    private final int[] colors;
    private final float[] stops;
    private final float angle;
    private final float offset;
    private final Mode mode;
    private final float directionX;
    private final float directionY;
    private final float scale;

    private Gradient(int[] colors, float[] stops, float angle, float offset, Mode mode) {
        this.colors = colors;
        this.stops = stops;
        this.angle = angle;
        this.offset = offset;
        this.mode = mode;
        double radians = Math.toRadians(angle);
        this.directionX = (float) Math.cos(radians);
        this.directionY = (float) Math.sin(radians);
        float extent = Math.abs(directionX) + Math.abs(directionY);
        this.scale = extent > 1.0E-6F ? 1.0F / extent : 1.0F;
    }

    public static Gradient of(int... colors) {
        if (colors.length == 0) {
            throw new IllegalArgumentException("Gradient requires at least one color");
        }
        float[] stops = new float[colors.length];
        for (int i = 0; i < stops.length; i++) {
            stops[i] = colors.length == 1 ? 0.0F : (float) i / (colors.length - 1);
        }
        return new Gradient(colors.clone(), stops, 0.0F, 0.0F, Mode.CLAMP);
    }

    public static Gradient of(Color... colors) {
        return of(Arrays.stream(colors).mapToInt(Color::argb).toArray());
    }

    public static Gradient stops(float[] stops, int[] colors) {
        if (colors.length == 0 || stops.length != colors.length) {
            throw new IllegalArgumentException("Gradient stops and colors must have the same non-zero length");
        }
        for (int i = 1; i < stops.length; i++) {
            if (stops[i] < stops[i - 1]) {
                throw new IllegalArgumentException("Gradient stops must be ascending");
            }
        }
        return new Gradient(colors.clone(), stops.clone(), 0.0F, 0.0F, Mode.CLAMP);
    }

    public Gradient angle(float degrees) {
        return new Gradient(colors, stops, degrees, offset, mode);
    }

    public Gradient offset(float offset) {
        return new Gradient(colors, stops, angle, offset, mode);
    }

    public Gradient mode(Mode mode) {
        return new Gradient(colors, stops, angle, offset, mode);
    }

    public int sample(float progress) {
        float p = wrap(progress + offset);
        int last = colors.length - 1;
        if (last == 0 || p <= stops[0]) {
            return colors[0];
        }
        if (p >= stops[last]) {
            return colors[last];
        }
        int i = 1;
        while (stops[i] < p) {
            i++;
        }
        float span = stops[i] - stops[i - 1];
        return ColorUtil.lerp(colors[i - 1], colors[i], span <= 0.0F ? 1.0F : (p - stops[i - 1]) / span);
    }

    public int sample(float x, float y) {
        return sample(((x - 0.5F) * directionX + (y - 0.5F) * directionY) * scale + 0.5F);
    }

    private float wrap(float value) {
        return switch (mode) {
            case CLAMP -> Math.clamp(value, 0.0F, 1.0F);
            case REPEAT -> value - (float) Math.floor(value);
            case MIRROR -> {
                float m = value - 2.0F * (float) Math.floor(value * 0.5F);
                yield m > 1.0F ? 2.0F - m : m;
            }
        };
    }
}

