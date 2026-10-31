package dev.render4u.gui.impl;

import dev.render4u.gui.ClickGuiScreen;
import dev.render4u.module.setting.Setting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;

public abstract class SettingRender {
    protected final ClickGuiScreen gui;

    public SettingRender(ClickGuiScreen gui) {
        this.gui = gui;
    }

    public abstract float height(Setting<?> s, float k);

    public abstract void render(GuiGraphicsExtractor g, Setting<?> s, float x, float y, float w, float k, float a, float mx, float my, float dt);

    public abstract boolean click(Setting<?> s, float mx, float my, float x, float y, float w, float k, int button);

    public void drag(Setting<?> s, float mx, float my, float x, float y, float w, float k) {
    }

    public void release(Setting<?> s) {
    }

    public boolean charTyped(CharacterEvent event) {
        return false;
    }

    public boolean keyPressed(KeyEvent event) {
        return false;
    }
}
