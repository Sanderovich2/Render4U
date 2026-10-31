package dev.render4u.gui.impl;

import dev.render4u.config.ConfigManager;
import dev.render4u.gui.ClickGuiScreen;
import dev.render4u.module.setting.MultiModeSetting;
import dev.render4u.module.setting.Setting;
import dev.render4u.render.display.Draw;
import dev.render4u.render.font.msdf.MsdfFonts;
import dev.render4u.theme.Theme;
import dev.render4u.util.color.ColorMath;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class MultiModeSettingRender extends SettingRender {
    public static final float CHIP_H = 18.0f;
    public static final float GAP = 5.0f;

    public MultiModeSettingRender(ClickGuiScreen gui) {
        super(gui);
    }

    @Override
    public float height(Setting<?> s, float k) {
        MultiModeSetting mms = (MultiModeSetting) s;
        float labelH = 14.0f * k;
        float chipH = CHIP_H * k;
        float gap = GAP * k;

        float innerW = 212.0f * k;
        float curX = 0;
        int rows = 1;

        for (String mode : mms.getModes()) {
            float tw = MsdfFonts.SF_SEMIBOLD.width(mode, 8.5f * k);
            float chipW = tw + 14.0f * k;
            if (curX + chipW > innerW && curX > 0) {
                curX = 0;
                rows++;
            }
            curX += chipW + gap;
        }

        return labelH + 4.0f * k + rows * chipH + (rows - 1) * gap + 8.0f * k;
    }

    @Override
    public void render(GuiGraphicsExtractor g, Setting<?> s, float x, float y, float w, float k, float a, float mx, float my, float dt) {
        MultiModeSetting mms = (MultiModeSetting) s;
        float labelH = 14.0f * k;
        float chipH = CHIP_H * k;
        float gap = GAP * k;
        Draw.text(g, MsdfFonts.SF_MEDIUM, mms.getName(), x, y, 10.0f * k, ColorMath.multiplyAlpha(0xFFE2E8F0, a));

        float curX = x;
        float curY = y + labelH + 4.0f * k;

        int accent = Theme.current.getFirstColor();
        for (String mode : mms.getModes()) {
            boolean isSel = mms.isSelected(mode);
            float tw = MsdfFonts.SF_SEMIBOLD.width(mode, 8.5f * k);
            float chipW = tw + 14.0f * k;

            if (curX + chipW > x + w && curX > x) {
                curX = x;
                curY += chipH + gap;
            }

            boolean hovered = (mx >= curX && mx <= curX + chipW && my >= curY && my <= curY + chipH);
            float hov = gui.animValue("mms_hov_" + mms.getName() + mode, hovered ? 1.0f : 0.0f, dt, 16.0f);
            float sel = gui.animValue("mms_sel_" + mms.getName() + mode, isSel ? 1.0f : 0.0f, dt, 16.0f);

            int chipBg = ColorMath.lerp(
                    ColorMath.lerp(0xFF2D2A3B, 0xFF3D384F, hov),
                    accent,
                    sel
            );
            int textCol = ColorMath.lerp(
                    ColorMath.lerp(0xFFD1D5DB, 0xFFFFFFFF, hov),
                    0xFF1B1333,
                    sel
            );

            Draw.rect(g, curX, curY, chipW, chipH, 5.0f * k, ColorMath.multiplyAlpha(chipBg, a));
            Draw.text(g, isSel ? MsdfFonts.SF_SEMIBOLD : MsdfFonts.SF_MEDIUM, mode, curX + 7.0f * k, curY + 4.0f * k, 8.5f * k, ColorMath.multiplyAlpha(textCol, a));

            curX += chipW + gap;
        }
    }

    @Override
    public boolean click(Setting<?> s, float mx, float my, float x, float y, float w, float k, int button) {
        MultiModeSetting mms = (MultiModeSetting) s;
        float labelH = 14.0f * k;
        float chipH = CHIP_H * k;
        float gap = GAP * k;

        float curX = x;
        float curY = y + labelH + 4.0f * k;

        for (String mode : mms.getModes()) {
            float tw = MsdfFonts.SF_SEMIBOLD.width(mode, 8.5f * k);
            float chipW = tw + 14.0f * k;

            if (curX + chipW > x + w && curX > x) {
                curX = x;
                curY += chipH + gap;
            }

            if (mx >= curX && mx <= curX + chipW && my >= curY && my <= curY + chipH) {
                mms.toggle(mode);
                ConfigManager.save();
                return true;
            }

            curX += chipW + gap;
        }
        return false;
    }
}
