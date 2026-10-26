package dev.render4u.util.color;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ColorUtilTest {

    @Test
    void parsesShortHex() {
        assertEquals(0xFFFFFFFF, ColorUtil.parseHex("#FFF"));
        assertEquals(0xFF123456, ColorUtil.parseHex("#123456"));
        assertEquals(0x80123456, ColorUtil.parseHex("#80123456"));
    }

    @Test
    void parsesPrefixedHex() {
        assertEquals(0xFF123456, ColorUtil.parseHex("0x123456"));
        assertEquals(0xFF123456, ColorUtil.parseHex("0X123456"));
        assertEquals(0xFF123456, ColorUtil.parseHex("123456"));
    }

    @Test
    void parsesHexWithAlphaPrefix() {
        assertEquals(0x10123456, ColorUtil.parseHex("#10123456"));
    }

    @Test
    void rejectsInvalidHex() {
        assertThrows(IllegalArgumentException.class, () -> ColorUtil.parseHex("#1"));
        assertThrows(IllegalArgumentException.class, () -> ColorUtil.parseHex("#12345"));
        assertThrows(IllegalArgumentException.class, () -> ColorUtil.parseHex("#1234567"));
        assertThrows(IllegalArgumentException.class, () -> ColorUtil.parseHex(null));
        assertThrows(IllegalArgumentException.class, () -> ColorUtil.parseHex(""));
    }

    @Test
    void formatsHexOutput() {
        assertEquals("#FF123456", ColorUtil.toHex(0xFF123456));
        assertEquals("#123456", ColorUtil.toRgbHex(0x80123456));
    }

    @Test
    void buildsRgbaInts() {
        assertEquals(0xFF102030, ColorUtil.rgb(0x10, 0x20, 0x30));
        assertEquals(0x40102030, ColorUtil.rgba(0x10, 0x20, 0x30, 0x40));
        assertEquals(0x40FFFFFF, ColorUtil.rgba(1.0F, 1.0F, 1.0F, 0.251F));
    }

    @Test
    void colorComponentsRoundTrip() {
        Color color = Color.rgba(0x10, 0x20, 0x30, 0x40);
        assertEquals(0x40, color.alpha());
        assertEquals(0x10, color.red());
        assertEquals(0x20, color.green());
        assertEquals(0x30, color.blue());
        assertEquals("#40102030", color.toHex());
        assertEquals("#102030", color.toRgbHex());
    }

    @Test
    void withAlphaReplacesOnlyAlpha() {
        assertEquals(0x10123456, ColorUtil.withAlpha(0xFF123456, 0x10));
        assertEquals(0x00FFFFFF, ColorUtil.withAlpha(0xFFFFFFFF, 0x00));
    }

    @Test
    void multiplyAlphaScalesAlphaChannel() {
        assertEquals(0xFF000000, ColorUtil.multiplyAlpha(0xFF000000, 1.0F));
        assertEquals(0x80000000, ColorUtil.multiplyAlpha(0xFF000000, 0.5F));
        assertEquals(0x00000000, ColorUtil.multiplyAlpha(0xFF000000, 0.0F));
        assertEquals(0x80FFFFFF, ColorUtil.multiplyAlpha(0xFFFFFFFF, 0.5F));
    }

    @Test
    void lerpInterpolatesChannels() {
        assertEquals(0xFF000000, ColorUtil.lerp(0xFF000000, 0xFF0000FF, 0.0F));
        assertEquals(0xFF0000FF, ColorUtil.lerp(0xFF000000, 0xFF0000FF, 1.0F));
        assertEquals(0xFF000080, ColorUtil.lerp(0xFF000000, 0xFF0000FF, 0.5F));
        assertEquals(0xFF808080, ColorUtil.lerp(0xFF000000, 0xFFFFFFFF, 0.5F));
    }

    @Test
    void bilerpCombinesFourCorners() {
        int black = 0xFF000000;
        int white = 0xFFFFFFFF;
        assertEquals(black, ColorUtil.bilerp(black, white, white, black, 0.0F, 0.0F));
        assertEquals(white, ColorUtil.bilerp(black, white, white, black, 1.0F, 0.0F));
        assertEquals(0xFF808080, ColorUtil.bilerp(black, white, white, black, 0.5F, 0.5F));
    }

    @Test
    void hsbProducesKnownColors() {
        assertEquals(0xFFFFFFFF, ColorUtil.hsb(0.0F, 0.0F, 1.0F));
        assertEquals(0xFF00FF00, ColorUtil.hsb(0.33333F, 1.0F, 1.0F));
        assertEquals(0xFFFF0000, ColorUtil.hsb(0.0F, 1.0F, 1.0F));
        assertEquals(0x80FF0000, ColorUtil.hsb(0.0F, 1.0F, 1.0F, 0.5F));
    }

    @Test
    void colorAlphaFactoryScalesAlpha() {
        assertEquals(0x80000000, Color.BLACK.alpha(0.5F).argb());
        assertEquals(0xFF000000, Color.BLACK.alpha(2.0F).argb());
        assertEquals(0x00000000, Color.BLACK.alpha(-1.0F).argb());
    }
}
