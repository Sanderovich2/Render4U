package dev.render4u.module.impl.render;

import dev.render4u.Constants;
import dev.render4u.core.event.EventTarget;
import dev.render4u.core.event.impl.EventDisplay;
import dev.render4u.module.Category;
import dev.render4u.module.Module;
import dev.render4u.module.setting.BooleanSetting;
import dev.render4u.module.setting.ColorSetting;
import dev.render4u.module.setting.ModeSetting;
import dev.render4u.render.display.Draw;
import dev.render4u.render.display.element.DrawRect;
import dev.render4u.render.font.msdf.MsdfFonts;
import dev.render4u.theme.Theme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;

import java.util.List;

public final class HUDModule extends Module {
    public static final String ACCENT_THEME = "Тема";
    public static final String ACCENT_CUSTOM = "Кастомный";

    public final BooleanSetting watermark = new BooleanSetting("Инфо-бар", true, "Показывать верхний инфо-бар");
    public final ModeSetting accentMode = new ModeSetting("Цвет акцента", ACCENT_THEME, List.of(ACCENT_THEME, ACCENT_CUSTOM), "Выбор цвета акцента: цвет темы или кастомный");
    public final ColorSetting customAccent = new ColorSetting("Кастомный цвет", 0xFFC9B8FB, false, "Собственный цвет акцента", () -> accentMode.is(ACCENT_CUSTOM));
    public final BooleanSetting showFps = new BooleanSetting("FPS", true, "Показывать счетчик кадров");
    public final BooleanSetting showPing = new BooleanSetting("Пинг", true, "Показывать пинг до сервера");
    public final BooleanSetting showServer = new BooleanSetting("Сервер", true, "Показывать адрес сервера");

    public HUDModule() {
        super("HUD", "Отображает интерфейс клиента на экране", Category.RENDER);
        addSettings(watermark, accentMode, customAccent, showFps, showPing, showServer);
        setEnabled(true);
    }

    public int getAccentColor() {
        if (accentMode.is(ACCENT_CUSTOM)) {
            return customAccent.get();
        }
        return Theme.current.getFirstColor();
    }

    @EventTarget
    public void onDisplay(EventDisplay event) {
        if (mc == null || mc.level == null || mc.player == null) {
            return;
        }

        if (mc.gui != null && mc.gui.hud != null && mc.gui.hud.isHidden()) {
            return;
        }

        if (!watermark.isEnabled()) {
            return;
        }

        GuiGraphicsExtractor g = event.graphics();
        int accent = getAccentColor();

        String clientName = Constants.getName();
        String userName = mc.getUser() != null ? mc.getUser().getName() : "Player";
        String serverIp = mc.getCurrentServer() != null ? mc.getCurrentServer().ip : "Singleplayer";

        int ping = 0;
        if (mc.player != null && mc.getConnection() != null) {
            PlayerInfo info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
            if (info != null) {
                ping = Math.max(0, info.getLatency());
            }
        }
        String pingStr = ping + " ms";
        String fpsStr = mc.getFps() + " FPS";

        float textSize = 9.5f;
        float titleW = MsdfFonts.SF_BOLD.width(clientName, textSize);
        float userW = MsdfFonts.SF_MEDIUM.width(userName, textSize);
        float serverW = showServer.isEnabled() ? MsdfFonts.SF_MEDIUM.width(serverIp, textSize) : 0.0f;
        float pingW = showPing.isEnabled() ? MsdfFonts.SF_MEDIUM.width(pingStr, textSize) : 0.0f;
        float fpsW = showFps.isEnabled() ? MsdfFonts.SF_SEMIBOLD.width(fpsStr, textSize) : 0.0f;

        float logoSize = 14.0f;
        float logoW = MsdfFonts.LOGO.width("A", logoSize);
        float logoSpacing = 3.0f;
        float pad = 6.0f;
        float divW = 1.0f;
        float divSpacing = 8.0f;

        float totalW = pad + logoW + logoSpacing + titleW + (divSpacing * 2.0f + divW) + userW;
        if (showServer.isEnabled()) {
            totalW += (divSpacing * 2.0f + divW) + serverW;
        }
        if (showPing.isEnabled()) {
            totalW += (divSpacing * 2.0f + divW) + pingW;
        }
        if (showFps.isEnabled()) {
            totalW += (divSpacing * 2.0f + divW) + fpsW;
        }
        totalW += pad;

        float pillX = 10.0f;
        float pillY = 10.0f;
        float pillH = 22.0f;

        DrawRect.create()
                .bounds(pillX, pillY, totalW, pillH)
                .rounding(6.0f)
                .bg(0xC80D0F15)
                .blur(3)
                .shadow(10.0f, 0.0f, 0.0f, 0x44000000)
                .outline(1.0f, 0x2E282E3D)
                .render(g);

        float curX = pillX + pad;

        Draw.text(g, MsdfFonts.LOGO, "A", curX, pillY + 5.0f, logoSize, accent);
        curX += logoW + logoSpacing;

        Draw.text(g, MsdfFonts.SF_BOLD, clientName, curX, pillY + 5.5f, textSize, accent);
        curX += titleW + divSpacing;

        Draw.rect(g, curX, pillY + 5.0f, divW, pillH - 10.0f, 0.5f, 0x22FFFFFF);
        curX += divW + divSpacing;

        Draw.text(g, MsdfFonts.SF_MEDIUM, userName, curX, pillY + 5.5f, textSize, 0xFFCBD5E1);
        curX += userW + divSpacing;

        if (showServer.isEnabled()) {
            Draw.rect(g, curX, pillY + 5.0f, divW, pillH - 10.0f, 0.5f, 0x22FFFFFF);
            curX += divW + divSpacing;
            Draw.text(g, MsdfFonts.SF_MEDIUM, serverIp, curX, pillY + 5.5f, textSize, 0xFF94A3B8);
            curX += serverW + divSpacing;
        }

        if (showPing.isEnabled()) {
            Draw.rect(g, curX, pillY + 5.0f, divW, pillH - 10.0f, 0.5f, 0x22FFFFFF);
            curX += divW + divSpacing;
            Draw.text(g, MsdfFonts.SF_MEDIUM, pingStr, curX, pillY + 5.5f, textSize, 0xFF94A3B8);
            curX += pingW + divSpacing;
        }

        if (showFps.isEnabled()) {
            Draw.rect(g, curX, pillY + 5.0f, divW, pillH - 10.0f, 0.5f, 0x22FFFFFF);
            curX += divW + divSpacing;
            Draw.text(g, MsdfFonts.SF_SEMIBOLD, fpsStr, curX, pillY + 5.5f, textSize, accent);
        }
    }
}
