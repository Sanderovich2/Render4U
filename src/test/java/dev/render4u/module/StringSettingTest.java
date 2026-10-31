package dev.render4u.module;

import dev.render4u.module.setting.StringSetting;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StringSettingTest {

    @Test
    public void testStringSettingDefaults() {
        StringSetting setting = new StringSetting("Prefix", ".", "Enter prefix...", 10, "Chat prefix", () -> true);
        assertEquals("Prefix", setting.getName());
        assertEquals(".", setting.get());
        assertEquals("Enter prefix...", setting.getPlaceholder());
        assertEquals(10, setting.getMaxLength());
        assertTrue(setting.isVisible());
    }

    @Test
    public void testStringSettingMaxLength() {
        StringSetting setting = new StringSetting("Tag", "abc", "", 5, "Tag", () -> true);
        setting.set("123456789");
        assertEquals("12345", setting.get());
    }

    @Test
    public void testStringSettingNullSafety() {
        StringSetting setting = new StringSetting("Tag", "abc", null, 10, null, null);
        assertEquals("", setting.getPlaceholder());
        assertEquals("", setting.getDescription());
        assertTrue(setting.isVisible());
        setting.set(null);
        assertEquals("", setting.get());
    }
}
