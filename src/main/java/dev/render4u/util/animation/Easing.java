package dev.render4u.util.animation;

@FunctionalInterface
public interface Easing {
    float ease(float t);

    Easing LINEAR = t -> t;

    Easing EASE_IN_QUAD = t -> t * t;
    Easing EASE_OUT_QUAD = t -> t * (2.0F - t);
    Easing EASE_IN_OUT_QUAD = t -> t < 0.5F ? 2.0F * t * t : -1.0F + (4.0F - 2.0F * t) * t;

    Easing EASE_IN_CUBIC = t -> t * t * t;
    Easing EASE_OUT_CUBIC = t -> {
        float f = t - 1.0F;
        return f * f * f + 1.0F;
    };
    Easing EASE_IN_OUT_CUBIC = t -> t < 0.5F ? 4.0F * t * t * t : (t - 1.0F) * (2.0F * t - 2.0F) * (2.0F * t - 2.0F) + 1.0F;

    Easing EASE_IN_QUART = t -> t * t * t * t;
    Easing EASE_OUT_QUART = t -> {
        float f = t - 1.0F;
        return 1.0F - f * f * f * f;
    };
    Easing EASE_IN_OUT_QUART = t -> {
        if (t < 0.5F) return 8.0F * t * t * t * t;
        float f = t - 1.0F;
        return 1.0F - 8.0F * f * f * f * f;
    };

    Easing EASE_IN_QUINT = t -> t * t * t * t * t;
    Easing EASE_OUT_QUINT = t -> {
        float f = t - 1.0F;
        return f * f * f * f * f + 1.0F;
    };
    Easing EASE_IN_OUT_QUINT = t -> {
        if (t < 0.5F) return 16.0F * t * t * t * t * t;
        float f = 2.0F * t - 2.0F;
        return 0.5F * f * f * f * f * f + 1.0F;
    };

    Easing EASE_IN_SINE = t -> 1.0F - (float) Math.cos(t * Math.PI * 0.5);
    Easing EASE_OUT_SINE = t -> (float) Math.sin(t * Math.PI * 0.5);
    Easing EASE_IN_OUT_SINE = t -> -0.5F * ((float) Math.cos(Math.PI * t) - 1.0F);

    Easing EASE_IN_EXPO = t -> t == 0.0F ? 0.0F : (float) Math.pow(2.0, 10.0 * (t - 1.0));
    Easing EASE_OUT_EXPO = t -> t == 1.0F ? 1.0F : 1.0F - (float) Math.pow(2.0, -10.0 * t);
    Easing EASE_IN_OUT_EXPO = t -> {
        if (t == 0.0F) return 0.0F;
        if (t == 1.0F) return 1.0F;
        if (t < 0.5F) return 0.5F * (float) Math.pow(2.0, 20.0 * t - 10.0);
        return 1.0F - 0.5F * (float) Math.pow(2.0, -20.0 * t + 10.0);
    };

    Easing EASE_IN_CIRC = t -> 1.0F - (float) Math.sqrt(Math.max(0.0, 1.0 - t * t));
    Easing EASE_OUT_CIRC = t -> (float) Math.sqrt(Math.max(0.0, 1.0 - (t - 1.0F) * (t - 1.0F)));
    Easing EASE_IN_OUT_CIRC = t -> {
        if (t < 0.5F) return 0.5F * (1.0F - (float) Math.sqrt(Math.max(0.0, 1.0 - 4.0 * t * t)));
        return 0.5F * ((float) Math.sqrt(Math.max(0.0, 1.0 - Math.pow(-2.0 * t + 2.0, 2.0))) + 1.0F);
    };

    Easing EASE_IN_BACK = t -> {
        float c1 = 1.70158F;
        float c3 = c1 + 1.0F;
        return c3 * t * t * t - c1 * t * t;
    };
    Easing EASE_OUT_BACK = t -> {
        float c1 = 1.70158F;
        float c3 = c1 + 1.0F;
        float f = t - 1.0F;
        return 1.0F + c3 * f * f * f + c1 * f * f;
    };
    Easing EASE_IN_OUT_BACK = t -> {
        float c1 = 1.70158F;
        float c2 = c1 * 1.525F;
        return t < 0.5F
                ? (float) (Math.pow(2.0 * t, 2.0) * ((c2 + 1.0) * 2.0 * t - c2)) * 0.5F
                : (float) (Math.pow(2.0 * t - 2.0, 2.0) * ((c2 + 1.0) * (t * 2.0 - 2.0) + c2) + 2.0) * 0.5F;
    };

    Easing EASE_OUT_ELASTIC = t -> {
        if (t <= 0.0F) return 0.0F;
        if (t >= 1.0F) return 1.0F;
        float c4 = (float) (2.0 * Math.PI) / 3.0F;
        return (float) (Math.pow(2.0, -10.0 * t) * Math.sin((t * 10.0F - 0.75F) * c4)) + 1.0F;
    };

    Easing EASE_OUT_BOUNCE = t -> {
        float n1 = 7.5625F;
        float d1 = 2.75F;
        if (t < 1.0F / d1) {
            return n1 * t * t;
        } else if (t < 2.0F / d1) {
            float f = t - 1.5F / d1;
            return n1 * f * f + 0.75F;
        } else if (t < 2.5F / d1) {
            float f = t - 2.25F / d1;
            return n1 * f * f + 0.9375F;
        } else {
            float f = t - 2.625F / d1;
            return n1 * f * f + 0.984375F;
        }
    };
    Easing EASE_IN_BOUNCE = t -> 1.0F - EASE_OUT_BOUNCE.ease(1.0F - t);
}
