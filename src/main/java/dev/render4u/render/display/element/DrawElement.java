package dev.render4u.render.display.element;

import net.minecraft.client.gui.GuiGraphicsExtractor;

@FunctionalInterface
public interface DrawElement {
    void render(GuiGraphicsExtractor graphics, float x, float y);
}
