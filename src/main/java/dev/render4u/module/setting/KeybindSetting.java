package dev.render4u.module.setting;

import dev.render4u.util.KeyUtil;

import java.util.function.Supplier;

public class KeybindSetting extends Setting<Integer> {
    public KeybindSetting(String name, int defaultValue, String description, Supplier<Boolean> visibility) {
        super(name, defaultValue, description, visibility);
    }

    public KeybindSetting(String name, int defaultValue, String description) {
        this(name, defaultValue, description, () -> true);
    }

    public KeybindSetting(String name, int defaultValue) {
        super(name, defaultValue, "", () -> true);
    }

    public String getKeyName() {
        return KeyUtil.getKeyName(get());
    }
}
