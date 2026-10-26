package dev.render4u.render.font;

import java.awt.Font;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FontManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("Render4UFontManager");
    private static final Map<String, TTFFont> FONTS = new ConcurrentHashMap<>();

    public static TTFFont ROBOTO;

    private FontManager() {
    }

    public static void init() {
        if (ROBOTO != null) {
            return;
        }
        ROBOTO = register("roboto", "/assets/render4u/fonts/roboto.ttf");
    }

    public static TTFFont register(String name, String resourcePath) {
        try (InputStream stream = FontManager.class.getResourceAsStream(resourcePath)) {
            if (stream != null) {
                Font font = Font.createFont(Font.TRUETYPE_FONT, stream);
                TTFFont ttfFont = new TTFFont(name, font);
                FONTS.put(name.toLowerCase(), ttfFont);
                LOGGER.info("Registered font '{}' from classpath resource '{}'", name, resourcePath);
                return ttfFont;
            } else {
                LOGGER.warn("Could not find font resource '{}', falling back to SansSerif", resourcePath);
                return register(name, new Font("SansSerif", Font.PLAIN, 18));
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load font '{}' from '{}', falling back to SansSerif", name, resourcePath, e);
            return register(name, new Font("SansSerif", Font.PLAIN, 18));
        }
    }

    public static TTFFont register(String name, Font font) {
        TTFFont ttfFont = new TTFFont(name, font);
        FONTS.put(name.toLowerCase(), ttfFont);
        return ttfFont;
    }

    public static TTFFont get(String name) {
        return FONTS.get(name.toLowerCase());
    }
}
