package dev.render4u.core.event.impl;

import dev.render4u.core.event.Event;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class EventDisplay extends Event {
    private static final EventDisplay INSTANCE = new EventDisplay();

    private GuiGraphicsExtractor graphics;

    private EventDisplay() {
    }

    public static EventDisplay get(GuiGraphicsExtractor graphics) {
        INSTANCE.graphics = graphics;
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }

    public GuiGraphicsExtractor graphics() {
        return graphics;
    }
}

