package dev.render4u.gui;

import com.mojang.blaze3d.platform.InputConstants;
import dev.render4u.Constants;
import dev.render4u.config.ConfigManager;
import dev.render4u.gui.impl.ModuleCardRender;
import dev.render4u.gui.impl.ProfilePopupRender;
import dev.render4u.gui.impl.SettingsPanelRender;
import dev.render4u.gui.impl.SidebarRender;
import dev.render4u.module.Category;
import dev.render4u.module.Module;
import dev.render4u.module.ModuleManager;
import dev.render4u.module.impl.render.ClickGuiModule;
import dev.render4u.render.display.Draw;
import dev.render4u.render.display.batch.DisplayBatcher;
import dev.render4u.render.display.element.DrawRect;
import dev.render4u.render.font.msdf.MsdfFonts;
import dev.render4u.theme.Theme;
import dev.render4u.util.KeyUtil;
import dev.render4u.util.Lang;
import dev.render4u.util.color.ColorMath;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.*;

public final class ClickGuiScreen extends Screen {
    public static final float BASE_W = 640.0f;
    public static final float BASE_H = 420.0f;
    public static float uiScale = 0.85f;
    public static boolean compact = false;
    private boolean profilePopupOpen = false;
    private boolean closing = false;
    private float openProgress = 0.0f;

    public static Category selectedCategory = Category.RENDER;
    private Module bindingModule = null;
    private final long openedAt = System.currentTimeMillis();
    private long categorySwitchTime = System.currentTimeMillis();
    private long lastFrameTime = 0;
    private String searchQuery = "";
    private boolean searchFocused = false;
    private float scrollOffset = 0.0f;
    private float targetScroll = 0.0f;
    private final SidebarRender sidebarRender;
    private final ModuleCardRender moduleCardRender;
    private final SettingsPanelRender settingsPanelRender;
    private final ProfilePopupRender profilePopupRender;

    private final Map<String, Float> anims = new HashMap<>();

