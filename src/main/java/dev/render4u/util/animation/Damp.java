package dev.render4u.util.animation;

import dev.render4u.util.color.ColorMath;

public final class Damp {
    private Damp() {
    }

    public static float damp(float current, float target, float lambda, float dtSeconds) {
        return target + (current - target) * (float) Math.exp(-lambda * Math.max(0.0F, dtSeconds));
    }

    public static float lerp(float a, float b, float t) {
        return a + (b - a) * Math.clamp(t, 0.0F, 1.0F);
    }

    public static int dampColor(int current, int target, float lambda, float dtSeconds) {
        float factor = 1.0F - (float) Math.exp(-lambda * Math.max(0.0F, dtSeconds));
        return ColorMath.lerp(current, target, factor);
    }
}
