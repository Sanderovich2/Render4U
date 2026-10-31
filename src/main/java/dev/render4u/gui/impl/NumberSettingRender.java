package dev.render4u.gui.impl;

import com.mojang.blaze3d.platform.InputConstants;
import dev.render4u.config.ConfigManager;
import dev.render4u.gui.ClickGuiScreen;
import dev.render4u.module.setting.NumberSetting;
import dev.render4u.module.setting.Setting;
import dev.render4u.render.display.Draw;
import dev.render4u.render.display.element.DrawRect;
import dev.render4u.render.font.msdf.MsdfFonts;
import dev.render4u.theme.Theme;
import dev.render4u.util.color.ColorMath;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;

public class NumberSettingRender extends SettingRender {
    public static final float BASE_H = 32.0f;
    private NumberSetting dragging = null;
    private NumberSetting editingSetting = null;
    private String inputBuffer = "";

    public NumberSettingRender(ClickGuiScreen gui) {
        super(gui);
    }

    public boolean isEditing() {
        return editingSetting != null;
    }

    public void clearEditing() {
        if (editingSetting != null) {
            applyInput();
            editingSetting = null;
        }
    }

    private void applyInput() {
        if (editingSetting == null) return;
        try {
            double parsed = Double.parseDouble(inputBuffer.replace(',', '.').trim());
            editingSetting.set(parsed);
            ConfigManager.save();
        } catch (Exception ignored) {
        }
    }

    @Override
    public float height(Setting<?> s, float k) {
        return BASE_H * k;
    }

    @Override
    public void render(GuiGraphicsExtractor g, Setting<?> s, float x, float y, float w, float k, float a, float mx, float my, float dt) {
        NumberSetting ns = (NumberSetting) s;
        float labelH = 14.0f * k;
        float sliderH = 6.0f * k;
        float slX = x;
        float slY = y + labelH + 4.0f * k;

        int accent = Theme.current.getFirstColor();
        Draw.text(g, MsdfFonts.SF_MEDIUM, ns.getName(), x, y, 10.0f * k, ColorMath.multiplyAlpha(0xFFE2E8F0, a));

        boolean editing = (editingSetting == ns);
        String valStr = editing ? inputBuffer : ns.getFormattedValue();
        float valW = Math.max(16.0f * k, MsdfFonts.SF_MEDIUM.width(valStr, 9.5f * k));

        float valBoxX = x + w - valW - 4.0f * k;
        float valBoxY = y - 1.5f * k;
        float valBoxW = valW + 8.0f * k;
        float valBoxH = 13.0f * k;

        boolean valHovered = (mx >= valBoxX && mx <= valBoxX + valBoxW && my >= valBoxY && my <= valBoxY + valBoxH);

        if (editing) {
            DrawRect.create()
                    .bounds(valBoxX, valBoxY, valBoxW, valBoxH)
                    .rounding(3.0f * k)
                    .bg(0xFF1E2235)
                    .outline(0.9f * k, accent)
                    .alpha(a)
                    .render(g);

            float pulse = 0.5f + 0.5f * (float) Math.sin(System.currentTimeMillis() / 180.0);
            float curX = valBoxX + 4.0f * k + MsdfFonts.SF_MEDIUM.width(valStr, 9.5f * k);
            Draw.text(g, MsdfFonts.SF_MEDIUM, valStr, valBoxX + 4.0f * k, y, 9.5f * k, ColorMath.multiplyAlpha(0xFFFFFFFF, a));
            Draw.text(g, MsdfFonts.SF_MEDIUM, "|", curX + 1.0f * k, y, 9.5f * k, ColorMath.multiplyAlpha(accent, pulse * a));
        } else {
            if (valHovered) {
                Draw.rect(g, valBoxX, valBoxY, valBoxW, valBoxH, 3.0f * k, ColorMath.multiplyAlpha(0x18FFFFFF, a));
            }
            Draw.text(g, MsdfFonts.SF_MEDIUM, valStr, x + w - valW, y, 9.5f * k, ColorMath.multiplyAlpha(valHovered ? 0xFFFFFFFF : accent, a));
        }

        Draw.rect(g, slX, slY, w, sliderH, 3.0f * k, ColorMath.multiplyAlpha(0xFF22222E, a));

        float targetT = (float) ((ns.get() - ns.getMin()) / (ns.getMax() - ns.getMin()));
        targetT = Math.max(0.0f, Math.min(1.0f, targetT));
        float t = gui.animValue("slider_t_" + ns.getName(), targetT, dt, 18.0f);
        Draw.rect(g, slX, slY, w * t, sliderH, 3.0f * k, ColorMath.multiplyAlpha(accent, a));

        boolean hovered = (mx >= slX - 4.0f * k && mx <= slX + w + 4.0f * k && my >= slY - 4.0f * k && my <= slY + sliderH + 4.0f * k);
        float dragAnim = gui.animValue("slider_drag_" + ns.getName(), (dragging == ns || hovered) ? 1.0f : 0.0f, dt, 16.0f);
        float knobSize = (9.0f + dragAnim * 2.0f) * k;

        float knobX = slX + w * t;
        float knobY = slY + sliderH / 2.0f;
        DrawRect.create()
                .bounds(knobX - knobSize / 2.0f, knobY - knobSize / 2.0f, knobSize, knobSize)
                .rounding(knobSize / 2.0f)
                .bg(0xFFFFFFFF)
                .outline(1.0f * k, accent)
                .alpha(a)
                .render(g);
    }

