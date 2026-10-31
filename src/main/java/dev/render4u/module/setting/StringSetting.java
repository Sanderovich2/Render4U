package dev.render4u.module.setting;

import java.util.function.Supplier;

public class StringSetting extends Setting<String> {
    private final String placeholder;
    private final int maxLength;

    public StringSetting(String name, String defaultValue, String placeholder, int maxLength, String description, Supplier<Boolean> visibility) {
        super(name, defaultValue, description, visibility);
        this.placeholder = placeholder != null ? placeholder : "";
        this.maxLength = maxLength > 0 ? maxLength : 256;
    }

    public StringSetting(String name, String defaultValue, String placeholder, String description) {
        this(name, defaultValue, placeholder, 256, description, () -> true);
    }

    public StringSetting(String name, String defaultValue, String placeholder) {
        this(name, defaultValue, placeholder, 256, "", () -> true);
    }

    public StringSetting(String name, String defaultValue) {
        this(name, defaultValue, "", 256, "", () -> true);
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public int getMaxLength() {
        return maxLength;
    }

    @Override
    public void set(String value) {
        if (value == null) {
            value = "";
        }
        if (value.length() > maxLength) {
            value = value.substring(0, maxLength);
        }
        super.set(value);
    }
}
