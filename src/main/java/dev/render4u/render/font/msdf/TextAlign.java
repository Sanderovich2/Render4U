package dev.render4u.render.font.msdf;

public enum TextAlign {
    LEFT(0.0F),
    CENTER(0.5F),
    RIGHT(1.0F);

    private final float factor;

    TextAlign(float factor) {
        this.factor = factor;
    }

    public float factor() {
        return factor;
    }
}
