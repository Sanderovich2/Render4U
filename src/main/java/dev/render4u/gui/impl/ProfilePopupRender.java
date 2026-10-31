package dev.render4u.gui.impl;

import dev.render4u.config.ConfigManager;
import dev.render4u.gui.ClickGuiScreen;
import dev.render4u.render.display.Draw;
import dev.render4u.render.display.element.DrawRect;
import dev.render4u.render.font.msdf.MsdfFonts;
import dev.render4u.theme.Theme;
import dev.render4u.util.Lang;
import dev.render4u.util.color.ColorMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class ProfilePopupRender {
    public static final float POP_W = 246.0f;
    public static final float POP_H = 182.0f;
    public static final float POP_PAD = 14.0f;

    public static final float POP_ROW_Y = 48.0f;
    public static final float POP_ROW_H = 26.0f;
    public static final float POP_SLIDER_Y = 118.0f;
    public static final float POP_VIEW_Y = 144.0f;
    public static final float POP_BTN_W = 66.0f;
    public static final float POP_BTN_H = 22.0f;

    private final ClickGuiScreen gui;
    private boolean sliderDragging = false;
    private float dragStartMx;
    private float dragStartScale;
    private float dragSliderW;

    public ProfilePopupRender(ClickGuiScreen gui) {
        this.gui = gui;
    }

    public boolean isSliderDragging() {
        return sliderDragging;
    }

    public boolean isHovering(float winX, float profY, float k, float mx, float my) {
        float pw = POP_W * k;
        float ph = POP_H * k;
        float px = winX + 12.0f * k;
        float py = profY - ph - 10.0f * k;
        return mx >= px && mx <= px + pw && my >= py && my <= py + ph;
    }

    public void render(GuiGraphicsExtractor g, float winX, float winY, float sidebarW, float winH, float profY,
                       float k, float a, float mx, float my, float dt, Minecraft minecraft) {
        if (a <= 0.01f) return;

        float easeP = ClickGuiScreen.easeOutCubic(a);
        float scaleP = 0.95f + 0.05f * easeP;
        float pw = POP_W * k * scaleP;
        float ph = POP_H * k * scaleP;
        float px = winX + 12.0f * k;
        float slide = (1.0f - easeP) * 10.0f * k;
        float py = profY - ph - 10.0f * k + slide;
        float pad = POP_PAD * k;

        int accentPrimary = Theme.current.getFirstColor();
        DrawRect.create()
                .bounds(px, py, pw, ph)
                .rounding(10.0f * k)
                .bg(0xF40F131C)
                .shadow(20.0f * k, 0.0f, 0.0f, 0xDD000000)
                .outline(1.2f, 0x3B4A5C7E)
                .alpha(a)
                .render(g);
        float avSize = 24.0f * k;
        Draw.headSelf(g, px + pad, py + 9.0f * k, avSize, 5.0f * k, a);

        String userName = (minecraft.getUser() != null && minecraft.getUser().getName() != null)
                ? minecraft.getUser().getName() : "Player";
        Draw.text(g, MsdfFonts.SF_MEDIUM, userName, px + pad + avSize + 10.0f * k, py + 10.5f * k, 10.5f * k, ColorMath.multiplyAlpha(0xFFFFFFFF, a));
        Draw.text(g, MsdfFonts.SF_MEDIUM, Lang.tr("client"), px + pad + avSize + 10.0f * k, py + 22.0f * k, 8.5f * k, ColorMath.multiplyAlpha(0xFF727379, a));
        float ccx = px + pw - pad - 6.0f * k;
        float ccy = py + 20.0f * k;
        float closeHit = 16.0f * k;
        boolean closeHov = (mx >= ccx - closeHit / 2.0f && mx <= ccx + closeHit / 2.0f
                && my >= ccy - closeHit / 2.0f && my <= ccy + closeHit / 2.0f);
        float chv = gui.animValue("pop_close_hov", closeHov ? 1.0f : 0.0f, dt, 16.0f);
        int closeCol = ColorMath.lerp(0xFF727379, 0xFFFFFFFF, chv);
        float cr = 3.2f * k;
        Draw.line(g, ccx - cr, ccy - cr, ccx + cr, ccy + cr, 1.3f * k, ColorMath.multiplyAlpha(closeCol, a));
        Draw.line(g, ccx + cr, ccy - cr, ccx - cr, ccy + cr, 1.3f * k, ColorMath.multiplyAlpha(closeCol, a));
        Draw.rect(g, px + pad, py + 38.0f * k, pw - pad * 2.0f, 1.0f, 0.5f, ColorMath.multiplyAlpha(0x18FFFFFF, a));
        float rowW = pw - pad * 2.0f;
        float ry0 = py + POP_ROW_Y * k;
        boolean hov0 = (mx >= px + pad && mx <= px + pad + rowW && my >= ry0 - 4.0f * k && my <= ry0 + POP_ROW_H * k - 4.0f * k);
        float aHov0 = gui.animValue("pop_row0_hov", hov0 ? 1.0f : 0.0f, dt, 16.0f);
        if (aHov0 > 0.01f) {
            Draw.rect(g, px + pad, ry0 - 4.0f * k, rowW, POP_ROW_H * k, 5.0f * k, ColorMath.multiplyAlpha(0x14FFFFFF, aHov0 * a));
        }
        Draw.text(g, MsdfFonts.SF_MEDIUM, Lang.tr("theme"), px + pad + 6.0f * k, ry0 + 2.0f * k, 10.0f * k, ColorMath.multiplyAlpha(0xFF9CA3AF, a));
        String themeName = Theme.current.getName();
        float tw = MsdfFonts.SF_MEDIUM.width(themeName, 10.0f * k);
        Draw.text(g, MsdfFonts.SF_MEDIUM, themeName, px + pw - pad - 6.0f * k - tw, ry0 + 2.0f * k, 10.0f * k, ColorMath.multiplyAlpha(ColorMath.lerp(0xFFE2E8F0, accentPrimary, aHov0), a));
        float ry1 = py + (POP_ROW_Y + POP_ROW_H) * k;
        boolean hov1 = (mx >= px + pad && mx <= px + pad + rowW && my >= ry1 - 4.0f * k && my <= ry1 + POP_ROW_H * k - 4.0f * k);
        float aHov1 = gui.animValue("pop_row1_hov", hov1 ? 1.0f : 0.0f, dt, 16.0f);
        if (aHov1 > 0.01f) {
            Draw.rect(g, px + pad, ry1 - 4.0f * k, rowW, POP_ROW_H * k, 5.0f * k, ColorMath.multiplyAlpha(0x14FFFFFF, aHov1 * a));
        }
        Draw.text(g, MsdfFonts.SF_MEDIUM, Lang.tr("language"), px + pad + 6.0f * k, ry1 + 2.0f * k, 10.0f * k, ColorMath.multiplyAlpha(0xFF9CA3AF, a));
        String langName = Lang.current.getDisplayName();
        float lw = MsdfFonts.SF_MEDIUM.width(langName, 10.0f * k);
        Draw.text(g, MsdfFonts.SF_MEDIUM, langName, px + pw - pad - 6.0f * k - lw, ry1 + 2.0f * k, 10.0f * k, ColorMath.multiplyAlpha(ColorMath.lerp(0xFFE2E8F0, accentPrimary, aHov1), a));
        float slY = py + POP_SLIDER_Y * k;
        float slX = px + pad + 6.0f * k;
        float slW = pw - pad * 2.0f - 12.0f * k;

        Draw.text(g, MsdfFonts.SF_MEDIUM, Lang.tr("guiScale"), slX, slY - 14.0f * k, 9.5f * k, ColorMath.multiplyAlpha(0xFF9CA3AF, a));
        String pctStr = Math.round(ClickGuiScreen.uiScale * 100.0f) + "%";
        float pctW = MsdfFonts.SF_MEDIUM.width(pctStr, 9.5f * k);
        Draw.text(g, MsdfFonts.SF_MEDIUM, pctStr, slX + slW - pctW, slY - 14.0f * k, 9.5f * k, ColorMath.multiplyAlpha(0xFFFFFFFF, a));

        Draw.rect(g, slX, slY, slW, 3.5f * k, 1.75f * k, ColorMath.multiplyAlpha(0xFF2A2E3D, a));
        float t = (ClickGuiScreen.uiScale - 0.5f) / 1.0f;
        t = Math.max(0.0f, Math.min(1.0f, t));
        if (t > 0.005f) {
            Draw.rect(g, slX, slY, slW * t, 3.5f * k, 1.75f * k, ColorMath.multiplyAlpha(accentPrimary, a));
        }
        float knobX = slX + slW * t;
        float knobY = slY + 1.75f * k;
        float knobR = sliderDragging ? 5.2f * k : 4.2f * k;
        Draw.circle(g, knobX, knobY, knobR, ColorMath.multiplyAlpha(0xFFFFFFFF, a));
        Draw.circleRing(g, knobX, knobY, knobR, 1.0f * k, ColorMath.multiplyAlpha(accentPrimary, a));
        float vy = py + POP_VIEW_Y * k;
        Draw.text(g, MsdfFonts.SF_MEDIUM, Lang.tr("guiView"), slX, vy + 4.5f * k, 9.5f * k, ColorMath.multiplyAlpha(0xFF9CA3AF, a));

        float btnW = POP_BTN_W * k;
        float btnH = POP_BTN_H * k;
        float btnFullX = px + pw - pad - btnW * 2.0f - 6.0f * k;
        float btnCompX = px + pw - pad - btnW;
        boolean fullActive = !ClickGuiScreen.compact;
        boolean fullHov = (mx >= btnFullX && mx <= btnFullX + btnW && my >= vy && my <= vy + btnH);
        float aFullHov = gui.animValue("pop_btn_full_hov", fullHov ? 1.0f : 0.0f, dt, 16.0f);
        int bgFull = fullActive ? ColorMath.multiplyAlpha(accentPrimary, 0.28f) : ColorMath.lerp(0x18FFFFFF, 0x2AFFFFFF, aFullHov);
        int bdFull = fullActive ? accentPrimary : ColorMath.lerp(0x2E3A445C, 0x554B5563, aFullHov);
        Draw.rect(g, btnFullX, vy, btnW, btnH, 5.0f * k, ColorMath.multiplyAlpha(bgFull, a));
        Draw.border(g, btnFullX, vy, btnW, btnH, 5.0f * k, 1.0f, ColorMath.multiplyAlpha(bdFull, a));
        String txtFull = Lang.tr("viewFull");
        float tfw = MsdfFonts.SF_MEDIUM.width(txtFull, 8.5f * k);
        Draw.text(g, MsdfFonts.SF_MEDIUM, txtFull, btnFullX + (btnW - tfw) / 2.0f, vy + 5.0f * k, 9f * k,
                ColorMath.multiplyAlpha(fullActive ? 0xFFFFFFFF : 0xFFCBD5E1, a));
        boolean compActive = ClickGuiScreen.compact;
        boolean compHov = (mx >= btnCompX && mx <= btnCompX + btnW && my >= vy && my <= vy + btnH);
        float aCompHov = gui.animValue("pop_btn_comp_hov", compHov ? 1.0f : 0.0f, dt, 16.0f);
        int bgComp = compActive ? ColorMath.multiplyAlpha(accentPrimary, 0.28f) : ColorMath.lerp(0x18FFFFFF, 0x2AFFFFFF, aCompHov);
        int bdComp = compActive ? accentPrimary : ColorMath.lerp(0x2E3A445C, 0x554B5563, aCompHov);
        Draw.rect(g, btnCompX, vy, btnW, btnH, 5.0f * k, ColorMath.multiplyAlpha(bgComp, a));
        Draw.border(g, btnCompX, vy, btnW, btnH, 5.0f * k, 1.0f, ColorMath.multiplyAlpha(bdComp, a));
        String txtComp = Lang.tr("viewCompact");
        float tcw = MsdfFonts.SF_MEDIUM.width(txtComp, 8.5f * k);
        Draw.text(g, MsdfFonts.SF_MEDIUM, txtComp, btnCompX + (btnW - tcw) / 2.0f, vy + 5.0f * k, 9f * k,
                ColorMath.multiplyAlpha(compActive ? 0xFFFFFFFF : 0xFFCBD5E1, a));
    }

    public boolean handleClick(float winX, float winY, float sidebarW, float winH, float profY,
                             float k, float mx, float my, int button) {
        float pw = POP_W * k;
        float ph = POP_H * k;
        float px = winX + 12.0f * k;
        float py = profY - ph - 10.0f * k;
        float pad = POP_PAD * k;
        if (mx < px || mx > px + pw || my < py || my > py + ph) {
            gui.setProfilePopupOpen(false);
            return true;
        }
        float ccx = px + pw - pad - 6.0f * k;
        float ccy = py + 20.0f * k;
        float closeHit = 16.0f * k;
        if (mx >= ccx - closeHit / 2.0f && mx <= ccx + closeHit / 2.0f
                && my >= ccy - closeHit / 2.0f && my <= ccy + closeHit / 2.0f) {
            gui.setProfilePopupOpen(false);
            return true;
        }
        float rowW = pw - pad * 2.0f;
        float ry0 = py + POP_ROW_Y * k;
        if (mx >= px + pad && mx <= px + pad + rowW && my >= ry0 - 4.0f * k && my <= ry0 + POP_ROW_H * k - 4.0f * k) {
            int idx = Theme.ALL.indexOf(Theme.current);
            Theme next = Theme.ALL.get((idx + 1) % Theme.ALL.size());
            Theme.current = next;
            ConfigManager.save();
            return true;
        }
        float ry1 = py + (POP_ROW_Y + POP_ROW_H) * k;
        if (mx >= px + pad && mx <= px + pad + rowW && my >= ry1 - 4.0f * k && my <= ry1 + POP_ROW_H * k - 4.0f * k) {
            Lang next = (Lang.current == Lang.RU ? Lang.EN : Lang.RU);
            Lang.setLanguage(next);
            ConfigManager.save();
            return true;
        }
        float slY = py + POP_SLIDER_Y * k;
        float slX = px + pad + 6.0f * k;
        float slW = pw - pad * 2.0f - 12.0f * k;
        if (mx >= slX - 6.0f * k && mx <= slX + slW + 6.0f * k && my >= slY - 10.0f * k && my <= slY + 12.0f * k) {
            float t = Math.max(0.0f, Math.min(1.0f, (mx - slX) / slW));
            dragStartScale = Math.round((0.50f + t * 1.00f) * 100.0f) / 100.0f;
            ClickGuiScreen.uiScale = dragStartScale;
            dragStartMx = mx;
            dragSliderW = slW;
            sliderDragging = true;
            return true;
        }
        float vy = py + POP_VIEW_Y * k;
        float btnW = POP_BTN_W * k;
        float btnH = POP_BTN_H * k;
        float btnFullX = px + pw - pad - btnW * 2.0f - 6.0f * k;
        float btnCompX = px + pw - pad - btnW;

        if (my >= vy && my <= vy + btnH) {
            if (mx >= btnFullX && mx <= btnFullX + btnW) {
                ClickGuiScreen.compact = false;
                ConfigManager.save();
                return true;
            }
            if (mx >= btnCompX && mx <= btnCompX + btnW) {
                ClickGuiScreen.compact = true;
                ConfigManager.save();
                return true;
            }
        }
        return true;
    }

    public void mouseDragged(float mx, float my) {
        if (sliderDragging && dragSliderW > 0.0f) {
            float delta = (mx - dragStartMx) / dragSliderW;
            float newScale = dragStartScale + delta * 1.00f;
            newScale = Math.max(0.50f, Math.min(1.50f, newScale));
            ClickGuiScreen.uiScale = Math.round(newScale * 100.0f) / 100.0f;
        }
    }

    public void mouseReleased() {
        if (sliderDragging) {
            sliderDragging = false;
            ConfigManager.save();
        }
    }
}
