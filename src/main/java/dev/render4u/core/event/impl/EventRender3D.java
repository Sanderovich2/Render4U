package dev.render4u.core.event.impl;

import dev.render4u.core.event.Event;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.phys.Vec3;

public final class EventRender3D extends Event {
    private static final EventRender3D INSTANCE = new EventRender3D();

    private GuiGraphicsExtractor graphics;
    private Vec3 cameraPos;

    private EventRender3D() {
    }

    public static EventRender3D get(GuiGraphicsExtractor graphics, Vec3 cameraPos) {
        INSTANCE.graphics = graphics;
        INSTANCE.cameraPos = cameraPos;
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }

    public GuiGraphicsExtractor graphics() {
        return graphics;
    }

    public Vec3 cameraPos() {
        return cameraPos;
    }
}
