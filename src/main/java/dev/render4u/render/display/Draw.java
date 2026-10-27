package dev.render4u.render.display;

import com.mojang.authlib.GameProfile;
import dev.render4u.render.display.blur.Blur;
import dev.render4u.render.display.head.PlayerHead;
import dev.render4u.render.display.image.Image;
import dev.render4u.render.display.shape.Circle;
import dev.render4u.render.display.shape.Line;
import dev.render4u.render.display.shape.Outline;
import dev.render4u.render.display.shape.RoundedRect;
import dev.render4u.render.display.shape.Shadow;
import dev.render4u.render.font.msdf.MsdfFont;
import dev.render4u.render.font.msdf.MsdfFonts;
import dev.render4u.render.font.msdf.Text;
import dev.render4u.render.font.msdf.TextAlign;
import dev.render4u.render.font.ttf.TTFFont;
import dev.render4u.render.font.ttf.TTFFontManager;
import dev.render4u.util.color.Color;
import dev.render4u.util.color.ColorMath;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;

public final class Draw {
    public static final int FLOW_MS = 2600;

    private Draw() {
    }

    public static void rect(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius, int color) {
        RoundedRect.of(x, y, w, h).radius(radius).color(color).render(g);
    }

    public static void rect(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius,
                            int topLeft, int topRight, int bottomRight, int bottomLeft) {
        RoundedRect.of(x, y, w, h).radius(radius).corners(topLeft, topRight, bottomRight, bottomLeft).render(g);
    }

    public static void gradH(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius, int left, int right) {
        RoundedRect.of(x, y, w, h).radius(radius).horizontalGradient(left, right).render(g);
    }

    public static void gradV(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius, int top, int bottom) {
        RoundedRect.of(x, y, w, h).radius(radius).verticalGradient(top, bottom).render(g);
    }

    public static void border(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius, float thickness, int color) {
        Outline.of(x, y, w, h).radius(radius).thickness(thickness).color(color).render(g);
    }

    public static void borderH(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius,
                               float thickness, int left, int right) {
        Outline.of(x, y, w, h).radius(radius).thickness(thickness).horizontalGradient(left, right).render(g);
    }

    public static void borderV(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius,
                               float thickness, int top, int bottom) {
        Outline.of(x, y, w, h).radius(radius).thickness(thickness).verticalGradient(top, bottom).render(g);
    }

    public static void shadow(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius, float blur, int color) {
        Shadow.of(x, y, w, h).radius(radius).blur(blur).color(color).render(g);
    }

    public static void glow(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius,
                            float blur, float spread, int color) {
        Shadow.of(x, y, w, h).radius(radius).blur(blur).spread(spread).color(color).render(g);
    }

    public static void blur(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius, int tint) {
        Blur.of(x, y, w, h).radius(radius).strength(3).tint(tint).render(g);
    }

    public static void blur(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius, int strength, int tint) {
        Blur.of(x, y, w, h).radius(radius).strength(strength).tint(tint).render(g);
    }

    public static void flowH(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius, int from, int to) {
        float phase = phase();
        int a = ColorMath.lerp(from, to, wave(phase));
        int b = ColorMath.lerp(from, to, wave(phase + 0.5F));
        RoundedRect.of(x, y, w, h).radius(radius).horizontalGradient(a, b).render(g);
    }

    public static void flowV(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius, int from, int to) {
        float phase = phase();
        int a = ColorMath.lerp(from, to, wave(phase));
        int b = ColorMath.lerp(from, to, wave(phase + 0.5F));
        RoundedRect.of(x, y, w, h).radius(radius).verticalGradient(a, b).render(g);
    }

    public static void flowBorderH(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius,
                                   float thickness, int from, int to) {
        float phase = phase();
        int a = ColorMath.lerp(from, to, wave(phase));
        int b = ColorMath.lerp(from, to, wave(phase + 0.5F));
        Outline.of(x, y, w, h).radius(radius).thickness(thickness).horizontalGradient(a, b).render(g);
    }

    private static float phase() {
        return (float) (System.currentTimeMillis() % FLOW_MS) / (float) FLOW_MS;
    }

    private static float wave(float value) {
        return 0.5F - 0.5F * (float) Math.cos(value * (float) (Math.PI * 2.0));
    }

    public static void text(GuiGraphicsExtractor g, MsdfFont font, String text, float x, float y, float size, int color) {
        Text.of(font, text, x, y, size).color(color).render(g);
    }

    public static void text(GuiGraphicsExtractor g, String text, float x, float y, float size, int color) {
        Text.of(MsdfFonts.defaultFont(), text, x, y, size).color(color).render(g);
    }

    public static void textCentered(GuiGraphicsExtractor g, MsdfFont font, String text, float centerX, float y, float size, int color) {
        Text.of(font, text, centerX, y, size).align(TextAlign.CENTER).color(color).render(g);
    }

    public static void textCentered(GuiGraphicsExtractor g, String text, float centerX, float y, float size, int color) {
        Text.of(MsdfFonts.defaultFont(), text, centerX, y, size).align(TextAlign.CENTER).color(color).render(g);
    }

