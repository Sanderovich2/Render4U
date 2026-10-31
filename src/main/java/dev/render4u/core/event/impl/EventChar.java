package dev.render4u.core.event.impl;

import dev.render4u.core.event.Event;

public class EventChar extends Event {
    private final String character;

    public EventChar(String character) {
        this.character = character;
    }

    public String getCharacter() {
        return character;
    }
}
