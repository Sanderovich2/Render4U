package dev.render4u.util.animation;

public final class SpringAnimation {
    private float value;
    private float target;
    private float velocity;
    private float stiffness = 180.0F;
    private float damping = 18.0F;
    private float mass = 1.0F;
    private long lastTimeMs = -1L;

    public SpringAnimation(float initialValue) {
        this.value = initialValue;
        this.target = initialValue;
    }

    public SpringAnimation(float initialValue, float stiffness, float damping) {
        this.value = initialValue;
        this.target = initialValue;
        this.stiffness = stiffness;
        this.damping = damping;
    }

    public void target(float target) {
        this.target = target;
    }

    public float update() {
        long now = System.currentTimeMillis();
        if (lastTimeMs < 0L) {
            lastTimeMs = now;
            return value;
        }
        float dt = Math.clamp((float) (now - lastTimeMs) / 1000.0F, 0.001F, 0.05F);
        lastTimeMs = now;
        return update(dt);
    }

    public float update(float dt) {
        float displacement = value - target;
        float springForce = -stiffness * displacement;
        float dampingForce = -damping * velocity;
        float acceleration = (springForce + dampingForce) / mass;

        velocity += acceleration * dt;
        value += velocity * dt;

        if (Math.abs(velocity) < 0.001F && Math.abs(displacement) < 0.001F) {
            value = target;
            velocity = 0.0F;
        }
        return value;
    }

    public float get() {
        return update();
    }

    public float getValue() {
        return value;
    }

    public float getTarget() {
        return target;
    }

    public void reset(float val) {
        this.value = val;
        this.target = val;
        this.velocity = 0.0F;
        this.lastTimeMs = -1L;
    }
}
