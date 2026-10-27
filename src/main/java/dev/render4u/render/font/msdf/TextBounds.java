package dev.render4u.render.font.msdf;

public record TextBounds(float x, float y, float width, float height) {
    public float right() {
        return x + width;
    }

    public float bottom() {
        return y + height;
    }

    public float centerX() {
        return x + width * 0.5F;
    }

    public float centerY() {
        return y + height * 0.5F;
    }

    public boolean contains(float px, float py) {
        return px >= x && py >= y && px < right() && py < bottom();
    }
}