    public static void textWithShadow(GuiGraphicsExtractor g, MsdfFont font, String text, float x, float y, float size, int color, int shadowColor, float offsetX, float offsetY) {
        Text.of(font, text, x, y, size).color(color).shadow(shadowColor, offsetX, offsetY).render(g);
    }

    public static void textWithOutline(GuiGraphicsExtractor g, MsdfFont font, String text, float x, float y, float size, int color, int outlineColor, float width) {
        Text.of(font, text, x, y, size).color(color).outline(outlineColor, width).render(g);
    }

    public static void textWithGlow(GuiGraphicsExtractor g, MsdfFont font, String text, float x, float y, float size, int color, int glowColor, float blur) {
        Text.of(font, text, x, y, size).color(color).glow(glowColor, blur).render(g);
    }

    public static void textGradH(GuiGraphicsExtractor g, MsdfFont font, String text, float x, float y, float size, int left, int right) {
        Text.of(font, text, x, y, size).horizontalGradient(left, right).render(g);
    }

    public static float textWidth(MsdfFont font, String text, float size) {
        return font.width(text, size);
    }

    public static float textHeight(MsdfFont font, float size) {
        return font.lineHeight(size);
    }

    public static void ttf(GuiGraphicsExtractor g, TTFFont font, String text, float x, float y, float size, int color) {
        if (font != null) {
            font.drawString(g, text, x, y, size, color);
        }
    }

    public static void ttfWithShadow(GuiGraphicsExtractor g, TTFFont font, String text, float x, float y, float size, int color) {
        if (font != null) {
            font.drawStringWithShadow(g, text, x, y, size, color);
        }
    }

    public static void ttfCentered(GuiGraphicsExtractor g, TTFFont font, String text, float x, float y, float size, int color) {
        if (font != null) {
            font.drawCenteredString(g, text, x, y, size, color);
        }
    }

    public static float ttfWidth(TTFFont font, String text, float size) {
        return font != null ? font.getStringWidth(text, size) : 0.0F;
    }

    public static float ttfHeight(TTFFont font, float size) {
        return font != null ? font.getFontHeight(size) : 0.0F;
    }

    public static void image(GuiGraphicsExtractor g, Identifier id, float x, float y, float w, float h, float radius, int color) {
        Image.of(id, x, y, w, h).radius(radius).color(color).render(g);
    }

    public static void imageFit(GuiGraphicsExtractor g, Identifier id, float x, float y, float w, float h, float radius, int color) {
        Image.of(id, x, y, w, h).radius(radius).contain().color(color).render(g);
    }

    public static void gif(GuiGraphicsExtractor g, Identifier id, float x, float y, float w, float h, float radius, int color) {
        Image.ofGif(id, x, y, w, h).radius(radius).color(color).render(g);
    }

    public static void head(GuiGraphicsExtractor g, GameProfile profile, float x, float y, float size, float radius) {
        PlayerHead.of(x, y, size).profile(profile).radius(radius).render(g);
    }

    public static void head(GuiGraphicsExtractor g, PlayerSkin skin, float x, float y, float size, float radius) {
        PlayerHead.of(x, y, size).skin(skin).radius(radius).render(g);
    }

    public static void headSelf(GuiGraphicsExtractor g, float x, float y, float size, float radius) {
        PlayerHead.of(x, y, size).self().radius(radius).render(g);
    }

    public static void headCircle(GuiGraphicsExtractor g, GameProfile profile, float x, float y, float size) {
        PlayerHead.of(x, y, size).profile(profile).circle().render(g);
    }

    public static void headCircleSelf(GuiGraphicsExtractor g, float x, float y, float size) {
        PlayerHead.of(x, y, size).self().circle().render(g);
    }

    public static void line(GuiGraphicsExtractor g, float x1, float y1, float x2, float y2, float thickness, int color) {
        Line.of(x1, y1, x2, y2).thickness(thickness).color(color).render(g);
    }

    public static void lineGrad(GuiGraphicsExtractor g, float x1, float y1, float x2, float y2, float thickness, int startColor, int endColor) {
        Line.of(x1, y1, x2, y2).thickness(thickness).gradient(startColor, endColor).render(g);
    }

    public static void circle(GuiGraphicsExtractor g, float centerX, float centerY, float radius, int color) {
        Circle.of(centerX, centerY, radius).color(color).render(g);
    }

    public static void circleRing(GuiGraphicsExtractor g, float centerX, float centerY, float radius, float thickness, int color) {
        Circle.of(centerX, centerY, radius).innerRadius(radius - thickness).color(color).render(g);
    }

    public static void circleArc(GuiGraphicsExtractor g, float centerX, float centerY, float radius, float thickness, float startAngle, float endAngle, int color) {
        Circle.of(centerX, centerY, radius).innerRadius(radius - thickness).angle(startAngle, endAngle).color(color).render(g);
    }

    public static Color color(int argb) {
        return new Color(argb);
    }
}
