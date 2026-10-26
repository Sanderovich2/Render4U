package dev.render4u.render.font;

import java.awt.Font;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class TTFFont {
    private final String name;
    private final Font baseFont;
    private final Map<Float, TTFFontRenderer> renderers = new ConcurrentHashMap<>();

    public TTFFont(String name, Font baseFont) {
        this.name = name;
        this.baseFont = baseFont;
    }

    public static TTFFont fromStream(String name, InputStream stream) {
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, stream);
            return new TTFFont(name, font);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load font: " + name, e);
        }
    }

    public TTFFontRenderer getRenderer(float size) {
        return renderers.computeIfAbsent(size, s -> new TTFFontRenderer(name, baseFont, s));
    }

    public void drawString(GuiGraphicsExtractor graphics, String text, float x, float y, float size, int color) {
        getRenderer(size).drawString(graphics, text, x, y, color);
    }

    public void drawString(GuiGraphicsExtractor graphics, String text, float x, float y, int color, float size) {
        drawString(graphics, text, x, y, size, color);
    }

    public void drawStringWithShadow(GuiGraphicsExtractor graphics, String text, float x, float y, float size, int color) {
        getRenderer(size).drawStringWithShadow(graphics, text, x, y, color);
    }

    public void drawCenteredString(GuiGraphicsExtractor graphics, String text, float x, float y, float size, int color) {
        getRenderer(size).drawCenteredString(graphics, text, x, y, color);
    }

    public void drawCenteredStringWithShadow(GuiGraphicsExtractor graphics, String text, float x, float y, float size, int color) {
        getRenderer(size).drawCenteredStringWithShadow(graphics, text, x, y, color);
    }

    public float getWidth(String text, float size) {
        return getRenderer(size).getStringWidth(text);
    }

    public float getStringWidth(String text, float size) {
        return getRenderer(size).getStringWidth(text);
    }

    public float getHeight(float size) {
        return getRenderer(size).getFontHeight();
    }

    public float getFontHeight(float size) {
        return getRenderer(size).getFontHeight();
    }

    public String getName() {
        return name;
    }

    public Font getBaseFont() {
        return baseFont;
    }
}
