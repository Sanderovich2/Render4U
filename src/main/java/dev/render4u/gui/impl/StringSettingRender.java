package dev.render4u.gui.impl;

import com.mojang.blaze3d.platform.InputConstants;
import dev.render4u.config.ConfigManager;
import dev.render4u.gui.ClickGuiScreen;
import dev.render4u.module.setting.Setting;
import dev.render4u.module.setting.StringSetting;
import dev.render4u.render.display.Draw;
import dev.render4u.render.display.element.DrawRect;
import dev.render4u.render.font.msdf.MsdfFonts;
import dev.render4u.theme.Theme;
import dev.render4u.util.color.ColorMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;

public class StringSettingRender extends SettingRender {
    public static final float BASE_H = 38.0f;
    private StringSetting focusedSetting = null;

    public StringSettingRender(ClickGuiScreen gui) {
        super(gui);
    }

    public boolean isFocused() {
        return focusedSetting != null;
    }

    public void clearFocus() {
        if (focusedSetting != null) {
            focusedSetting = null;
            ConfigManager.save();
        }
    }

    @Override
    public float height(Setting<?> s, float k) {
        return BASE_H * k;
    }

    @Override
    public void render(GuiGraphicsExtractor g, Setting<?> s, float x, float y, float w, float k, float a, float mx, float my, float dt) {
        StringSetting ss = (StringSetting) s;
        boolean focused = (focusedSetting == ss);

        float labelH = 14.0f * k;
        float boxH = 18.0f * k;
        float boxY = y + labelH + 2.0f * k;

        int accent = Theme.current.getFirstColor();

        Draw.text(g, MsdfFonts.SF_MEDIUM, ss.getName(), x, y, 10.0f * k, ColorMath.multiplyAlpha(0xFFE2E8F0, a));

        boolean hovered = (mx >= x && mx <= x + w && my >= boxY && my <= boxY + boxH);
        float hov = gui.animValue("str_hov_" + ss.getName(), (hovered || focused) ? 1.0f : 0.0f, dt, 16.0f);

        int bgCol = ColorMath.lerp(0xFF141724, 0xFF1A1F30, hov);
        int borderCol = focused ? accent : ColorMath.lerp(0x30FFFFFF, ColorMath.multiplyAlpha(accent, 0.6f), hov);

        DrawRect.create()
                .bounds(x, boxY, w, boxH)
                .rounding(4.0f * k)
                .bg(bgCol)
                .outline(1.0f * k, borderCol)
                .alpha(a)
                .render(g);

        String val = ss.get();
        float textY = boxY + 4.0f * k;
        float textPadX = 6.0f * k;
        float maxTextW = w - textPadX * 2.0f - 8.0f * k;

        if (val == null || val.isEmpty()) {
            if (focused) {
                float pulse = 0.5f + 0.5f * (float) Math.sin(System.currentTimeMillis() / 180.0);
                Draw.text(g, MsdfFonts.SF_MEDIUM, "|", x + textPadX, textY, 9.0f * k, ColorMath.multiplyAlpha(accent, pulse * a));
            } else {
                String ph = ss.getPlaceholder().isEmpty() ? "..." : ss.getPlaceholder();
                Draw.text(g, MsdfFonts.SF_MEDIUM, ph, x + textPadX, textY, 9.0f * k, ColorMath.multiplyAlpha(0xFF64748B, a));
            }
        } else {
            String renderText = val;
            while (!renderText.isEmpty() && MsdfFonts.SF_MEDIUM.width(renderText, 9.0f * k) > maxTextW) {
                renderText = renderText.substring(1);
            }
            Draw.text(g, MsdfFonts.SF_MEDIUM, renderText, x + textPadX, textY, 9.0f * k, ColorMath.multiplyAlpha(0xFFFFFFFF, a));

            if (focused) {
                float pulse = 0.5f + 0.5f * (float) Math.sin(System.currentTimeMillis() / 180.0);
                float curX = x + textPadX + MsdfFonts.SF_MEDIUM.width(renderText, 9.0f * k) + 1.0f * k;
                Draw.text(g, MsdfFonts.SF_MEDIUM, "|", curX, textY, 9.0f * k, ColorMath.multiplyAlpha(accent, pulse * a));
            }
        }
    }

    @Override
    public boolean click(Setting<?> s, float mx, float my, float x, float y, float w, float k, int button) {
        StringSetting ss = (StringSetting) s;
        float labelH = 14.0f * k;
        float boxH = 18.0f * k;
        float boxY = y + labelH + 2.0f * k;

        if (mx >= x && mx <= x + w && my >= boxY && my <= boxY + boxH) {
            focusedSetting = ss;
            return true;
        } else if (focusedSetting == ss) {
            clearFocus();
        }
        return false;
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (focusedSetting == null) return false;
        String ch = event.codepointAsString();
        if (ch != null && !ch.isEmpty()) {
            String cur = focusedSetting.get();
            if (cur == null) cur = "";
            if (cur.length() < focusedSetting.getMaxLength()) {
                focusedSetting.set(cur + ch);
                ConfigManager.save();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (focusedSetting == null) return false;
        int key = event.key();

        if (key == InputConstants.KEY_ESCAPE || key == InputConstants.KEY_RETURN) {
            clearFocus();
            return true;
        }

        if (key == InputConstants.KEY_BACKSPACE) {
            String cur = focusedSetting.get();
            if (cur != null && !cur.isEmpty()) {
                focusedSetting.set(cur.substring(0, cur.length() - 1));
                ConfigManager.save();
            }
            return true;
        }

        if (event.hasControlDown() && key == InputConstants.KEY_V) {
            String clip = getClipboardString();
            if (clip != null && !clip.isEmpty()) {
                String cur = focusedSetting.get();
                if (cur == null) cur = "";
                String combined = cur + clip;
                if (combined.length() > focusedSetting.getMaxLength()) {
                    combined = combined.substring(0, focusedSetting.getMaxLength());
                }
                focusedSetting.set(combined);
                ConfigManager.save();
            }
            return true;
        }

        if (event.hasControlDown() && key == InputConstants.KEY_C) {
            String cur = focusedSetting.get();
            if (cur != null && !cur.isEmpty()) {
                setClipboardString(cur);
            }
            return true;
        }

        return false;
    }

    private String getClipboardString() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.keyboardHandler != null) {
                return mc.keyboardHandler.getClipboard();
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    private void setClipboardString(String text) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.keyboardHandler != null) {
                mc.keyboardHandler.setClipboard(text);
            }
        } catch (Exception ignored) {
        }
    }
}
