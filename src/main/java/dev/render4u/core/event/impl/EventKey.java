package dev.render4u.core.event.impl;

import dev.render4u.core.event.Event;

public class EventKey extends Event {
    private final int key;
    private final int scancode;
    private final int action;
    private final int modifiers;

    public EventKey(int key, int scancode, int action, int modifiers) {
        this.key = key;
        this.scancode = scancode;
        this.action = action;
        this.modifiers = modifiers;
    }

    public int getKey() {
        return key;
    }

    public int getScancode() {
        return scancode;
    }

    public int getAction() {
        return action;
    }

    public int getModifiers() {
        return modifiers;
    }

    public boolean isPressed() {
        return action == 1;
    }
}
