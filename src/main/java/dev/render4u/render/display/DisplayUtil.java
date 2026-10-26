package dev.render4u.render.display;

import dev.render4u.Render4U;
import dev.render4u.core.event.impl.EventDisplay;
import dev.render4u.render.display.batch.DisplayBatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.GameRenderState;

public final class DisplayUtil {
    public static final float SCALE = 2.0F;

    private DisplayUtil() {
    }

    public static float width() {
        return Minecraft.getInstance().getWindow().getWidth() / SCALE;
    }

    public static float height() {
        return Minecraft.getInstance().getWindow().getHeight() / SCALE;
    }

    public static void render(Minecraft minecraft, GameRenderState state) {
        GuiGraphicsExtractor graphics = new GuiGraphicsExtractor(minecraft, state.guiRenderState, 0, 0);
        graphics.nextStratum();
        graphics.pose().pushMatrix();
        graphics.pose().scale(SCALE / (float) minecraft.getWindow().getGuiScale());
        DisplayBatcher.begin(graphics);
        try {
            Render4U.getEventBus().post(EventDisplay.get(graphics));
        } finally {
            DisplayBatcher.end();
            graphics.pose().popMatrix();
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

