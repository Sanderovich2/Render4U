package dev.render4u.render.font;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class Font {
    private final String name;
    private final java.awt.Font baseFont;
    private final Map<Float, FontRenderer> renderers = new ConcurrentHashMap<>();

    public Font(String name, java.awt.Font baseFont) {
        this.name = name;
        this.baseFont = baseFont;
    }

    public static Font fromStream(String name, InputStream stream) {
        try {
            java.awt.Font font = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, stream);
            return new Font(name, font);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load font: " + name, e);
        }
    }

    public FontRenderer getRenderer(float size) {
        return renderers.computeIfAbsent(size, s -> new FontRenderer(name, baseFont, s));
    }

    public void drawString(GuiGraphicsExtractor graphics, String text, float x, float y, float size, int color) {
        getRenderer(size).drawString(graphics, text, x, y, color);
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

    public float getStringWidth(String text, float size) {
        return getRenderer(size).getStringWidth(text);
    }

    public float getFontHeight(float size) {
        return getRenderer(size).getFontHeight();
    }

    public String getName() {
        return name;
    }

    public java.awt.Font getBaseFont() {
        return baseFont;
    }
}
