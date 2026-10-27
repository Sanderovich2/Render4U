package dev.render4u.util.color;

public record GradientStop(float position, int color) implements Comparable<GradientStop> {
    public GradientStop(float position, Color color) {
        this(position, color.argb());
    }

    @Override
    public int compareTo(GradientStop other) {
        return Float.compare(this.position, other.position);
    }
}
