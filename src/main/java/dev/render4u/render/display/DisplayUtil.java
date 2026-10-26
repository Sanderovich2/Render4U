package dev.render4u.render.display;

import dev.render4u.Render4U;
import dev.render4u.core.event.impl.EventDisplay;
import dev.render4u.core.event.impl.EventRender3D;
import dev.render4u.render.display.batch.DisplayBatcher;
import dev.render4u.render.world.Render3D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.GameRenderState;

public final class DisplayUtil {
    public static final float SCALE = 2.0F;

    private static double lastFrameMs;

    private DisplayUtil() {
    }

    public static float width() {
        return Minecraft.getInstance().getWindow().getWidth() / SCALE;
    }

    public static float height() {
        return Minecraft.getInstance().getWindow().getHeight() / SCALE;
    }

    public static double lastFrameMs() {
        return lastFrameMs;
    }

    public static void render(Minecraft minecraft, GameRenderState state) {
        long start = System.nanoTime();
        GuiGraphicsExtractor graphics = new GuiGraphicsExtractor(minecraft, state.guiRenderState, 0, 0);
        graphics.nextStratum();
        graphics.pose().pushMatrix();
        graphics.pose().scale(SCALE / (float) minecraft.getWindow().getGuiScale());
        DisplayBatcher.begin(graphics);
        try {
            Render3D.beginFrame(minecraft);
            Render4U.getEventBus().post(EventDisplay.get(graphics));
            float partialTick = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true);
            Render4U.getEventBus().post(EventRender3D.get(graphics, Render3D.getCameraPos(), partialTick));
        } finally {
            Render3D.endFrame();
            DisplayBatcher.end();
            graphics.pose().popMatrix();
            lastFrameMs = (System.nanoTime() - start) / 1.0E6;
        }
    }

    public static void flush() {
        DisplayBatcher.flush();
    }

    public static void nextStratum(GuiGraphicsExtractor graphics) {
        DisplayBatcher.flush();
        graphics.nextStratum();
    }
}
