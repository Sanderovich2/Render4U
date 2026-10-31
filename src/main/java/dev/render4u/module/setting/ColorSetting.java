package dev.render4u.module.setting;

import java.awt.Color;
import java.util.Locale;
import java.util.function.Supplier;

public class ColorSetting extends Setting<Integer> {
    private final boolean hasAlpha;
    private float cachedHue = -1.0f;
    private float cachedSat = 0.0f;
    private float cachedBri = 1.0f;
    private boolean settingFromHsb = false;

    public ColorSetting(String name, int defaultArgb, boolean hasAlpha, String description, Supplier<Boolean> visibility) {
        super(name, defaultArgb, description, visibility);
        this.hasAlpha = hasAlpha;
    }

    public ColorSetting(String name, int defaultArgb, boolean hasAlpha, String description) {
        this(name, defaultArgb, hasAlpha, description, () -> true);
    }

    public ColorSetting(String name, int defaultArgb, boolean hasAlpha) {
        this(name, defaultArgb, hasAlpha, "", () -> true);
    }

    public ColorSetting(String name, int defaultArgb) {
        this(name, defaultArgb, true, "", () -> true);
    }

    public ColorSetting(String name, int r, int g, int b, int a) {
        this(name, ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF), true);
    }

    public ColorSetting(String name, int r, int g, int b) {
        this(name, (0xFF << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF), false);
    }

    public boolean hasAlpha() {
        return hasAlpha;
    }

    public int getAlpha() {
        return (get() >> 24) & 0xFF;
    }

    public int getRed() {
        return (get() >> 16) & 0xFF;
    }

    public int getGreen() {
        return (get() >> 8) & 0xFF;
    }

    public int getBlue() {
        return get() & 0xFF;
    }

    @Override
    public void set(Integer newValue) {
        super.set(newValue);
        if (!settingFromHsb && newValue != null) {
            float[] hsb = new float[3];
            Color.RGBtoHSB((newValue >> 16) & 0xFF, (newValue >> 8) & 0xFF, newValue & 0xFF, hsb);
            cachedHue = hsb[0];
            cachedSat = hsb[1];
            cachedBri = hsb[2];
        }
    }

    public float[] getHSB() {
        if (cachedHue < 0.0f) {
            float[] hsb = new float[3];
            Color.RGBtoHSB(getRed(), getGreen(), getBlue(), hsb);
            cachedHue = hsb[0];
            cachedSat = hsb[1];
            cachedBri = hsb[2];
        }
        return new float[]{cachedHue, cachedSat, cachedBri};
    }

    public float getHue() {
        return getHSB()[0];
    }

    public float getSaturation() {
        return getHSB()[1];
    }

    public float getBrightness() {
        return getHSB()[2];
    }

    public void setHSB(float hue, float saturation, float brightness, float alpha) {
        cachedHue = Math.max(0.0f, Math.min(1.0f, hue));
        cachedSat = Math.max(0.0f, Math.min(1.0f, saturation));
        cachedBri = Math.max(0.0f, Math.min(1.0f, brightness));
        int rgb = Color.HSBtoRGB(cachedHue, cachedSat, cachedBri) & 0x00FFFFFF;
        int a = Math.round(Math.max(0.0f, Math.min(1.0f, alpha)) * 255.0f);
        settingFromHsb = true;
        set((a << 24) | rgb);
        settingFromHsb = false;
    }

    public void setRGBA(int r, int g, int b, int a) {
        set(((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF));
    }

    public String toHex() {
        if (hasAlpha) {
            return String.format(Locale.US, "#%02X%02X%02X%02X", getAlpha(), getRed(), getGreen(), getBlue());
        } else {
            return String.format(Locale.US, "#%02X%02X%02X", getRed(), getGreen(), getBlue());
        }
    }
}
