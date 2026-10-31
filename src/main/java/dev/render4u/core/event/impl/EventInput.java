package dev.render4u.core.event.impl;

import dev.render4u.core.event.Event;

public class EventInput extends Event {
    private boolean forward;
    private boolean backward;
    private boolean left;
    private boolean right;
    private boolean jump;
    private boolean sneak;
    private boolean sprint;
    private float strafeImpulse;
    private float forwardImpulse;

    public EventInput() {
    }

    public EventInput(boolean forward, boolean backward, boolean left, boolean right,
                      boolean jump, boolean sneak, boolean sprint,
                      float strafeImpulse, float forwardImpulse) {
        this.forward = forward;
        this.backward = backward;
        this.left = left;
        this.right = right;
        this.jump = jump;
        this.sneak = sneak;
        this.sprint = sprint;
        this.strafeImpulse = strafeImpulse;
        this.forwardImpulse = forwardImpulse;
    }

    public boolean isForward() {
        return forward;
    }

    public void setForward(boolean forward) {
        this.forward = forward;
    }

    public boolean isBackward() {
        return backward;
    }

    public void setBackward(boolean backward) {
        this.backward = backward;
    }

    public boolean isLeft() {
        return left;
    }

    public void setLeft(boolean left) {
        this.left = left;
    }

    public boolean isRight() {
        return right;
    }

    public void setRight(boolean right) {
        this.right = right;
    }

    public boolean isJump() {
        return jump;
    }

    public void setJump(boolean jump) {
        this.jump = jump;
    }

    public boolean isSneak() {
        return sneak;
    }

    public void setSneak(boolean sneak) {
        this.sneak = sneak;
    }

    public boolean isSprint() {
        return sprint;
    }

    public void setSprint(boolean sprint) {
        this.sprint = sprint;
    }

    public float getStrafeImpulse() {
        return strafeImpulse;
    }

    public void setStrafeImpulse(float strafeImpulse) {
        this.strafeImpulse = strafeImpulse;
    }

    public float getForwardImpulse() {
        return forwardImpulse;
    }

    public void setForwardImpulse(float forwardImpulse) {
        this.forwardImpulse = forwardImpulse;
    }
}
