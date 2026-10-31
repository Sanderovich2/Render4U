package dev.render4u;

import dev.render4u.module.Category;
import dev.render4u.theme.Theme;
import dev.render4u.util.KeyUtil;
import dev.render4u.util.Lang;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LangAndThemeTest {

    @Test
    void testTranslationsLoaded() {
        Lang.setLanguage(Lang.RU);
        assertEquals("Тема", Lang.tr("theme"));
        assertEquals("Масштаб GUI", Lang.tr("guiScale"));
        assertEquals("ВКЛ", Lang.tr("on"));
        assertEquals("ВЫКЛ", Lang.tr("off"));
        assertEquals("Бой", Category.COMBAT.getDisplayName());
        assertEquals("Рендер", Category.RENDER.getDisplayName());

        Lang.setLanguage(Lang.EN);
        assertEquals("Theme", Lang.tr("theme"));
        assertEquals("GUI Scale", Lang.tr("guiScale"));
        assertEquals("ON", Lang.tr("on"));
        assertEquals("OFF", Lang.tr("off"));
        assertEquals("Combat", Category.COMBAT.getDisplayName());
        assertEquals("Render", Category.RENDER.getDisplayName());

        Lang.setLanguage(Lang.RU);
    }

    @Test
    void testThemesLoadedFromJson() {
        assertNotNull(Theme.ALL);
        assertFalse(Theme.ALL.isEmpty());
        assertTrue(Theme.ALL.size() >= 6);

        Theme purple = Theme.find("purple");
        assertNotNull(purple);
        assertEquals(0xFFC9B8FB, purple.getFirstColor());
        assertEquals(0xFF8B5CF6, purple.getSecondColor());

        Theme blue = Theme.find("blue");
        assertNotNull(blue);
        assertEquals(0xFF60A5FA, blue.getFirstColor());
    }

    @Test
    void testKeyMatchShiftAliases() {
        assertTrue(KeyUtil.isKeyMatch(344, 344, 0));
        assertTrue(KeyUtil.isKeyMatch(344, 0, 229));
        assertTrue(KeyUtil.isKeyMatch(340, 340, 0));
        assertTrue(KeyUtil.isKeyMatch(340, 0, 225));
    }

    @Test
    void testConstants() {
        assertEquals("render4u", Constants.getModId());
        assertEquals("Render4U", Constants.getName());
        assertEquals("1.3.0", Constants.getVersion());
    }
}
