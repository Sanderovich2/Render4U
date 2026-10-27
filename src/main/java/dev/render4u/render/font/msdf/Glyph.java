package dev.render4u.render.font.msdf;

public record Glyph(int unicode, float advance, float left, float bottom, float right, float top,
                    float u0, float v0, float u1, float v1) {
    public boolean visible() {
        return right > left && top > bottom;
    }

    public float width() {
        return right - left;
    }

    public float height() {
        return top - bottom;
    }
}
