package dev.render4u.gui.impl;

import com.mojang.blaze3d.platform.InputConstants;
import dev.render4u.config.ConfigManager;
import dev.render4u.gui.ClickGuiScreen;
import dev.render4u.module.Module;
import dev.render4u.module.setting.*;
import dev.render4u.render.display.Draw;
import dev.render4u.render.display.element.DrawRect;
import dev.render4u.render.font.msdf.MsdfFonts;
import dev.render4u.theme.Theme;
import dev.render4u.util.KeyUtil;
import dev.render4u.util.Lang;
import dev.render4u.util.color.ColorMath;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;

public class ModuleCardRender {
    public static float cardHeight(float k) {
        return (ClickGuiScreen.compact ? 44.0f : 68.0f) * k;
    }

    private final ClickGuiScreen gui;

    public ModuleCardRender(ClickGuiScreen gui) {
        this.gui = gui;
    }

    public void render(GuiGraphicsExtractor g, Module module, int index, float cx, float baseCy, float colW, float k,
                       float ap, float clipTop, float clipBottom, float mx, float my, float dt, long now, long openedAt) {
        float cardH = cardHeight(k);
        float slideOffset = (1.0f - ap) * 12.0f * k;
        float cy = baseCy + slideOffset;

        float switchW = 26.0f * k;
        float switchH = 15.0f * k;
        float switchX = cx + colW - 14.0f * k - switchW;

        boolean isBinding = (gui.getBindingModule() == module);
        String bindText = isBinding ? "..." : (module.getKey() != 0 && module.getKey() != InputConstants.UNKNOWN.getValue() ? KeyUtil.getKeyName(module.getKey()) : Lang.tr("bind"));
        float tw = MsdfFonts.SF_MEDIUM.width(bindText, 7.5f * k);
        float bindW = Math.max(34.0f * k, tw + 10.0f * k);
        float bindH = 15.0f * k;
        float bindX = switchX - 8.0f * k - bindW;

        float gearSize = 14.0f * k;
        float gearX = bindX - 6.0f * k - gearSize;

        float btnHitY = baseCy + 8.0f * k;
        float gearHitY = baseCy + 8.5f * k;
        float gearPad = 2.0f * k;

        boolean inCard = (mx >= cx && mx <= cx + colW && my >= baseCy && my <= baseCy + cardH && my >= clipTop && my <= clipBottom);

        boolean switchHovered = inCard && (mx >= switchX - 2.0f * k && mx <= switchX + switchW + 2.0f * k && my >= btnHitY - 2.0f * k && my <= btnHitY + switchH + 2.0f * k);
        boolean bindHovered = inCard && (mx >= bindX && mx <= bindX + bindW && my >= btnHitY - 2.0f * k && my <= btnHitY + bindH + 2.0f * k);
        boolean gearHovered = inCard && module.hasSettings() && (mx >= gearX - gearPad && mx <= gearX + gearSize + gearPad && my >= gearHitY - gearPad && my <= gearHitY + gearSize + gearPad);

        boolean hovered = inCard && !switchHovered && !bindHovered && !gearHovered;

        float hov = gui.animValue("card_hov_" + module.getName(), hovered ? 1.0f : 0.0f, dt, 16.0f);
        float cardLift = hov * 2.0f * k;
        float drawCy = cy - cardLift;

        int accentPrimary = Theme.current.getFirstColor();
        int cardBg = ColorMath.lerp(0xEE12151F, 0xF5181E2E, hov);
        int cardBorder = ColorMath.lerp(0x262D384D, 0x4B485A7E, hov);

        DrawRect card = DrawRect.create()
                .bounds(cx, drawCy, colW, cardH)
                .rounding(8.0f * k)
                .bg(cardBg)
                .outline(1.0f, cardBorder)
                .alpha(ap);
        if (hov > 0.01f) {
            card.shadow((10.0f + hov * 6.0f) * k, 0.0f, 0.0f, ColorMath.multiplyAlpha(0x50000000, hov));
        }
        card.render(g);
        int titleCol = ColorMath.lerp(0xFFE2E8F0, 0xFFFFFFFF, hov);
        Draw.text(g, MsdfFonts.SF_MEDIUM, module.getName(), cx + 12.0f * k, drawCy + 9.5f * k, 10.5f * k, ColorMath.multiplyAlpha(titleCol, ap));

        float switchY = drawCy + 8.0f * k;
        float bindY = drawCy + 8.0f * k;
        float gearY = drawCy + 8.5f * k;
        if (module.hasSettings()) {
            float gHov = gui.animValue("gear_hov_" + module.getName(), gearHovered ? 1.0f : 0.0f, dt, 16.0f);
            if (gHov > 0.01f) {
                Draw.rect(g, gearX - 2.0f * k, gearY - 1.0f * k, gearSize + 4.0f * k, gearSize + 2.0f * k, 4.0f * k, ColorMath.multiplyAlpha(0x18FFFFFF, gHov * ap));
            }
            int gearCol = ColorMath.lerp(0xFF64748B, accentPrimary, gHov);
            Draw.text(g, MsdfFonts.ICONS, "E", gearX + 2.0f * k, gearY + 1.5f * k, 10.0f * k, ColorMath.multiplyAlpha(gearCol, ap));
        }
        float bHov = gui.animValue("bhov_" + module.getName(), bindHovered ? 1.0f : 0.0f, dt, 16.0f);

        if (isBinding) {
            float pulse = 0.65f + 0.35f * (float) Math.sin((now - openedAt) / 140.0);
            int pBorder = ColorMath.multiplyAlpha(accentPrimary, pulse * ap);
            DrawRect.create()
                    .bounds(bindX, bindY, bindW, bindH)
                    .rounding(4.0f * k)
                    .bg(ColorMath.multiplyAlpha(accentPrimary, 0.25f))
                    .outline(1.0f, pBorder)
                    .alpha(ap)
                    .render(g);
        } else {
            int bBg = ColorMath.lerp(0x22242B3B, 0x332E374D, bHov);
            int bBorder = ColorMath.lerp(0x33374151, 0x774B5563, bHov);
            DrawRect.create()
                    .bounds(bindX, bindY, bindW, bindH)
                    .rounding(4.0f * k)
                    .bg(bBg)
                    .outline(0.8f, bBorder)
                    .alpha(ap)
                    .render(g);
        }

        int bindTextCol = isBinding ? accentPrimary : ColorMath.lerp(0xFF9CA3AF, 0xFFCBD5E1, bHov);
        Draw.text(g, MsdfFonts.SF_MEDIUM, bindText, bindX + (bindW - tw) / 2.0f, bindY + 3.0f * k, 7.5f * k, ColorMath.multiplyAlpha(bindTextCol, ap));
        float tog = gui.animValue("tg_" + module.getName(), module.isEnabled() ? 1.0f : 0.0f, dt, 16.0f);
        float swHov = gui.animValue("sw_hov_" + module.getName(), switchHovered ? 1.0f : 0.0f, dt, 16.0f);

        int toggleOff = 0xFF0C0F16;
        int trackBg = ColorMath.lerp(toggleOff, accentPrimary, tog);
        int trackBorder = ColorMath.lerp(ColorMath.lerp(0x22FFFFFF, 0x66FFFFFF, swHov), 0x00FFFFFF, tog);

        DrawRect switchTrack = DrawRect.create()
                .bounds(switchX, switchY, switchW, switchH)
                .rounding(switchH / 2.0f)
                .bg(trackBg)
                .outline(1.0f, trackBorder)
                .alpha(ap);

        if (tog > 0.05f) {
            float glow = (0.45f + 0.15f * swHov) * tog;
            switchTrack.shadow(8.0f * k, 0.0f, 0.0f, ColorMath.multiplyAlpha(accentPrimary, glow));
        }
        switchTrack.render(g);

        float knob = 11.0f * k;
        float knobX = switchX + 2.0f * k + (switchW - knob - 4.0f * k) * tog;
        float knobY = switchY + 2.0f * k;
        int knobColor = ColorMath.lerp(0xFF969CA8, 0xFFFFFFFF, tog);
        Draw.rect(g, knobX, knobY, knob, knob, knob / 2.0f, ColorMath.multiplyAlpha(knobColor, ap));
        String desc = module.getDescription();
        float maxDescW = colW - 24.0f * k;
        float descW = MsdfFonts.SF_MEDIUM.width(desc, 8.2f * k);
        if (descW > maxDescW) {
            while (!desc.isEmpty() && MsdfFonts.SF_MEDIUM.width(desc + "...", 8.2f * k) > maxDescW) {
                desc = desc.substring(0, desc.length() - 1);
            }
            desc = desc + "...";
        }
        float descY = drawCy + (ClickGuiScreen.compact ? 27.0f : 28.0f) * k;
        Draw.text(g, MsdfFonts.SF_MEDIUM, desc, cx + 12.0f * k, descY, 8.2f * k, ColorMath.multiplyAlpha(0xFF758298, ap));
        if (!ClickGuiScreen.compact) {
            float pillY = drawCy + 45.0f * k;
            float curPillX = cx + 12.0f * k;
            float maxPillsW = colW - 24.0f * k;

            List<Setting<?>> settings = module.getSettings();
            if (settings != null && !settings.isEmpty()) {
                int shown = 0;
                for (Setting<?> setting : settings) {
                    if (!setting.isVisible() || shown >= 3) continue;

                    String pillText;
                    int pillColor = 0xFF9CA3AF;
                    if (setting instanceof BooleanSetting bs) {
                        pillText = bs.getName() + ": " + (bs.get() ? Lang.tr("on") : Lang.tr("off"));
                        pillColor = bs.get() ? accentPrimary : 0xFF64748B;
                    } else if (setting instanceof ModeSetting ms) {
                        pillText = ms.get();
                        pillColor = accentPrimary;
                    } else if (setting instanceof NumberSetting ns) {
                        pillText = ns.getName() + ": " + ns.getFormattedValue();
                        pillColor = 0xFFCBD5E1;
                    } else if (setting instanceof ColorSetting cs) {
                        pillText = cs.toHex();
                        pillColor = cs.get();
                    } else {
                        pillText = setting.getName();
                    }

                    float ptw = MsdfFonts.SF_MEDIUM.width(pillText, 7.5f * k);
                    float pillW = ptw + 10.0f * k;
                    if (curPillX + pillW - (cx + 12.0f * k) > maxPillsW) break;

                    Draw.rect(g, curPillX, pillY, pillW, 14.0f * k, 4.0f * k, ColorMath.multiplyAlpha(0x18FFFFFF, ap));
                    Draw.border(g, curPillX, pillY, pillW, 14.0f * k, 4.0f * k, 0.7f, ColorMath.multiplyAlpha(0x22FFFFFF, ap));
                    Draw.text(g, MsdfFonts.SF_MEDIUM, pillText, curPillX + 5.0f * k, pillY + 3.0f * k, 7.5f * k, ColorMath.multiplyAlpha(pillColor, ap));

                    curPillX += pillW + 5.0f * k;
                    shown++;
                }
            } else {
                String catText = module.getCategory().getDisplayName();
                float ctw = MsdfFonts.SF_MEDIUM.width(catText, 7.5f * k);
                float pillW = ctw + 10.0f * k;
                Draw.rect(g, curPillX, pillY, pillW, 14.0f * k, 4.0f * k, ColorMath.multiplyAlpha(0x10FFFFFF, ap));
                Draw.text(g, MsdfFonts.SF_MEDIUM, catText, curPillX + 5.0f * k, pillY + 3.0f * k, 7.5f * k, ColorMath.multiplyAlpha(0xFF64748B, ap));
            }
        }
    }

