package dev.render4u.render.font;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FontManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("Render4UFontManager");
    private static final Map<String, Font> FONTS = new ConcurrentHashMap<>();

    public static Font ROBOTO;

    private FontManager() {
    }

    public static void init() {
        if (ROBOTO != null) {
            return;
        }
        ROBOTO = register("roboto", "/assets/render4u/fonts/roboto.ttf");
    }

    public static Font register(String name, String resourcePath) {
        try (InputStream stream = FontManager.class.getResourceAsStream(resourcePath)) {
            if (stream != null) {
                java.awt.Font font = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, stream);
                Font f = new Font(name, font);
                FONTS.put(name.toLowerCase(), f);
                LOGGER.info("Registered font '{}' from classpath resource '{}'", name, resourcePath);
                return f;
            } else {
                LOGGER.warn("Could not find font resource '{}', falling back to SansSerif", resourcePath);
                return register(name, new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 18));
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load font '{}' from '{}', falling back to SansSerif", name, resourcePath, e);
            return register(name, new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 18));
        }
    }

    public static Font register(String name, java.awt.Font font) {
        Font f = new Font(name, font);
        FONTS.put(name.toLowerCase(), f);
        return f;
    }

    public static Font get(String name) {
        return FONTS.get(name.toLowerCase());
    }
}
