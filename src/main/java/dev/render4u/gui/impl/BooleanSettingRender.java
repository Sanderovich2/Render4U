package dev.render4u.gui.impl;

import dev.render4u.config.ConfigManager;
import dev.render4u.gui.ClickGuiScreen;
import dev.render4u.module.setting.BooleanSetting;
import dev.render4u.module.setting.Setting;
import dev.render4u.render.display.Draw;
import dev.render4u.render.display.element.DrawRect;
import dev.render4u.render.font.msdf.MsdfFonts;
import dev.render4u.theme.Theme;
import dev.render4u.util.color.ColorMath;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class BooleanSettingRender extends SettingRender {
    public static final float ROW_H = 28.0f;
    public static final float SWITCH_W = 28.0f;
    public static final float SWITCH_H = 15.0f;

    public BooleanSettingRender(ClickGuiScreen gui) {
        super(gui);
    }

    @Override
    public float height(Setting<?> s, float k) {
        return ROW_H * k + 5.0f * k;
    }

    @Override
    public void render(GuiGraphicsExtractor g, Setting<?> s, float x, float y, float w, float k, float a, float mx, float my, float dt) {
        BooleanSetting bs = (BooleanSetting) s;
        float rh = ROW_H * k;

        boolean hovered = (mx >= x && mx <= x + w && my >= y && my <= y + rh);
        float hv = gui.animValue("bshov_" + bs.getName(), hovered ? 1.0f : 0.0f, dt, 14.0f);
        float tog = gui.animValue("bstog_" + bs.getName(), bs.isEnabled() ? 1.0f : 0.0f, dt, 16.0f);

        int boxBg = ColorMath.lerp(0x990F131C, 0xD01F2028, hv);
        int boxBorder = ColorMath.lerp(0x22354258, 0x4BFFFFFF, hv);
        DrawRect.create()
                .bounds(x, y, w, rh)
                .rounding(7.0f * k)
                .bg(boxBg)
                .outline(1.0f, boxBorder)
                .alpha(a)
                .render(g);

        float fontSize = 10.0f * k;
        int textCol = ColorMath.lerp(0xFFCBD5E1, 0xFFFFFFFF, hv);
        Draw.text(g, MsdfFonts.SF_MEDIUM, bs.getName(), x + 10.0f * k, y + (rh - fontSize) / 2.0f, fontSize, ColorMath.multiplyAlpha(textCol, a));

        float sw = SWITCH_W * k;
        float sh = SWITCH_H * k;
        float sx = x + w - 10.0f * k - sw;
        float sy = y + (rh - sh) / 2.0f;

        int accent = Theme.current.getFirstColor();
        int toggleOff = 0xFF0C0F16;
        int trackBg = ColorMath.lerp(toggleOff, accent, tog);
        int trackBorder = ColorMath.lerp(0x22FFFFFF, 0x00FFFFFF, tog);

        DrawRect.create()
                .bounds(sx, sy, sw, sh)
                .rounding(sh / 2.0f)
                .bg(trackBg)
                .outline(1.0f, trackBorder)
                .alpha(a)
                .render(g);

        float knob = 11.0f * k;
        float knobX = sx + 2.0f * k + (sw - knob - 4.0f * k) * tog;
        float knobY = sy + 2.0f * k;
        int knobCol = ColorMath.lerp(0xFF969CA8, 0xFFFFFFFF, tog);
        Draw.rect(g, knobX, knobY, knob, knob, knob / 2.0f, ColorMath.multiplyAlpha(knobCol, a));
    }

    @Override
    public boolean click(Setting<?> s, float mx, float my, float x, float y, float w, float k, int button) {
        BooleanSetting bs = (BooleanSetting) s;
        float rh = ROW_H * k;
        if (my >= y && my <= y + rh && mx >= x && mx <= x + w) {
            bs.toggle();
            ConfigManager.save();
            return true;
        }
        return false;
    }
}
