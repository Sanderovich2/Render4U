package dev.render4u.gui.impl;

import com.mojang.blaze3d.platform.InputConstants;
import dev.render4u.Constants;
import dev.render4u.gui.ClickGuiScreen;
import dev.render4u.module.Category;
import dev.render4u.render.display.Draw;
import dev.render4u.render.display.element.DrawRect;
import dev.render4u.render.font.msdf.MsdfFonts;
import dev.render4u.theme.Theme;
import dev.render4u.util.Lang;
import dev.render4u.util.color.ColorMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.Arrays;

public class SidebarRender {
    public static final Category[] CATEGORIES = {
            Category.COMBAT, Category.MOVEMENT,
            Category.RENDER, Category.PLAYER,
            Category.MISC, Category.THEMES
    };

    private static final float[] CAT_REL_YS = {66.0f, 96.0f, 158.0f, 188.0f, 250.0f, 280.0f};
    private static final String[] SEL_KEYS = Arrays.stream(CATEGORIES).map(c -> "sel_" + c.name()).toArray(String[]::new);
    private static final String[] HOV_KEYS = Arrays.stream(CATEGORIES).map(c -> "hov_tab_" + c.name()).toArray(String[]::new);

    private final ClickGuiScreen gui;

    public SidebarRender(ClickGuiScreen gui) {
        this.gui = gui;
    }

    public void render(GuiGraphicsExtractor g, float winX, float winY, float sidebarW, float winH, float profY,
                       float k, float winAlpha, float mx, float my, float dt, Minecraft minecraft) {
        if (winAlpha <= 0.005f) return;

        int accentPrimary = Theme.current.getFirstColor();
        int accentSecondary = Theme.current.getSecondColor();
        Draw.rect(g, winX + sidebarW, winY + 12.0f * k, 1.0f, winH - 24.0f * k, 0.5f, ColorMath.multiplyAlpha(0x18FFFFFF, winAlpha));
        float logoX = winX + 16.0f * k;
        float logoY = winY + 16.0f * k;
        float logoSize = 27.0f * k;
        Draw.text(g, MsdfFonts.LOGO, "A", logoX, logoY, logoSize, ColorMath.multiplyAlpha(accentPrimary, winAlpha));
        Draw.text(g, MsdfFonts.SF_BOLD, Constants.getName(), logoX + logoSize + 3.0f * k, winY + 18.0f * k, 12.0f * k, ColorMath.multiplyAlpha(0xFFFFFFFF, winAlpha));
        Draw.text(g, MsdfFonts.SF_MEDIUM, "v" + Constants.getVersion(), logoX + logoSize + 3.0f * k, winY + 30.0f * k, 8.0f * k, ColorMath.multiplyAlpha(0xFF64748B, winAlpha));
        Draw.text(g, MsdfFonts.SF_MEDIUM, Lang.tr("main"), winX + 22.0f * k, winY + 54.0f * k, 8.0f * k, ColorMath.multiplyAlpha(0xFF5B6476, winAlpha));
        Draw.text(g, MsdfFonts.SF_MEDIUM, Lang.tr("general"), winX + 22.0f * k, winY + 146.0f * k, 8.0f * k, ColorMath.multiplyAlpha(0xFF5B6476, winAlpha));
        Draw.text(g, MsdfFonts.SF_MEDIUM, Lang.tr("other"), winX + 22.0f * k, winY + 238.0f * k, 8.0f * k, ColorMath.multiplyAlpha(0xFF5B6476, winAlpha));

        float tabX = winX + 10.0f * k;
        float tabW = sidebarW - 20.0f * k;
        float tabH = 26.0f * k;

        for (int i = 0; i < CATEGORIES.length; i++) {
            Category cat = CATEGORIES[i];
            float ty = winY + CAT_REL_YS[i] * k;
            boolean selected = (cat == gui.getSelectedCategory() && gui.getSearchQuery().isEmpty());
            boolean hovered = (mx >= tabX && mx <= tabX + tabW && my >= ty && my <= ty + tabH);

            if (!gui.hasAnim(SEL_KEYS[i])) {
                gui.setAnim(SEL_KEYS[i], selected ? 1.0f : 0.0f);
            }
            float selv = gui.animValue(SEL_KEYS[i], selected ? 1.0f : 0.0f, dt, 16.0f);
            float hov = gui.animValue(HOV_KEYS[i], hovered ? 1.0f : 0.0f, dt, 16.0f);

            float bgAlpha = Math.max(hov * 0.45f, selv);
            if (bgAlpha > 0.01f) {
                int tabBg = ColorMath.lerp(0x0EFFFFFF, ColorMath.multiplyAlpha(accentSecondary, 0.22f), selv);
                DrawRect tab = DrawRect.create()
                        .bounds(tabX, ty, tabW, tabH)
                        .rounding(6.0f * k)
                        .bg(tabBg)
                        .alpha(bgAlpha * winAlpha);
                if (selv > 0.01f) {
                    tab.outline(0.8f, ColorMath.multiplyAlpha(accentPrimary, 0.35f * selv));
                }
                tab.render(g);
            }

            int iconCol = ColorMath.lerp(0xFF6B7280, accentPrimary, selv);
            int textCol = ColorMath.lerp(0xFF9CA3AF, 0xFFFFFFFF, Math.max(hov, selv));

            String catIcon = gui.getCategoryIcon(cat);
            Draw.text(g, MsdfFonts.ICONS, catIcon, tabX + 10.0f * k, ty + 6.5f * k, 12.0f * k, ColorMath.multiplyAlpha(iconCol, winAlpha));
            Draw.text(g, MsdfFonts.SF_MEDIUM, cat.getDisplayName(), tabX + 28.0f * k, ty + 7.5f * k, 10.0f * k, ColorMath.multiplyAlpha(textCol, winAlpha));
        }
        Draw.rect(g, winX + 12.0f * k, profY - 6.0f * k, sidebarW - 24.0f * k, 1.0f, 0.5f, ColorMath.multiplyAlpha(0x18FFFFFF, winAlpha));
        Draw.headSelf(g, winX + 16.0f * k, profY + 2.0f * k, 22.0f * k, 5.0f * k, winAlpha);

        String userName = (minecraft.getUser() != null && minecraft.getUser().getName() != null)
                ? minecraft.getUser().getName() : "Player";
        Draw.text(g, MsdfFonts.SF_MEDIUM, userName, winX + 44.0f * k, profY + 4.5f * k, 9.0f * k, ColorMath.multiplyAlpha(0xFFFFFFFF, winAlpha));
        Draw.text(g, MsdfFonts.SF_MEDIUM, Lang.tr("user"), winX + 44.0f * k, profY + 15.5f * k, 7.5f * k, ColorMath.multiplyAlpha(0xFF64748B, winAlpha));
        float dotsX = winX + sidebarW - 20.0f * k;
        float dotsY = profY + 13.0f * k;
        float dotsHit = 18.0f * k;
        boolean dotsHovered = (mx >= dotsX - dotsHit / 2.0f && mx <= dotsX + dotsHit / 2.0f
                && my >= dotsY - dotsHit / 2.0f && my <= dotsY + dotsHit / 2.0f);
        int dotsCol = (dotsHovered || gui.isProfilePopupOpen()) ? accentPrimary : 0xFF64748B;
        for (int d = 0; d < 3; d++) {
            Draw.rect(g, dotsX - 1.0f * k, dotsY - 5.0f * k + d * 5.0f * k, 2.0f * k, 2.0f * k, 1.0f * k, ColorMath.multiplyAlpha(dotsCol, winAlpha));
        }
    }

