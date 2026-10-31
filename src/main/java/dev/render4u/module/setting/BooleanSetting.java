package dev.render4u.module.setting;

import java.util.function.Supplier;

public class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(String name, boolean defaultValue, String description, Supplier<Boolean> visibility) {
        super(name, defaultValue, description, visibility);
    }

    public BooleanSetting(String name, boolean defaultValue, String description) {
        super(name, defaultValue, description);
    }

    public BooleanSetting(String name, boolean defaultValue) {
        super(name, defaultValue);
    }

    public void toggle() {
        set(!get());
    }

    public boolean isEnabled() {
        return get();
    }
}
