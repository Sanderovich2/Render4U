package dev.render4u.render.font.msdf;

@FunctionalInterface
public interface ColorFunction {
    int color(int glyph, float x, float y);
}
