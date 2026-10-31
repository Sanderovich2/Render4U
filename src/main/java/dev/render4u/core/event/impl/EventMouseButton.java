package dev.render4u.core.event.impl;

import dev.render4u.core.event.Event;

public class EventMouseButton extends Event {
    private final int button;
    private final int action;
    private final float mouseX;
    private final float mouseY;

    public EventMouseButton(int button, int action, float mouseX, float mouseY) {
        this.button = button;
        this.action = action;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    public int getButton() {
        return button;
    }

    public int getAction() {
        return action;
    }

    public float getMouseX() {
        return mouseX;
    }

    public float getMouseY() {
        return mouseY;
    }

    public boolean isPressed() {
        return action == 1;
    }

    public boolean isReleased() {
        return action == 0;
    }
}
