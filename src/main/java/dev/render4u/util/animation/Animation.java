package dev.render4u.util.animation;

public final class Animation {
    private float from;
    private float to;
    private float value;
    private long durationMs;
    private long startTimeMs;
    private Easing easing;
    private boolean finished;

    public Animation(float initialValue, long durationMs, Easing easing) {
        this.from = initialValue;
        this.to = initialValue;
        this.value = initialValue;
        this.durationMs = Math.max(1L, durationMs);
        this.easing = easing != null ? easing : Easing.LINEAR;
        this.startTimeMs = System.currentTimeMillis();
        this.finished = true;
    }

    public Animation(long durationMs, Easing easing) {
        this(0.0F, durationMs, easing);
    }

    public static Animation of(long durationMs, Easing easing) {
        return new Animation(durationMs, easing);
    }

    public static Animation of(float initialValue, long durationMs, Easing easing) {
        return new Animation(initialValue, durationMs, easing);
    }

    public void run(float target) {
        if (Float.compare(this.to, target) == 0 && !finished) {
            return;
        }
        this.from = get();
        this.to = target;
        this.startTimeMs = System.currentTimeMillis();
        this.finished = false;
    }

    public void run(boolean state, float falseVal, float trueVal) {
        run(state ? trueVal : falseVal);
    }

    public void toggle(boolean state) {
        run(state ? 1.0F : 0.0F);
    }

    public float get() {
        if (finished) {
            return to;
        }
        long elapsed = System.currentTimeMillis() - startTimeMs;
        if (elapsed >= durationMs) {
            finished = true;
            value = to;
            return to;
        }
        float t = Math.clamp((float) elapsed / (float) durationMs, 0.0F, 1.0F);
        float eased = easing.ease(t);
        value = from + (to - from) * eased;
        return value;
    }

    public float getValue() {
        return get();
    }

    public float progress() {
        if (finished) return 1.0F;
        long elapsed = System.currentTimeMillis() - startTimeMs;
        return Math.clamp((float) elapsed / (float) durationMs, 0.0F, 1.0F);
    }

    public boolean isFinished() {
        get();
        return finished;
    }

    public void reset(float val) {
        this.from = val;
        this.to = val;
        this.value = val;
        this.finished = true;
        this.startTimeMs = System.currentTimeMillis();
    }

    public void reset() {
        reset(0.0F);
    }

    public float getTarget() {
        return to;
    }

    public long getDuration() {
        return durationMs;
    }

    public void setDuration(long durationMs) {
        this.durationMs = Math.max(1L, durationMs);
    }

    public void setEasing(Easing easing) {
        this.easing = easing != null ? easing : Easing.LINEAR;
    }
}
