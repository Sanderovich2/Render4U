package dev.render4u.render.font;

import java.awt.Font;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FontManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("Render4UFontManager");

    public static TTFFontRenderer ROBOTO_12;
    public static TTFFontRenderer ROBOTO_14;
    public static TTFFontRenderer ROBOTO_16;
    public static TTFFontRenderer ROBOTO_18;
    public static TTFFontRenderer ROBOTO_20;
    public static TTFFontRenderer ROBOTO_24;

    private FontManager() {
    }

    public static void init() {
        Font baseFont;
        try (InputStream stream = FontManager.class.getResourceAsStream("/assets/render4u/fonts/roboto.ttf")) {
            if (stream != null) {
                baseFont = Font.createFont(Font.TRUETYPE_FONT, stream);
                LOGGER.info("Successfully loaded Roboto TTF from classpath resource!");
            } else {
                LOGGER.warn("Could not find /assets/render4u/fonts/roboto.ttf on classpath, using SansSerif");
                baseFont = new Font("SansSerif", Font.PLAIN, 18);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load Roboto TTF font, falling back to SansSerif", e);
            baseFont = new Font("SansSerif", Font.PLAIN, 18);
        }

        ROBOTO_12 = new TTFFontRenderer("Roboto", baseFont, 12.0F);
        ROBOTO_14 = new TTFFontRenderer("Roboto", baseFont, 14.0F);
        ROBOTO_16 = new TTFFontRenderer("Roboto", baseFont, 16.0F);
        ROBOTO_18 = new TTFFontRenderer("Roboto", baseFont, 18.0F);
        ROBOTO_20 = new TTFFontRenderer("Roboto", baseFont, 20.0F);
        ROBOTO_24 = new TTFFontRenderer("Roboto", baseFont, 24.0F);
    }
}

