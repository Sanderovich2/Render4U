package dev.render4u.gui.impl;

import dev.render4u.config.ConfigManager;
import dev.render4u.gui.ClickGuiScreen;
import dev.render4u.module.setting.ColorSetting;
import dev.render4u.module.setting.Setting;
import dev.render4u.render.display.Draw;
import dev.render4u.render.font.msdf.MsdfFonts;
import dev.render4u.util.color.ColorMath;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

import java.awt.Color;

public class ColorSettingRender extends SettingRender {
    private static final Identifier HUE_TEXTURE = Identifier.fromNamespaceAndPath("render4u", "textures/gui/hue.png");
    public static final float ROW_H = 24.0f;
    public static final float EXPANDED_H = 105.0f;

    private ColorSetting open = null;
    private String activePart = "";

    private float svX, svY, svW, svH;
    private float hueX, hueY, hueW, hueH;
    private float alphaX, alphaY, alphaW, alphaH;
    private float prevX, prevY, prevW, prevH;

    public ColorSettingRender(ClickGuiScreen gui) {
        super(gui);
    }

    public boolean isOpen(ColorSetting cs) {
        return open == cs;
    }

    public void close() {
        open = null;
        activePart = "";
    }

    @Override
    public float height(Setting<?> s, float k) {
        ColorSetting cs = (ColorSetting) s;
        float openProgress = gui.getAnim("cp_open_" + cs.getName(), open == cs ? 1.0f : 0.0f);
        float extra = (EXPANDED_H + (cs.hasAlpha() ? 18.0f : 0.0f)) * k;
        return (ROW_H * k + 6.0f * k) + extra * openProgress;
    }

    private void layout(ColorSetting cs, float x, float y, float w, float k) {
        prevW = 32.0f * k;
        prevH = 14.0f * k;
        prevX = x + w - prevW;
        prevY = y + (ROW_H * k - prevH) / 2.0f;

        float topY = y + ROW_H * k + 4.0f * k;
        svX = x;
        svW = w;
        svH = 55.0f * k;
        svY = topY;

        hueX = x;
        hueW = w;
        hueY = svY + svH + 6.0f * k;
        hueH = 9.0f * k;

        alphaX = x;
        alphaW = w;
        alphaY = hueY + hueH + 6.0f * k;
        alphaH = 9.0f * k;
    }

    @Override
    public void render(GuiGraphicsExtractor g, Setting<?> s, float x, float y, float w, float k, float a, float mx, float my, float dt) {
        ColorSetting cs = (ColorSetting) s;
        layout(cs, x, y, w, k);
        Draw.text(g, MsdfFonts.SF_MEDIUM, cs.getName(), x, y + 5.0f * k, 10.0f * k, ColorMath.multiplyAlpha(0xFFE2E8F0, a));
        Draw.rect(g, prevX, prevY, prevW, prevH, 4.0f * k, ColorMath.multiplyAlpha(cs.get(), a));
        Draw.border(g, prevX, prevY, prevW, prevH, 4.0f * k, 0.8f, ColorMath.multiplyAlpha(0x44FFFFFF, a));
        float openProgress = gui.animValue("cp_open_" + cs.getName(), open == cs ? 1.0f : 0.0f, dt, 16.0f);
        if (openProgress <= 0.01f) return;

        float currentAlpha = a * openProgress;
        float[] hsb = cs.getHSB();
        int pureHue = Color.HSBtoRGB(hsb[0], 1.0f, 1.0f);
        Draw.gradH(g, svX, svY, svW, svH, 4.0f * k, ColorMath.multiplyAlpha(0xFFFFFFFF, currentAlpha), ColorMath.multiplyAlpha(pureHue, currentAlpha));
        Draw.gradV(g, svX, svY, svW, svH, 4.0f * k, ColorMath.multiplyAlpha(0x00000000, currentAlpha), ColorMath.multiplyAlpha(0xFF000000, currentAlpha));
        Draw.border(g, svX, svY, svW, svH, 4.0f * k, 0.8f, ColorMath.multiplyAlpha(0x33FFFFFF, currentAlpha));
        float curSvX = Math.max(svX, Math.min(svX + svW, svX + hsb[1] * svW));
        float curSvY = Math.max(svY, Math.min(svY + svH, svY + (1.0f - hsb[2]) * svH));
        Draw.circle(g, curSvX, curSvY, 4.5f * k, ColorMath.multiplyAlpha(0xFFFFFFFF, currentAlpha));
        Draw.circle(g, curSvX, curSvY, 3.5f * k, ColorMath.multiplyAlpha(cs.get() | 0xFF000000, currentAlpha));
        Draw.image(g, HUE_TEXTURE, hueX, hueY, hueW, hueH, 3.0f * k, ColorMath.multiplyAlpha(0xFFFFFFFF, currentAlpha));
        Draw.border(g, hueX, hueY, hueW, hueH, 3.0f * k, 0.8f, ColorMath.multiplyAlpha(0x33FFFFFF, currentAlpha));
        float curHueX = Math.max(hueX, Math.min(hueX + hueW, hueX + hsb[0] * hueW));
        Draw.rect(g, curHueX - 1.5f * k, hueY - 1.5f * k, 3.0f * k, hueH + 3.0f * k, 1.5f * k, ColorMath.multiplyAlpha(0xFFFFFFFF, currentAlpha));
        Draw.border(g, curHueX - 1.5f * k, hueY - 1.5f * k, 3.0f * k, hueH + 3.0f * k, 1.5f * k, 0.8f, ColorMath.multiplyAlpha(0x99000000, currentAlpha));
        float lastY = hueY + hueH;
        if (cs.hasAlpha()) {
            float aRatio = Math.max(0.0f, Math.min(1.0f, cs.getAlpha() / 255.0f));
            Draw.rect(g, alphaX, alphaY, alphaW, alphaH, 3.0f * k, ColorMath.multiplyAlpha(0xFF1E1E28, currentAlpha));
            Draw.gradH(g, alphaX, alphaY, alphaW, alphaH, 3.0f * k,
                    ColorMath.multiplyAlpha(cs.get() & 0x00FFFFFF, currentAlpha),
                    ColorMath.multiplyAlpha(cs.get() | 0xFF000000, currentAlpha));
            Draw.border(g, alphaX, alphaY, alphaW, alphaH, 3.0f * k, 0.8f, ColorMath.multiplyAlpha(0x33FFFFFF, currentAlpha));

            float curAlphaX = Math.max(alphaX, Math.min(alphaX + alphaW, alphaX + aRatio * alphaW));
            Draw.rect(g, curAlphaX - 1.5f * k, alphaY - 1.5f * k, 3.0f * k, alphaH + 3.0f * k, 1.5f * k, ColorMath.multiplyAlpha(0xFFFFFFFF, currentAlpha));
            Draw.border(g, curAlphaX - 1.5f * k, alphaY - 1.5f * k, 3.0f * k, alphaH + 3.0f * k, 1.5f * k, 0.8f, ColorMath.multiplyAlpha(0x99000000, currentAlpha));
            lastY = alphaY + alphaH;
        }
        String hex = cs.toHex();
        Draw.text(g, MsdfFonts.SF_MEDIUM, hex, x, lastY + 6.0f * k, 8.5f * k, ColorMath.multiplyAlpha(0xFF94A3B8, currentAlpha));
    }

