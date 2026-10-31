package dev.render4u.gui.impl;

import dev.render4u.config.ConfigManager;
import dev.render4u.gui.ClickGuiScreen;
import dev.render4u.module.Module;
import dev.render4u.module.setting.*;
import dev.render4u.render.display.Draw;
import dev.render4u.render.display.element.DrawRect;
import dev.render4u.render.font.msdf.MsdfFonts;
import dev.render4u.util.color.ColorMath;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;

import java.util.HashMap;
import java.util.Map;

public class SettingsPanelRender {
    private final ClickGuiScreen gui;
    private final Map<Class<? extends Setting<?>>, SettingRender> renders = new HashMap<>();

    private final BooleanSettingRender booleanRender;
    private final NumberSettingRender numberRender;
    private final ModeSettingRender modeRender;
    private final MultiModeSettingRender multiModeRender;
    private final ColorSettingRender colorRender;
    private final KeybindSettingRender keybindRender;
    private final StringSettingRender stringRender;

    private Module currentModule = null;
    private Module closingModule = null;
    private float scrollOffset = 0.0f;
    private float targetScroll = 0.0f;

    public SettingsPanelRender(ClickGuiScreen gui) {
        this.gui = gui;
        this.booleanRender = new BooleanSettingRender(gui);
        this.numberRender = new NumberSettingRender(gui);
        this.modeRender = new ModeSettingRender(gui);
        this.multiModeRender = new MultiModeSettingRender(gui);
        this.colorRender = new ColorSettingRender(gui);
        this.keybindRender = new KeybindSettingRender(gui);
        this.stringRender = new StringSettingRender(gui);

        renders.put(BooleanSetting.class, booleanRender);
        renders.put(NumberSetting.class, numberRender);
        renders.put(ModeSetting.class, modeRender);
        renders.put(MultiModeSetting.class, multiModeRender);
        renders.put(ColorSetting.class, colorRender);
        renders.put(KeybindSetting.class, keybindRender);
        renders.put(StringSetting.class, stringRender);
    }

    public Module getCurrentModule() {
        return currentModule;
    }

    public void open(Module module) {
        this.currentModule = module;
        this.closingModule = null;
        this.scrollOffset = 0.0f;
        this.targetScroll = 0.0f;
        this.colorRender.close();
    }

    public void close() {
        if (this.currentModule != null) {
            this.closingModule = this.currentModule;
        }
        this.currentModule = null;
        this.colorRender.close();
        this.keybindRender.clearBinding();
        this.stringRender.clearFocus();
        this.numberRender.clearEditing();
        ConfigManager.save();
    }

    public boolean isOpen() {
        return currentModule != null;
    }

    public boolean isBinding() {
        return keybindRender.isBinding();
    }

    public void clearBinding() {
        keybindRender.clearBinding();
    }

    public void onKey(int key) {
        keybindRender.onKey(key);
    }

    public boolean charTyped(CharacterEvent event) {
        if (currentModule == null) return false;
        if (stringRender.isFocused()) {
            return stringRender.charTyped(event);
        }
        if (numberRender.isEditing()) {
            return numberRender.charTyped(event);
        }
        return false;
    }