    public boolean handleClick(Module module, float cx, float cy, float colW, float k, float mx, float my, int button) {
        float cardH = cardHeight(k);

        float switchW = 26.0f * k;
        float switchH = 15.0f * k;
        float switchX = cx + colW - 14.0f * k - switchW;
        float switchY = cy + 8.0f * k;

        boolean isBinding = (gui.getBindingModule() == module);
        String bindText = isBinding ? "..." : (module.getKey() != 0 && module.getKey() != InputConstants.UNKNOWN.getValue() ? KeyUtil.getKeyName(module.getKey()) : Lang.tr("bind"));
        float tw = MsdfFonts.SF_MEDIUM.width(bindText, 7.5f * k);
        float bindW = Math.max(34.0f * k, tw + 10.0f * k);
        float bindH = 15.0f * k;
        float bindX = switchX - 8.0f * k - bindW;
        float bindY = cy + 8.0f * k;

        float gearSize = 14.0f * k;
        float gearX = bindX - 6.0f * k - gearSize;
        float gearY = cy + 8.5f * k;
        float gearPad = 2.0f * k;
        boolean isLeft = (button == InputConstants.MOUSE_BUTTON_LEFT || button == 0);
        boolean isRight = (button == InputConstants.MOUSE_BUTTON_RIGHT || button == 3);

        if (mx >= switchX - 2.0f * k && mx <= switchX + switchW + 2.0f * k && my >= switchY - 2.0f * k && my <= switchY + switchH + 2.0f * k) {
            if (isLeft) {
                module.toggle();
                ConfigManager.save();
                return true;
            }
        }
        if (mx >= bindX - 2.0f * k && mx <= bindX + bindW + 2.0f * k && my >= bindY - 3.0f * k && my <= bindY + bindH + 3.0f * k) {
            if (isRight) {
                module.setKey(InputConstants.UNKNOWN.getValue());
                gui.setBindingModule(null);
                ConfigManager.save();
            } else if (isLeft) {
                if (gui.getBindingModule() == module) {
                    gui.setBindingModule(null);
                } else {
                    gui.setBindingModule(module);
                }
            }
            return true;
        }
        if (module.hasSettings() && mx >= gearX - gearPad && mx <= gearX + gearSize + gearPad && my >= gearY - gearPad && my <= gearY + gearSize + gearPad) {
            if (isLeft) {
                gui.getSettingsPanel().open(module);
                return true;
            }
        }
        if (mx >= cx && mx <= cx + colW && my >= cy && my <= cy + cardH) {
            if (isRight) {
                if (module.hasSettings()) {
                    gui.getSettingsPanel().open(module);
                } else {
                    module.toggle();
                    ConfigManager.save();
                }
            } else if (isLeft) {
                module.toggle();
                ConfigManager.save();
            }
            return true;
        }

        return false;
    }
}