    public ClickGuiScreen() {
        super(Component.literal(Constants.getName() + " ClickGUI"));
        this.sidebarRender = new SidebarRender(this);
        this.moduleCardRender = new ModuleCardRender(this);
        this.settingsPanelRender = new SettingsPanelRender(this);
        this.profilePopupRender = new ProfilePopupRender(this);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean isInGameUi() {
        return true;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    }

    public boolean isBinding() {
        return bindingModule != null || settingsPanelRender.isBinding();
    }

    public Module getBindingModule() {
        return bindingModule;
    }

    public void setBindingModule(Module module) {
        this.bindingModule = module;
    }

    public SettingsPanelRender getSettingsPanel() {
        return settingsPanelRender;
    }

    public Category getSelectedCategory() {
        return selectedCategory;
    }

    public void setSelectedCategory(Category category) {
        selectedCategory = category;
        this.categorySwitchTime = System.currentTimeMillis();
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public boolean isProfilePopupOpen() {
        return profilePopupOpen;
    }

    public void setProfilePopupOpen(boolean profilePopupOpen) {
        this.profilePopupOpen = profilePopupOpen;
    }

    public void resetScroll() {
        this.scrollOffset = 0.0f;
        this.targetScroll = 0.0f;
    }

    public void setSearchFocused(boolean focused) {
        if (this.searchFocused == focused) return;
        this.searchFocused = focused;
        try {
            if (minecraft != null && minecraft.textInputManager() != null) {
                if (focused) {
                    minecraft.textInputManager().startTextInput(this);
                } else {
                    minecraft.textInputManager().stopTextInput();
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onClose() {
        if (!closing && openProgress > 0.05f) {
            closing = true;
            setSearchFocused(false);
            bindingModule = null;
            profilePopupOpen = false;
            profilePopupRender.mouseReleased();
            settingsPanelRender.close();
            ConfigManager.save();
            return;
        }
        ClickGuiModule mod = ModuleManager.getModule(ClickGuiModule.class);
        if (mod != null && mod.isEnabled()) {
            mod.setEnabled(false);
        }
        super.onClose();
    }

    public float getScale() {
        float gw = (float) this.width;
        float gh = (float) this.height;
        float fit = Math.min((gw - 20.0f) / BASE_W, (gh - 20.0f) / BASE_H);
        float base = Math.min(1.0f, fit);
        return Math.max(0.35f, base * uiScale);
    }

    public boolean hasAnim(String key) {
        return anims.containsKey(key);
    }

    public float getAnim(String key, float defaultValue) {
        return anims.getOrDefault(key, defaultValue);
    }

    public void setAnim(String key, float value) {
        anims.put(key, value);
    }

    public float animValue(String key, float target, float dt, float speed) {
        float v = anims.getOrDefault(key, 0.0f);
        float factor = 1.0f - (float) Math.exp(-speed * dt);
        v += (target - v) * Math.max(0.0f, Math.min(1.0f, factor));
        if (Math.abs(v - target) < 0.0005f) v = target;
        anims.put(key, v);
        return v;
    }

    public float animValue(String key, float target, float dt) {
        return animValue(key, target, dt, 15.0f);
    }

    public static float easeOutCubic(float x) {
        return 1.0f - (float) Math.pow(1.0f - Math.max(0.0f, Math.min(1.0f, x)), 3);
    }

    public static float appear(float t, int index) {
        float delay = Math.min(index, 16) * 0.022f;
        float progress = Math.max(0.0f, Math.min(1.0f, (t - delay) / 0.24f));
        return easeOutCubic(progress);
    }

    public String getCategoryIcon(Category category) {
        return switch (category) {
            case COMBAT -> "C";
            case MOVEMENT -> "B";
            case RENDER -> "4";
            case PLAYER -> "K";
            case MISC -> "v";
            case THEMES -> "E";
        };
    }

    public List<Module> getVisibleModules() {
        if (!searchQuery.trim().isEmpty()) {
            String q = searchQuery.toLowerCase(Locale.ROOT).trim();
            return ModuleManager.getModules().stream()
                    .filter(m -> m.getName().toLowerCase(Locale.ROOT).contains(q) || m.getDescription().toLowerCase(Locale.ROOT).contains(q))
                    .toList();
        }
        return ModuleManager.getByCategory(selectedCategory);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (closing) return false;
        float k = getScale();
        if (settingsPanelRender.isOpen()) {
            settingsPanelRender.mouseScrolled(mouseX, mouseY, scrollY, k, (float) this.width, (float) this.height);
            return true;
        }
        targetScroll -= (float) scrollY * 26.0f;
        float contentH;
        float totalH;
        if (selectedCategory == Category.THEMES && searchQuery.isEmpty()) {
            contentH = (BASE_H - 56.0f) * k;
            int rows = (Theme.ALL.size() + 1) / 2;
            totalH = rows * (46.0f * k + 10.0f * k);
        } else {
            contentH = (BASE_H - 56.0f) * k;
            int rows = (getVisibleModules().size() + 1) / 2;
            totalH = rows * (ModuleCardRender.cardHeight(k) + 10.0f * k);
        }
        float maxScroll = Math.max(0.0f, totalH - contentH);
        targetScroll = Math.max(0.0f, Math.min(maxScroll, targetScroll));
        return true;
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (closing) return false;
        if (settingsPanelRender.isOpen() && settingsPanelRender.charTyped(event)) {
            return true;
        }
        if (searchFocused) {
            String ch = event.codepointAsString();
            if (ch != null && !ch.isEmpty()) {
                searchQuery += ch;
                categorySwitchTime = System.currentTimeMillis();
                resetScroll();
                return true;
            }
        }
        return super.charTyped(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (closing) return false;
        if (bindingModule != null) {
            int key = event.key() != 0 ? event.key() : event.keycode();
            if (key == InputConstants.KEY_ESCAPE || key == InputConstants.KEY_DELETE || key == InputConstants.KEY_BACKSPACE) {
                bindingModule.setKey(InputConstants.UNKNOWN.getValue());
            } else {
                bindingModule.setKey(key);
            }
            bindingModule = null;
            ConfigManager.save();
            return true;
        }
        if (settingsPanelRender.isBinding()) {
            int key = event.key() != 0 ? event.key() : event.keycode();
            settingsPanelRender.onKey(key);
            return true;
        }
        if (settingsPanelRender.isOpen() && settingsPanelRender.keyPressed(event)) {
            return true;
        }
        if (event.hasControlDown() && event.key() == InputConstants.KEY_F) {
            setSearchFocused(true);
            return true;
        }
        if (searchFocused) {
            if (event.key() == InputConstants.KEY_BACKSPACE) {
                if (!searchQuery.isEmpty()) {
                    searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                    categorySwitchTime = System.currentTimeMillis();
                    resetScroll();
                }
                return true;
            }
            if (event.key() == InputConstants.KEY_ESCAPE || event.key() == InputConstants.KEY_RETURN) {
                setSearchFocused(false);
                return true;
            }
            if (event.key() == InputConstants.KEY_DELETE) {
                searchQuery = "";
                categorySwitchTime = System.currentTimeMillis();
                resetScroll();
                return true;
            }
            if (event.key() == InputConstants.KEY_SPACE) {
                searchQuery += " ";
                categorySwitchTime = System.currentTimeMillis();
                resetScroll();
                return true;
            }
        }
        if (System.currentTimeMillis() - openedAt < 120) {
            return false;
        }
        if (event.key() == InputConstants.KEY_ESCAPE) {
            if (settingsPanelRender.isOpen()) {
                settingsPanelRender.close();
                return true;
            }
            if (profilePopupOpen) {
                profilePopupOpen = false;
                return true;
            }
            if (!searchQuery.isEmpty()) {
                searchQuery = "";
                return true;
            }
            onClose();
            return true;
        }
        ClickGuiModule clickGui = ModuleManager.getModule(ClickGuiModule.class);
        if (clickGui != null && KeyUtil.isKeyMatch(clickGui.getKey(), event.key(), event.keycode())) {
            onClose();
            return true;
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        profilePopupRender.mouseReleased();
        settingsPanelRender.mouseReleased();
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (closing) return false;
        float mx = (float) event.x();
        float my = (float) event.y();
        int button = event.button();

        boolean isLeft = (button == InputConstants.MOUSE_BUTTON_LEFT || button == 0);
        boolean isRight = (button == InputConstants.MOUSE_BUTTON_RIGHT || button == 3);

        float k = getScale();
        float winW = BASE_W * k;
        float winH = BASE_H * k;
        float winX = (this.width - winW) / 2.0f;
        float winY = (this.height - winH) / 2.0f;
        float sidebarW = 140.0f * k;
        float profY = winY + winH - 42.0f * k;
        if (bindingModule != null) {
            bindingModule = null;
            return true;
        }
        if (settingsPanelRender.isBinding()) {
            settingsPanelRender.clearBinding();
            return true;
        }
        if (settingsPanelRender.isOpen()) {
            if (settingsPanelRender.mouseClicked(mx, my, button, k, (float) this.width, (float) this.height)) {
                return true;
            }
        }
        if (profilePopupOpen) {
            if (profilePopupRender.handleClick(winX, winY, sidebarW, winH, profY, k, mx, my, button)) {
                return true;
            }
        }
        if (sidebarRender.handleClick(winX, winY, sidebarW, profY, k, mx, my, button)) {
            return true;
        }
        float searchW = 145.0f * k;
        float searchH = 22.0f * k;
        float searchX = winX + winW - searchW - 16.0f * k;
        float searchY = winY + 8.5f * k;

        boolean searchHovered = (mx >= searchX && mx <= searchX + searchW && my >= searchY && my <= searchY + searchH);
        if (searchHovered) {
            if (isLeft) {
                setSearchFocused(true);
                return true;
            }
        } else {
            setSearchFocused(false);
        }
        float mainX = winX + sidebarW + 1.0f;
        float mainW = winW - sidebarW - 1.0f;
        float contentY = winY + 46.0f * k;
        float padX = 16.0f * k;
        float colGap = 12.0f * k;
        float contentW = mainW - padX * 2.0f;
        float colW = (contentW - colGap) / 2.0f;
        float col1X = mainX + padX;
        float col2X = col1X + colW + colGap;
        if (selectedCategory == Category.THEMES && searchQuery.isEmpty()) {
            float themeCardH = 46.0f * k;
            List<Theme> themes = Theme.ALL;
            for (int i = 0; i < themes.size(); i++) {
                Theme theme = themes.get(i);
                boolean isCol1 = (i % 2 == 0);
                int row = i / 2;
                float cx = isCol1 ? col1X : col2X;
                float cy = contentY + row * (themeCardH + 10.0f * k) - scrollOffset;

                if (cy + themeCardH < contentY || cy > winY + winH - 10.0f * k) continue;

                if (mx >= cx && mx <= cx + colW && my >= cy && my <= cy + themeCardH) {
                    if (isLeft) {
                        Theme.current = theme;
                        ConfigManager.save();
                        return true;
                    }
                }
            }
            return super.mouseClicked(event, doubleClick);
        }
        List<Module> modules = getVisibleModules();
        float cardH = ModuleCardRender.cardHeight(k);
        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            boolean isCol1 = (i % 2 == 0);
            int row = i / 2;
            float cx = isCol1 ? col1X : col2X;
            float cy = contentY + row * (cardH + 10.0f * k) - scrollOffset;

            if (cy + cardH < contentY || cy > winY + winH - 10.0f * k) continue;

            if (moduleCardRender.handleClick(module, cx, cy, colW, k, mx, my, button)) {
                return true;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        float mx = (float) mouseX;
        float my = (float) mouseY;

        long now = System.currentTimeMillis();
        float dt = lastFrameTime == 0 ? 0.016f : Math.min(0.08f, (now - lastFrameTime) / 1000.0f);
        lastFrameTime = now;

        float targetOpen = closing ? 0.0f : 1.0f;
        float openFactor = 1.0f - (float) Math.exp(-15.0 * dt);
        openProgress += (targetOpen - openProgress) * openFactor;
        if (Math.abs(openProgress - targetOpen) < 0.002f) {
            openProgress = targetOpen;
        }

        if (closing && openProgress <= 0.01f) {
            closing = false;
            ClickGuiModule mod = ModuleManager.getModule(ClickGuiModule.class);
            if (mod != null && mod.isEnabled()) {
                mod.setEnabled(false);
            }
            super.onClose();
            return;
        }

        float easeOpen = easeOutCubic(openProgress);
        float winAlpha = easeOpen;

        if (winAlpha <= 0.005f) {
            return;
        }

        float scrollFactor = 1.0f - (float) Math.exp(-16.0 * dt);
        scrollOffset += (targetScroll - scrollOffset) * scrollFactor;

        int accentPrimary = Theme.current.getFirstColor();

        if (profilePopupOpen && profilePopupRender.isSliderDragging()) {
            profilePopupRender.mouseDragged(mx, my);
        }

        float k = getScale() * (0.95f + 0.05f * easeOpen);
        float winW = BASE_W * k;
        float winH = BASE_H * k;
        float winX = (this.width - winW) / 2.0f;
        float winY = (this.height - winH) / 2.0f + (1.0f - easeOpen) * 14.0f * k;
        float sidebarW = 140.0f * k;
        float profY = winY + winH - 42.0f * k;
        if (settingsPanelRender.isOpen()) {
            settingsPanelRender.mouseDragged(mx, my, k, (float) this.width, (float) this.height);
        }

        boolean modalOpen = settingsPanelRender.isOpen();
        boolean popupHovered = profilePopupOpen && profilePopupRender.isHovering(winX, profY, k, mx, my);
        float passMx = (modalOpen || popupHovered) ? -9999.0f : mx;
        float passMy = (modalOpen || popupHovered) ? -9999.0f : my;

        DisplayBatcher.begin(graphics);
        try {
            Draw.rect(graphics, 0, 0, this.width, this.height, 0, ColorMath.multiplyAlpha(0x99080A10, winAlpha));
            DrawRect.create()
                    .bounds(winX, winY, winW, winH)
                    .rounding(14.0f * k)
                    .bg(0xF00D0F16)
                    .blur(3)
                    .shadow(18.0f * k, 0.0f, 0.0f, 0x88000000)
                    .outline(1.0f, 0x2E282E3E)
                    .alpha(winAlpha)
                    .render(graphics);
            sidebarRender.render(graphics, winX, winY, sidebarW, winH, profY, k, winAlpha, passMx, passMy, dt, minecraft);
            float mainX = winX + sidebarW + 1.0f;
            float mainW = winW - sidebarW - 1.0f;
            if (searchQuery.isEmpty()) {
                Draw.text(graphics, MsdfFonts.SF_MEDIUM, Constants.getName(), mainX + 18.0f * k, winY + 12.5f * k, 10.0f * k, ColorMath.multiplyAlpha(0xFF6B7280, winAlpha));
                Draw.text(graphics, MsdfFonts.SF_MEDIUM, "/", mainX + 68.0f * k, winY + 12.5f * k, 10.0f * k, ColorMath.multiplyAlpha(0xFF4B5563, winAlpha));
                Draw.text(graphics, MsdfFonts.ICONS, getCategoryIcon(selectedCategory), mainX + 80.0f * k, winY + 12.0f * k, 11.5f * k, ColorMath.multiplyAlpha(accentPrimary, winAlpha));
                Draw.text(graphics, MsdfFonts.SF_MEDIUM, selectedCategory.getDisplayName(), mainX + 96.0f * k, winY + 12.5f * k, 10.5f * k, ColorMath.multiplyAlpha(0xFFFFFFFF, winAlpha));
            } else {
                Draw.text(graphics, MsdfFonts.SF_MEDIUM, Lang.tr("searchTitle"), mainX + 18.0f * k, winY + 12.5f * k, 10.0f * k, ColorMath.multiplyAlpha(0xFF6B7280, winAlpha));
                Draw.text(graphics, MsdfFonts.SF_MEDIUM, "\"" + searchQuery + "\"", mainX + 60.0f * k, winY + 12.5f * k, 10.0f * k, ColorMath.multiplyAlpha(accentPrimary, winAlpha));
                int countHits = getVisibleModules().size();
                Draw.text(graphics, MsdfFonts.SF_MEDIUM, "(" + countHits + ")", mainX + 65.0f * k + MsdfFonts.SF_MEDIUM.width("\"" + searchQuery + "\"", 10.0f * k), winY + 12.5f * k, 9.5f * k, ColorMath.multiplyAlpha(0xFF64748B, winAlpha));
            }
            float searchW = 145.0f * k;
            float searchH = 22.0f * k;
            float searchX = winX + winW - searchW - 16.0f * k;
            float searchY = winY + 8.5f * k;

            boolean searchHovered = (passMx >= searchX && passMx <= searchX + searchW && passMy >= searchY && passMy <= searchY + searchH);
            float searchHov = animValue("search_hov", (searchHovered || searchFocused) ? 1.0f : 0.0f, dt, 14.0f);

            int searchBg = ColorMath.lerp(0x1A141824, 0x221B2130, searchHov);
            int searchBorder = ColorMath.lerp(0x222E384D, ColorMath.multiplyAlpha(accentPrimary, 0.65f), searchFocused ? 1.0f : searchHov);

            DrawRect.create()
                    .bounds(searchX, searchY, searchW, searchH)
                    .rounding(5.0f * k)
                    .bg(searchBg)
                    .outline(0.8f, searchBorder)
                    .alpha(winAlpha)
                    .render(graphics);
            Draw.text(graphics, MsdfFonts.ICONS, "s", searchX + 8.0f * k, searchY + 5.5f * k, 10.0f * k, ColorMath.multiplyAlpha(searchFocused ? accentPrimary : 0xFF64748B, winAlpha));

            if (searchQuery.isEmpty()) {
                if (searchFocused) {
                    float pulse = 0.5f + 0.5f * (float) Math.sin((now - openedAt) / 180.0);
                    Draw.text(graphics, MsdfFonts.SF_MEDIUM, "|", searchX + 24.0f * k, searchY + 5.5f * k, 9.5f * k, ColorMath.multiplyAlpha(0xFF94A3B8, pulse * winAlpha));
                } else {
                    Draw.text(graphics, MsdfFonts.SF_MEDIUM, Lang.tr("search"), searchX + 24.0f * k, searchY + 5.5f * k, 9.5f * k, ColorMath.multiplyAlpha(0xFF5B6476, winAlpha));
                }
            } else {
                String renderQuery = searchQuery;
                float maxTextW = searchW - 32.0f * k;
                while (!renderQuery.isEmpty() && MsdfFonts.SF_MEDIUM.width(renderQuery, 9.5f * k) > maxTextW) {
                    renderQuery = renderQuery.substring(1);
                }
                Draw.text(graphics, MsdfFonts.SF_MEDIUM, renderQuery, searchX + 24.0f * k, searchY + 5.5f * k, 9.5f * k, ColorMath.multiplyAlpha(0xFFFFFFFF, winAlpha));
                if (searchFocused) {
                    float pulse = 0.5f + 0.5f * (float) Math.sin((now - openedAt) / 180.0);
                    float curX = searchX + 24.0f * k + MsdfFonts.SF_MEDIUM.width(renderQuery, 9.5f * k) + 1.0f * k;
                    Draw.text(graphics, MsdfFonts.SF_MEDIUM, "|", curX, searchY + 5.5f * k, 9.5f * k, ColorMath.multiplyAlpha(accentPrimary, pulse * winAlpha));
                }
            }

            Draw.rect(graphics, mainX + 16.0f * k, winY + 38.0f * k, mainW - 32.0f * k, 1.0f, 0.5f, ColorMath.multiplyAlpha(0x18FFFFFF, winAlpha));
            float contentY = winY + 46.0f * k;
            float padX = 16.0f * k;
            float colGap = 12.0f * k;
            float contentW = mainW - padX * 2.0f;
            float colW = (contentW - colGap) / 2.0f;
            float col1X = mainX + padX;
            float col2X = col1X + colW + colGap;

            float categoryAge = (now - categorySwitchTime) / 1000.0f;

            float clipTop = contentY - 4.0f * k;
            float clipBottom = winY + winH - 10.0f * k;
            graphics.enableScissor((int) mainX, (int) clipTop, (int) (winX + winW - 8.0f * k), (int) clipBottom);

                if (selectedCategory == Category.THEMES && searchQuery.isEmpty()) {
                    float themeCardH = 46.0f * k;
                    List<Theme> themes = Theme.ALL;

                    for (int i = 0; i < themes.size(); i++) {
                        Theme theme = themes.get(i);
                        float ap = appear(categoryAge, i);
                        float cardAlpha = ap * winAlpha;
                        if (cardAlpha <= 0.001f) continue;

                        boolean isCol1 = (i % 2 == 0);
                        int row = i / 2;
                        float cx = isCol1 ? col1X : col2X;
                        float baseCy = contentY + row * (themeCardH + 10.0f * k) - scrollOffset;

                        float slideOffset = (1.0f - ap) * 12.0f * k;
                        float cy = baseCy + slideOffset;

                        boolean hovered = (passMx >= cx && passMx <= cx + colW && passMy >= baseCy && passMy <= baseCy + themeCardH && passMy >= clipTop && passMy <= clipBottom);
                        float hov = animValue("thov_" + theme.getName(), hovered ? 1.0f : 0.0f, dt, 16.0f);
                        float cardLift = hov * 2.0f * k;
                        float drawCy = cy - cardLift;

                        boolean isCurrent = (Theme.current == theme);

                        int cardBg = ColorMath.lerp(0xEE12151F, 0xF5181E2E, hov);
                        int cardBorder = isCurrent ? theme.getFirstColor() : ColorMath.lerp(0x262D384D, 0x4B485A7E, hov);

                        DrawRect themeCard = DrawRect.create()
                                .bounds(cx, drawCy, colW, themeCardH)
                                .rounding(8.0f * k)
                                .bg(cardBg)
                                .outline(isCurrent ? 1.2f : 1.0f, cardBorder)
                                .alpha(cardAlpha);

                        if (hov > 0.01f || isCurrent) {
                            int shadowCol = isCurrent ? ColorMath.multiplyAlpha(theme.getFirstColor(), 0.35f) : ColorMath.multiplyAlpha(0x40000000, hov);
                            themeCard.shadow(10.0f * k, 0.0f, 0.0f, shadowCol);
                        }
                        themeCard.render(graphics);

                        int titleCol = isCurrent ? theme.getFirstColor() : ColorMath.lerp(0xFFE2E8F0, 0xFFFFFFFF, hov);
                        Draw.text(graphics, MsdfFonts.SF_MEDIUM, theme.getName(), cx + 12.0f * k, drawCy + 10.0f * k, 10.5f * k, ColorMath.multiplyAlpha(titleCol, cardAlpha));
                        Draw.text(graphics, MsdfFonts.SF_MEDIUM, theme.getHexDescription(), cx + 12.0f * k, drawCy + 26.5f * k, 8.0f * k, ColorMath.multiplyAlpha(0xFF758298, cardAlpha));

                        float prevW = 44.0f * k;
                        float prevH = 16.0f * k;
                        float prevX = cx + colW - 14.0f * k - prevW;
                        float prevY = drawCy + (themeCardH - prevH) / 2.0f;

                        int col1 = ColorMath.multiplyAlpha(theme.getFirstColor(), cardAlpha);
                        int col2 = ColorMath.multiplyAlpha(theme.getSecondColor(), cardAlpha);
                        Draw.flowH(graphics, prevX, prevY, prevW, prevH, 4.0f * k, col1, col2);
                        Draw.border(graphics, prevX, prevY, prevW, prevH, 4.0f * k, 0.8f, ColorMath.multiplyAlpha(0x44FFFFFF, cardAlpha));
                    }
                } else {
                    List<Module> modules = getVisibleModules();

                    if (modules.isEmpty()) {
                        String emptyMsg = searchQuery.isEmpty()
                                ? Lang.tr("emptyCategory")
                                : Lang.tr("emptySearch", searchQuery);
                        float textSize = 10.0f * k;
                        float textW = MsdfFonts.SF_MEDIUM.width(emptyMsg, textSize);
                        float textX = mainX + (mainW - textW) / 2.0f;
                        float textY = (clipTop + clipBottom) / 2.0f - textSize / 2.0f;
                        Draw.text(graphics, MsdfFonts.SF_MEDIUM, emptyMsg, textX, textY, textSize, ColorMath.multiplyAlpha(0xFF64748B, winAlpha));
                    } else {
                        float cardH = ModuleCardRender.cardHeight(k);
                        for (int i = 0; i < modules.size(); i++) {
                            Module module = modules.get(i);
                            float ap = appear(categoryAge, i);
                            float cardAlpha = ap * winAlpha;
                            if (cardAlpha <= 0.001f) continue;

                            boolean isCol1 = (i % 2 == 0);
                            int row = i / 2;
                            float cx = isCol1 ? col1X : col2X;
                            float baseCy = contentY + row * (cardH + 10.0f * k) - scrollOffset;
                            moduleCardRender.render(graphics, module, i, cx, baseCy, colW, k, cardAlpha, clipTop, clipBottom, passMx, passMy, dt, now, openedAt);
                        }
                    }
                }

                graphics.disableScissor();
            float popAnim = animValue("prof_pop_anim", profilePopupOpen ? 1.0f : 0.0f, dt, 18.0f) * winAlpha;
            if (popAnim > 0.01f) {
                profilePopupRender.render(graphics, winX, winY, sidebarW, winH, profY, k, popAnim, modalOpen ? -9999.0f : mx, modalOpen ? -9999.0f : my, dt, minecraft);
            }
            settingsPanelRender.render(graphics, k, (float) this.width, (float) this.height, winAlpha, mx, my, dt);

        } finally {
            DisplayBatcher.end();
        }
    }
}
