package dev.render4u;

import dev.render4u.config.ConfigManager;
import dev.render4u.core.event.EventBus;
import dev.render4u.core.event.EventTarget;
import dev.render4u.core.event.impl.EventKey;
import dev.render4u.module.Module;
import dev.render4u.module.ModuleManager;
import dev.render4u.render.display.Pipelines;
import dev.render4u.render.font.msdf.MsdfFonts;
import dev.render4u.render.font.ttf.TTFFontManager;
import dev.render4u.util.KeyUtil;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Render4U implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(Constants.getName());

    private static Render4U instance;
    private final EventBus eventBus = new EventBus();
    private ModuleManager moduleManager;

    @Override
    public void onInitializeClient() {
        instance = this;
        LOGGER.info("Initializing {} v{} - Modern Rendering Utility", Constants.getName(), Constants.getVersion());

        Pipelines.init();
        TTFFontManager.init();
        MsdfFonts.init();

        moduleManager = new ModuleManager();
        moduleManager.init();
        ConfigManager.load();
        Runtime.getRuntime().addShutdownHook(new Thread(ConfigManager::save, "Render4U-Config-Shutdown"));

        eventBus.register(this);

        LOGGER.info("{} initialized successfully!", Constants.getName());
    }

    @EventTarget
    public void onKey(EventKey event) {
        Minecraft mc = Minecraft.getInstance();
        if (!event.isPressed() || (mc.gui != null && mc.gui.screen() != null)) {
            return;
        }

        for (Module module : ModuleManager.getModules()) {
            if (KeyUtil.isKeyMatch(module.getKey(), event.getKey(), event.getScancode())) {
                module.toggle();
                event.setCancelled(true);
                break;
            }
        }
    }

    public static Render4U getInstance() {
        return instance;
    }

    public static EventBus getEventBus() {
        return instance != null ? instance.eventBus : null;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }
}
