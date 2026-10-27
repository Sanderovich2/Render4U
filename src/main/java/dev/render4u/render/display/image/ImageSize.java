package dev.render4u.render.display.image;

public record ImageSize(int width, int height) {
    public float aspect() {
        return height > 0 ? (float) width / (float) height : 1.0F;
    }
}
