package dev.render4u.gui.impl;

import com.mojang.blaze3d.platform.InputConstants;
import dev.render4u.config.ConfigManager;
import dev.render4u.gui.ClickGuiScreen;
import dev.render4u.module.setting.KeybindSetting;
import dev.render4u.module.setting.Setting;
import dev.render4u.render.display.Draw;
import dev.render4u.render.display.element.DrawRect;
import dev.render4u.render.font.msdf.MsdfFonts;
import dev.render4u.theme.Theme;
import dev.render4u.util.color.ColorMath;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class KeybindSettingRender extends SettingRender {
    public static final float ROW_H = 22.0f;
    private KeybindSetting binding = null;

    public KeybindSettingRender(ClickGuiScreen gui) {
        super(gui);
    }

    public boolean isBinding() {
        return binding != null;
    }

    public void setBinding(KeybindSetting ks) {
        this.binding = ks;
    }

    public void clearBinding() {
        this.binding = null;
    }

    public void onKey(int key) {
        if (binding != null) {
            if (key == InputConstants.KEY_ESCAPE || key == InputConstants.KEY_DELETE || key == InputConstants.KEY_BACKSPACE) {
                binding.set(InputConstants.UNKNOWN.getValue());
            } else {
                binding.set(key);
            }
            binding = null;
            ConfigManager.save();
        }
    }

    @Override
    public float height(Setting<?> s, float k) {
        return ROW_H * k + 6.0f * k;
    }

    @Override
    public void render(GuiGraphicsExtractor g, Setting<?> s, float x, float y, float w, float k, float a, float mx, float my, float dt) {
        KeybindSetting ks = (KeybindSetting) s;
        float rowH = ROW_H * k;

        boolean isBind = (binding == ks);
        String bindText = isBind ? "..." : ks.getKeyName();
        int accent = Theme.current.getFirstColor();

        float tw = MsdfFonts.SF_MEDIUM.width(bindText, 7.5f * k);
        float bindW = Math.max(40.0f * k, tw + 10.0f * k);
        float bindH = 16.0f * k;
        float bindX = x + w - bindW;
        float bindY = y + (rowH - bindH) / 2.0f;

        Draw.text(g, MsdfFonts.SF_MEDIUM, ks.getName(), x, y + 4.5f * k, 10.0f * k, ColorMath.multiplyAlpha(0xFFE2E8F0, a));

        boolean hovered = (mx >= bindX && mx <= bindX + bindW && my >= bindY && my <= bindY + bindH);
        float hov = gui.animValue("kb_hov_" + ks.getName(), hovered ? 1.0f : 0.0f, dt, 16.0f);

        int bBg = isBind ? ColorMath.multiplyAlpha(accent, 0.35f) : ColorMath.lerp(0xFF242B3B, 0xFF353D50, hov);
        DrawRect.create()
                .bounds(bindX, bindY, bindW, bindH)
                .rounding(4.0f * k)
                .bg(bBg)
                .outline(0.8f, isBind ? accent : 0x33FFFFFF)
                .alpha(a)
                .render(g);

        Draw.text(g, MsdfFonts.SF_MEDIUM, bindText, bindX + (bindW - tw) / 2.0f, bindY + 3.5f * k, 7.5f * k, ColorMath.multiplyAlpha(0xFFFFFFFF, a));
    }

    @Override
    public boolean click(Setting<?> s, float mx, float my, float x, float y, float w, float k, int button) {
        KeybindSetting ks = (KeybindSetting) s;
        float rowH = ROW_H * k;

        boolean isBind = (binding == ks);
        String bindText = isBind ? "..." : ks.getKeyName();
        float tw = MsdfFonts.SF_MEDIUM.width(bindText, 7.5f * k);
        float bindW = Math.max(40.0f * k, tw + 10.0f * k);
        float bindH = 16.0f * k;
        float bindX = x + w - bindW;
        float bindY = y + (rowH - bindH) / 2.0f;

        if (mx >= bindX - 2.0f * k && mx <= bindX + bindW + 2.0f * k && my >= bindY - 2.0f * k && my <= bindY + bindH + 2.0f * k) {
            boolean isLeft = (button == InputConstants.MOUSE_BUTTON_LEFT || button == 0);
            boolean isRight = (button == InputConstants.MOUSE_BUTTON_RIGHT || button == 3);
            if (isRight) {
                ks.set(InputConstants.UNKNOWN.getValue());
                binding = null;
                ConfigManager.save();
            } else if (isLeft) {
                if (binding == ks) {
                    binding = null;
                } else {
                    binding = ks;
                }
            }
            return true;
        }
        return false;
    }
}
