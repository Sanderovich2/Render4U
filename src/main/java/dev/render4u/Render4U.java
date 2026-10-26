package dev.render4u;

import dev.render4u.core.event.EventBus;
import dev.render4u.feature.module.ModuleManager;
import dev.render4u.render.display.blur.Blur;
import dev.render4u.render.display.outline.OutlinePipeline;
import dev.render4u.render.display.rounded.RoundedRectPipeline;
import dev.render4u.render.display.shadow.ShadowPipeline;
import dev.render4u.render.font.FontManager;
import dev.render4u.render.font.TTFTextPipeline;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Render4U implements ClientModInitializer {
    public static final String MOD_ID = "render4u";
    public static final String CLIENT_NAME = "Render4U";
    public static final String CLIENT_VERSION = "1.0.0";
    public static final Logger LOGGER = LoggerFactory.getLogger(CLIENT_NAME);

    private static Render4U instance;
    private final EventBus eventBus = new EventBus();
    private ModuleManager moduleManager;

    @Override
    public void onInitializeClient() {
        instance = this;
        LOGGER.info("Initializing {} v{} - Modern Rendering Utility", CLIENT_NAME, CLIENT_VERSION);

        RoundedRectPipeline.init();
        ShadowPipeline.init();
        OutlinePipeline.init();
        Blur.init();
        TTFTextPipeline.init();

        FontManager.init();

        moduleManager = new ModuleManager();
        moduleManager.init();

        LOGGER.info("{} rendering utility initialized successfully!", CLIENT_NAME);
    }

    public static Render4U getInstance() {
        return instance;
    }

    public static EventBus getEventBus() {
        return instance.eventBus;
    }

    public static ModuleManager getModuleManager() {
        return instance.moduleManager;
    }
}

