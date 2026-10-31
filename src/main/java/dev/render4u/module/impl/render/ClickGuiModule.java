package dev.render4u.module.impl.render;

import com.mojang.blaze3d.platform.InputConstants;
import dev.render4u.gui.ClickGuiScreen;
import dev.render4u.module.Category;
import dev.render4u.module.Module;
import net.minecraft.client.gui.screens.Screen;

public final class ClickGuiModule extends Module {
    public ClickGuiModule() {
        super("ClickGUI", "Открывает графический интерфейс клиента", Category.RENDER, InputConstants.KEY_RSHIFT);
    }

    @Override
    public void onEnable() {
        mc.execute(() -> {
            Screen cur = mc.gui != null ? mc.gui.screen() : null;
            if (!(cur instanceof ClickGuiScreen)) {
                ClickGuiScreen screen = new ClickGuiScreen();
                if (mc.gui != null) {
                    mc.gui.setScreen(screen);
                } else {
                    mc.setScreenAndShow(screen);
                }
            }
        });
    }

    @Override
    public void onDisable() {
        mc.execute(() -> {
            Screen cur = mc.gui != null ? mc.gui.screen() : null;
            if (cur instanceof ClickGuiScreen cgs) {
                cgs.onClose();
            }
        });
    }
}
