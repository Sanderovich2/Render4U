package dev.render4u.render.font;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import dev.render4u.render.display.batch.DisplayBatcher;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

public final class TTFFontRenderer {
    private static final int ATLAS_SIZE = 1024;
    private static final float OVERSAMPLE = 2.0F;

    private static final int[] MC_COLORS = new int[32];
    static {
        for (int i = 0; i < 32; i++) {
            int base = (i >> 3 & 1) * 85;
            int r = (i >> 2 & 1) * 170 + base;
            int g = (i >> 1 & 1) * 170 + base;
            int b = (i & 1) * 170 + base;
            if (i == 6) r += 85;
            if (i >= 16) {
                r /= 4;
                g /= 4;
                b /= 4;
            }
            MC_COLORS[i] = 0xFF000000 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
        }
    }

    private final String name;
    private final float size;
    private final Map<Character, Glyph> glyphMap = new HashMap<>();
    private final Identifier textureId;
    private final BufferedImage atlasImage;
    private float fontHeight;
    private volatile DynamicTexture dynamicTexture;

    public record Glyph(float x, float y, float width, float height, float u0, float v0, float u1, float v1, float advance) {
    }

    public static TTFFontRenderer create(String name, InputStream stream, float size) {
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, stream);
            return new TTFFontRenderer(name, font, size);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load TTF font: " + name, e);
        }
    }

    public TTFFontRenderer(String name, Font awtFont, float size) {
        this.name = name;
        this.size = size;
        this.textureId = Identifier.fromNamespaceAndPath("render4u", "ttf_" + name.toLowerCase().replaceAll("[^a-z0-9_]", "_") + "_" + (int) size);

        Font font = awtFont.deriveFont(Font.PLAIN, size * OVERSAMPLE);
        this.atlasImage = new BufferedImage(ATLAS_SIZE, ATLAS_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = atlasImage.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setFont(font);

        FontMetrics metrics = g2d.getFontMetrics();
        this.fontHeight = metrics.getHeight() / OVERSAMPLE;

        int curX = 2;
        int curY = 2;
        int rowHeight = 0;

        StringBuilder chars = new StringBuilder();
        for (int i = 32; i < 256; i++) chars.append((char) i);
        for (int i = 1040; i <= 1103; i++) chars.append((char) i);
        chars.append((char) 1025);
        chars.append((char) 1105);

        g2d.setColor(Color.WHITE);

        for (int i = 0; i < chars.length(); i++) {
            char c = chars.charAt(i);
            int charWidth = metrics.charWidth(c);
            int charHeight = metrics.getHeight();
            if (charWidth <= 0) charWidth = 1;

            if (curX + charWidth + 4 >= ATLAS_SIZE) {
                curX = 2;
                curY += rowHeight + 4;
                rowHeight = 0;
            }

            if (curY + charHeight + 4 >= ATLAS_SIZE) {
                break;
            }

            g2d.drawString(String.valueOf(c), curX, curY + metrics.getAscent());

            float u0 = (float) curX / ATLAS_SIZE;
            float v0 = (float) curY / ATLAS_SIZE;
            float u1 = (float) (curX + charWidth) / ATLAS_SIZE;
            float v1 = (float) (curY + charHeight) / ATLAS_SIZE;

            glyphMap.put(c, new Glyph(0, 0, (float) charWidth / OVERSAMPLE, (float) charHeight / OVERSAMPLE,
                    u0, v0, u1, v1, (float) charWidth / OVERSAMPLE));

            curX += charWidth + 4;
            if (charHeight > rowHeight) {
                rowHeight = charHeight;
            }
        }
        g2d.dispose();
    }

    private void ensureTextureUploaded() {
        if (dynamicTexture != null) return;
        synchronized (this) {
            if (dynamicTexture != null) return;

            NativeImage nativeImage = new NativeImage(NativeImage.Format.RGBA, ATLAS_SIZE, ATLAS_SIZE, false);
            for (int y = 0; y < ATLAS_SIZE; y++) {
                for (int x = 0; x < ATLAS_SIZE; x++) {
                    int argb = atlasImage.getRGB(x, y);
                    int a = (argb >> 24) & 0xFF;
                    int r = (argb >> 16) & 0xFF;
                    int g = (argb >> 8) & 0xFF;
                    int b = argb & 0xFF;
                    int abgr = (a << 24) | (b << 16) | (g << 8) | r;
                    nativeImage.setPixel(x, y, abgr);
                }
            }

            DynamicTexture tex = new DynamicTexture(() -> "TTF_" + name, nativeImage);
            Minecraft.getInstance().getTextureManager().register(textureId, tex);
            this.dynamicTexture = tex;
        }
    }

    public GpuTextureView textureView() {
        ensureTextureUploaded();
        return dynamicTexture.getTextureView();
    }

    public float getStringWidth(String text) {
        if (text == null || text.isEmpty()) return 0.0F;
        float width = 0.0F;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '§' && i + 1 < text.length()) {
                i++;
                continue;
            }
            Glyph glyph = glyphMap.get(c);
            if (glyph != null) {
                width += glyph.advance();
            } else {
                width += size * 0.5F;
            }
        }
        return width;
    }

    public float getFontHeight() {
        return fontHeight;
    }

    public void drawString(GuiGraphicsExtractor graphics, String text, float x, float y, int color) {
        renderString(graphics, text, x, y, color, false);
    }

    public void drawStringWithShadow(GuiGraphicsExtractor graphics, String text, float x, float y, int color) {
        int shadowColor = 0xFF000000 | ((color >> 16 & 0xFF) / 4) << 16 | ((color >> 8 & 0xFF) / 4) << 8 | ((color & 0xFF) / 4);
        int alpha = (color >>> 24);
        shadowColor = (alpha << 24) | (shadowColor & 0x00FFFFFF);

        renderString(graphics, text, x + 1.0F, y + 1.0F, shadowColor, true);
        renderString(graphics, text, x, y, color, false);
    }

    public void drawCenteredString(GuiGraphicsExtractor graphics, String text, float x, float y, int color) {
        drawString(graphics, text, x - getStringWidth(text) / 2.0F, y, color);
    }

    public void drawCenteredStringWithShadow(GuiGraphicsExtractor graphics, String text, float x, float y, int color) {
        drawStringWithShadow(graphics, text, x - getStringWidth(text) / 2.0F, y, color);
    }

    private void renderString(GuiGraphicsExtractor graphics, String text, float x, float y, int color, boolean shadow) {
        if (text == null || text.isEmpty()) return;

        int len = text.length();
        float[] quads = new float[len * 8];
        int[] colors = new int[len];
        int quadCount = 0;

        float cursorX = x;
        int currentColor = color;

        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;

        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            if (c == '§' && i + 1 < len) {
                int codeIndex = "0123456789abcdefklmnor".indexOf(Character.toLowerCase(text.charAt(i + 1)));
                if (codeIndex >= 0) {
                    if (codeIndex < 16) {
                        int base = MC_COLORS[shadow ? codeIndex + 16 : codeIndex];
                        currentColor = (color & 0xFF000000) | (base & 0x00FFFFFF);
                    } else if (codeIndex == 21) {
                        currentColor = color;
                    }
                }
                i++;
                continue;
            }

            Glyph glyph = glyphMap.get(c);
            if (glyph != null) {
                float gx0 = cursorX;
                float gy0 = y;
                float gx1 = cursorX + glyph.width();
                float gy1 = y + glyph.height();

                int q = quadCount * 8;
                quads[q] = gx0;
                quads[q + 1] = gy0;
                quads[q + 2] = gx1;
                quads[q + 3] = gy1;
                quads[q + 4] = glyph.u0();
                quads[q + 5] = glyph.v0();
                quads[q + 6] = glyph.u1();
                quads[q + 7] = glyph.v1();

                colors[quadCount] = currentColor;
                quadCount++;

                minX = Math.min(minX, gx0);
                minY = Math.min(minY, gy0);
                maxX = Math.max(maxX, gx1);
                maxY = Math.max(maxY, gy1);

                cursorX += glyph.advance();
            } else {
                cursorX += size * 0.5F;
            }
        }

        if (quadCount > 0) {
            TTFTextRenderState state = new TTFTextRenderState(
                    DisplayBatcher.pose(graphics),
                    textureView(),
                    quads, colors, quadCount,
                    minX, minY, maxX, maxY,
                    graphics.scissorStack.peek()
            );
            DisplayBatcher.submit(graphics, state);
        }
    }
}