    public boolean keyPressed(KeyEvent event) {
        if (currentModule == null) return false;
        if (stringRender.isFocused()) {
            return stringRender.keyPressed(event);
        }
        if (numberRender.isEditing()) {
            return numberRender.keyPressed(event);
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY, float k, float screenW, float screenH) {
        if (currentModule == null) return false;

        float panelW = 240.0f * k;
        float panelH = Math.min(360.0f * k, screenH - 40.0f * k);
        float panelX = (screenW - panelW) / 2.0f;
        float panelY = (screenH - panelH) / 2.0f;

        if (mouseX >= panelX && mouseX <= panelX + panelW && mouseY >= panelY && mouseY <= panelY + panelH) {
            targetScroll -= (float) scrollY * 26.0f;
            targetScroll = Math.max(0.0f, targetScroll);
            return true;
        }
        return false;
    }

    public void render(GuiGraphicsExtractor g, float k, float screenW, float screenH, float winAlpha, float mx, float my, float dt) {
        float anim = gui.animValue("settings_panel_open", currentModule != null ? 1.0f : 0.0f, dt, 18.0f);
        if (anim <= 0.01f || winAlpha <= 0.005f) {
            closingModule = null;
            return;
        }
        Module mod = currentModule != null ? currentModule : closingModule;
        if (mod == null) return;

        float easeA = ClickGuiScreen.easeOutCubic(anim) * winAlpha;
        float scaleM = 0.95f + 0.05f * easeA;

        float scrollFactor = 1.0f - (float) Math.exp(-16.0 * dt);
        scrollOffset += (targetScroll - scrollOffset) * scrollFactor;

        float panelW = 240.0f * k * scaleM;
        float panelH = Math.min(360.0f * k, screenH - 40.0f * k) * scaleM;
        float panelX = (screenW - panelW) / 2.0f;
        float slide = (1.0f - easeA) * 14.0f * k;
        float panelY = (screenH - panelH) / 2.0f + slide;
        Draw.rect(g, 0, 0, screenW, screenH, 0, ColorMath.multiplyAlpha(0x66000000, easeA));
        DrawRect.create()
                .bounds(panelX, panelY, panelW, panelH)
                .rounding(14.0f * k)
                .bg(0xF813131B)
                .shadow(24.0f * k, 0.0f, 0.0f, 0xDD000000)
                .outline(1.2f, 0x333A384F)
                .alpha(easeA)
                .render(g);
        String title = mod.getName();
        float titleW = MsdfFonts.SF_BOLD.width(title, 12.0f * k);
        Draw.text(g, MsdfFonts.SF_BOLD, title, panelX + (panelW - titleW) / 2.0f, panelY + 11.0f * k, 12.0f * k, ColorMath.multiplyAlpha(0xFFFFFFFF, easeA));
        float closeSize = 18.0f * k;
        float closeX = panelX + panelW - 14.0f * k - closeSize;
        float closeY = panelY + 9.0f * k;
        boolean closeHovered = (mx >= closeX && mx <= closeX + closeSize && my >= closeY && my <= closeY + closeSize);
        int closeCol = ColorMath.multiplyAlpha(closeHovered ? 0xFFFFFFFF : 0xFF8892B0, easeA);

        float cx = closeX + closeSize / 2.0f;
        float cy = closeY + closeSize / 2.0f;
        float cr = 3.5f * k;
        Draw.line(g, cx - cr, cy - cr, cx + cr, cy + cr, 1.4f * k, closeCol);
        Draw.line(g, cx + cr, cy - cr, cx - cr, cy + cr, 1.4f * k, closeCol);
        Draw.rect(g, panelX + 12.0f * k, panelY + 34.0f * k, panelW - 24.0f * k, 1.0f, 0.5f, ColorMath.multiplyAlpha(0x22FFFFFF, easeA));
        float clipTop = panelY + 36.0f * k;
        float clipBottom = panelY + panelH - 10.0f * k;
        g.enableScissor((int) panelX, (int) clipTop, (int) (panelX + panelW), (int) clipBottom);

        float panelPadX = 14.0f * k;
        float panelInnerW = panelW - panelPadX * 2.0f;
        float setCurY = panelY + 42.0f * k - scrollOffset;

        float totalHeight = 0.0f;
        for (Setting<?> setting : mod.getSettings()) {
            if (!setting.isVisible()) continue;
            SettingRender sr = getRender(setting);
            if (sr == null) continue;

            float sh = sr.height(setting, k);
            totalHeight += sh;
            sr.render(g, setting, panelX + panelPadX, setCurY, panelInnerW, k, easeA, mx, my, dt);
            setCurY += sh;
        }

        float maxScroll = Math.max(0.0f, totalHeight - (panelH - 50.0f * k));
        targetScroll = Math.max(0.0f, Math.min(maxScroll, targetScroll));

        g.disableScissor();
    }

    public boolean mouseClicked(float mx, float my, int button, float k, float screenW, float screenH) {
        if (currentModule == null) return false;

        float panelW = 240.0f * k;
        float panelH = Math.min(360.0f * k, screenH - 40.0f * k);
        float panelX = (screenW - panelW) / 2.0f;
        float panelY = (screenH - panelH) / 2.0f;
        if (mx < panelX || mx > panelX + panelW || my < panelY || my > panelY + panelH) {
            close();
            return true;
        }
        float closeSize = 18.0f * k;
        float closeX = panelX + panelW - 14.0f * k - closeSize;
        float closeY = panelY + 9.0f * k;
        if (mx >= closeX && mx <= closeX + closeSize && my >= closeY && my <= closeY + closeSize) {
            close();
            return true;
        }
        float panelPadX = 14.0f * k;
        float panelInnerW = panelW - panelPadX * 2.0f;
        float setCurY = panelY + 42.0f * k - scrollOffset;

        for (Setting<?> setting : currentModule.getSettings()) {
            if (!setting.isVisible()) continue;
            SettingRender sr = getRender(setting);
            if (sr == null) continue;

            float sh = sr.height(setting, k);
            if (sr.click(setting, mx, my, panelX + panelPadX, setCurY, panelInnerW, k, button)) {
                return true;
            }
            setCurY += sh;
        }

        return true;
    }

    public void mouseDragged(float mx, float my, float k, float screenW, float screenH) {
        if (currentModule == null) return;

        float panelW = 240.0f * k;
        float panelH = Math.min(360.0f * k, screenH - 40.0f * k);
        float panelX = (screenW - panelW) / 2.0f;
        float panelY = (screenH - panelH) / 2.0f;

        float panelPadX = 14.0f * k;
        float panelInnerW = panelW - panelPadX * 2.0f;
        float setCurY = panelY + 42.0f * k - scrollOffset;

        for (Setting<?> setting : currentModule.getSettings()) {
            if (!setting.isVisible()) continue;
            SettingRender sr = getRender(setting);
            if (sr == null) continue;

            float sh = sr.height(setting, k);
            sr.drag(setting, mx, my, panelX + panelPadX, setCurY, panelInnerW, k);
            setCurY += sh;
        }
    }

    public void mouseReleased() {
        if (currentModule == null) return;
        for (Setting<?> setting : currentModule.getSettings()) {
            SettingRender sr = getRender(setting);
            if (sr != null) {
                sr.release(setting);
            }
        }
    }

    private SettingRender getRender(Setting<?> setting) {
        for (Map.Entry<Class<? extends Setting<?>>, SettingRender> entry : renders.entrySet()) {
            if (entry.getKey().isInstance(setting)) {
                return entry.getValue();
            }
        }
        return null;
    }
}
