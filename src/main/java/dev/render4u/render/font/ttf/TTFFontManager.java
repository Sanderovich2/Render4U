package dev.render4u.render.font.ttf;

import dev.render4u.Constants;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TTFFontManager {
    public static final String DIRECTORY = "/assets/" + Constants.getModId() + "/fonts/ttf/";
    private static final Logger LOGGER = LoggerFactory.getLogger("Render4UTTFFontManager");
    private static final Map<String, TTFFont> FONTS = new ConcurrentHashMap<>();

    public static TTFFont ROBOTO;

    private TTFFontManager() {
    }

    public static void init() {
        if (ROBOTO != null) {
            return;
        }
        ROBOTO = register("roboto", DIRECTORY + "roboto.ttf");
    }

    public static TTFFont register(String name, String resourcePath) {
        try (InputStream stream = TTFFontManager.class.getResourceAsStream(resourcePath)) {
            if (stream != null) {
                java.awt.Font font = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, stream);
                TTFFont f = new TTFFont(name, font);
                FONTS.put(name.toLowerCase(), f);
                LOGGER.info("Registered TTF font '{}' from classpath resource '{}'", name, resourcePath);
                return f;
            } else {
                LOGGER.warn("Could not find TTF font resource '{}', falling back to SansSerif", resourcePath);
                return register(name, new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 18));
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load TTF font '{}' from '{}', falling back to SansSerif", name, resourcePath, e);
            return register(name, new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 18));
        }
    }

    public static TTFFont register(String name, java.awt.Font font) {
        TTFFont f = new TTFFont(name, font);
        FONTS.put(name.toLowerCase(), f);
        return f;
    }

    public static TTFFont get(String name) {
        if (name == null) {
            return null;
        }
        return FONTS.get(name.toLowerCase());
    }
}
