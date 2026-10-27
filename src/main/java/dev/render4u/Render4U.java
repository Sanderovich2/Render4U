package dev.render4u;

import dev.render4u.core.event.EventBus;
import dev.render4u.render.display.Pipelines;
import dev.render4u.render.font.msdf.MsdfFonts;
import dev.render4u.render.font.ttf.TTFFontManager;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Render4U implements ClientModInitializer {
    public static final String MOD_ID = "render4u";
    public static final String CLIENT_NAME = "Render4U";
    public static final String CLIENT_VERSION = "1.2.0";
    public static final Logger LOGGER = LoggerFactory.getLogger(CLIENT_NAME);

    private static Render4U instance;
    private final EventBus eventBus = new EventBus();

    @Override
    public void onInitializeClient() {
        instance = this;
        LOGGER.info("Initializing {} v{} - Modern Rendering Utility", CLIENT_NAME, CLIENT_VERSION);

        Pipelines.init();
        TTFFontManager.init();
        MsdfFonts.init();

        eventBus.register(this);

        LOGGER.info("{} rendering utility initialized successfully!", CLIENT_NAME);
    }

    public static Render4U getInstance() {
        return instance;
    }

    public static EventBus getEventBus() {
        return instance.eventBus;
    }
}