    @Override
    public boolean click(Setting<?> s, float mx, float my, float x, float y, float w, float k, int button) {
        ColorSetting cs = (ColorSetting) s;
        layout(cs, x, y, w, k);
        if (my >= y && my <= y + ROW_H * k && mx >= x && mx <= x + w) {
            open = (open == cs) ? null : cs;
            activePart = "";
            return true;
        }

        if (open != cs) return false;
        if (mx >= svX && mx <= svX + svW && my >= svY && my <= svY + svH) {
            activePart = "sv";
            apply(cs, mx, my);
            return true;
        }
        if (mx >= hueX && mx <= hueX + hueW && my >= hueY && my <= hueY + hueH) {
            activePart = "hue";
            apply(cs, mx, my);
            return true;
        }
        if (cs.hasAlpha() && mx >= alphaX && mx <= alphaX + alphaW && my >= alphaY && my <= alphaY + alphaH) {
            activePart = "alpha";
            apply(cs, mx, my);
            return true;
        }

        return false;
    }

    private void apply(ColorSetting cs, float mx, float my) {
        if (cs == null || activePart.isEmpty()) return;
        float[] hsb = cs.getHSB();

        if (activePart.equals("sv")) {
            float sat = Math.max(0.0f, Math.min(1.0f, (mx - svX) / svW));
            float bri = Math.max(0.0f, Math.min(1.0f, 1.0f - (my - svY) / svH));
            cs.setHSB(hsb[0], sat, bri, cs.getAlpha() / 255.0f);
        } else if (activePart.equals("hue")) {
            float hue = Math.max(0.0f, Math.min(1.0f, (mx - hueX) / hueW));
            cs.setHSB(hue, hsb[1], hsb[2], cs.getAlpha() / 255.0f);
        } else if (activePart.equals("alpha") && cs.hasAlpha()) {
            float alpha = Math.max(0.0f, Math.min(1.0f, (mx - alphaX) / alphaW));
            cs.setHSB(hsb[0], hsb[1], hsb[2], alpha);
        }
    }

    @Override
    public void drag(Setting<?> s, float mx, float my, float x, float y, float w, float k) {
        if (open == s && !activePart.isEmpty()) {
            layout((ColorSetting) s, x, y, w, k);
            apply((ColorSetting) s, mx, my);
        }
    }

    @Override
    public void release(Setting<?> s) {
        if (!activePart.isEmpty()) {
            activePart = "";
            ConfigManager.save();
        }
    }
}