    @Override
    public boolean click(Setting<?> s, float mx, float my, float x, float y, float w, float k, int button) {
        NumberSetting ns = (NumberSetting) s;
        float labelH = 14.0f * k;
        float sliderH = 6.0f * k;
        float slY = y + labelH + 4.0f * k;

        String valStr = (editingSetting == ns) ? inputBuffer : ns.getFormattedValue();
        float valW = Math.max(16.0f * k, MsdfFonts.SF_MEDIUM.width(valStr, 9.5f * k));
        float valBoxX = x + w - valW - 4.0f * k;
        float valBoxY = y - 1.5f * k;
        float valBoxW = valW + 8.0f * k;
        float valBoxH = 13.0f * k;

        if (mx >= valBoxX && mx <= valBoxX + valBoxW && my >= valBoxY && my <= valBoxY + valBoxH) {
            editingSetting = ns;
            double val = ns.get() != null ? ns.get() : 0.0;
            inputBuffer = ns.getPrecision() == 0 ? String.valueOf((long) val) : String.valueOf(val);
            return true;
        } else if (editingSetting == ns) {
            clearEditing();
        }

        if (mx >= x - 4.0f * k && mx <= x + w + 4.0f * k && my >= slY - 4.0f * k && my <= slY + sliderH + 6.0f * k) {
            clearEditing();
            dragging = ns;
            updateValue(ns, mx, x, w);
            return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (editingSetting == null) return false;
        String ch = event.codepointAsString();
        if (ch != null && !ch.isEmpty()) {
            char c = ch.charAt(0);
            if (Character.isDigit(c) || c == '.' || c == ',' || c == '-') {
                inputBuffer += ch;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (editingSetting == null) return false;
        int key = event.key();
        if (key == InputConstants.KEY_RETURN || key == InputConstants.KEY_NUMPADENTER) {
            clearEditing();
            return true;
        }
        if (key == InputConstants.KEY_ESCAPE) {
            editingSetting = null;
            return true;
        }
        if (key == InputConstants.KEY_BACKSPACE) {
            if (!inputBuffer.isEmpty()) {
                inputBuffer = inputBuffer.substring(0, inputBuffer.length() - 1);
            }
            return true;
        }
        return false;
    }

    @Override
    public void drag(Setting<?> s, float mx, float my, float x, float y, float w, float k) {
        if (dragging == s) {
            updateValue((NumberSetting) s, mx, x, w);
        }
    }

    @Override
    public void release(Setting<?> s) {
        if (dragging == s) {
            dragging = null;
            ConfigManager.save();
        }
    }

    private void updateValue(NumberSetting ns, float mx, float x, float w) {
        if (w <= 0) return;
        float t = Math.max(0.0f, Math.min(1.0f, (mx - x) / w));
        ns.set(ns.getMin() + t * (ns.getMax() - ns.getMin()));
    }
}