    public boolean handleClick(float winX, float winY, float sidebarW, float profY, float k, float mx, float my, int button) {
        if (button != InputConstants.MOUSE_BUTTON_LEFT && button != 0) return false;
        float dotsX = winX + sidebarW - 20.0f * k;
        float dotsY = profY + 13.0f * k;
        float dotsHit = 18.0f * k;
        if (mx >= dotsX - dotsHit / 2.0f && mx <= dotsX + dotsHit / 2.0f
                && my >= dotsY - dotsHit / 2.0f && my <= dotsY + dotsHit / 2.0f) {
            gui.setProfilePopupOpen(!gui.isProfilePopupOpen());
            return true;
        }

        float tabX = winX + 10.0f * k;
        float tabW = sidebarW - 20.0f * k;
        float tabH = 26.0f * k;

        for (int i = 0; i < CATEGORIES.length; i++) {
            float ty = winY + CAT_REL_YS[i] * k;
            if (mx >= tabX && mx <= tabX + tabW && my >= ty && my <= ty + tabH) {
                if (gui.getSelectedCategory() != CATEGORIES[i] || !gui.getSearchQuery().isEmpty()) {
                    gui.setSelectedCategory(CATEGORIES[i]);
                    gui.setSearchQuery("");
                    gui.setSearchFocused(false);
                    gui.resetScroll();
                }
                return true;
            }
        }

        return false;
    }
}
