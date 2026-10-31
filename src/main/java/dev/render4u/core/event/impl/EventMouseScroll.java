package dev.render4u.core.event.impl;

import dev.render4u.core.event.Event;

public class EventMouseScroll extends Event {
    private final double scrollX;
    private final double scrollY;

    public EventMouseScroll(double scrollX, double scrollY) {
        this.scrollX = scrollX;
        this.scrollY = scrollY;
    }

    public double getScrollX() {
        return scrollX;
    }

    public double getScrollY() {
        return scrollY;
    }
}
