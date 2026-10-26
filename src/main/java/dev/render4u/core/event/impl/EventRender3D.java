package dev.render4u.core.event.impl;

import dev.render4u.core.event.Event;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.phys.Vec3;

public final class EventRender3D extends Event {
    private static final EventRender3D INSTANCE = new EventRender3D();

    private GuiGraphicsExtractor graphics;
    private Vec3 cameraPos;
    private float partialTick;

    private EventRender3D() {
    }

    public static EventRender3D get(GuiGraphicsExtractor graphics, Vec3 cameraPos, float partialTick) {
        INSTANCE.graphics = graphics;
        INSTANCE.cameraPos = cameraPos;
        INSTANCE.partialTick = partialTick;
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }

    public GuiGraphicsExtractor graphics() {
        return graphics;
    }

    public GuiGraphicsExtractor getGraphics() {
        return graphics;
    }

    public Vec3 cameraPos() {
        return cameraPos;
    }

    public float partialTick() {
        return partialTick;
    }
}
