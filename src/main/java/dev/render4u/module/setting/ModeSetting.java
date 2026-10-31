package dev.render4u.module.setting;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class ModeSetting extends Setting<String> {
    private final List<String> modes;

    public ModeSetting(String name, String defaultValue, List<String> modes, String description, Supplier<Boolean> visibility) {
        super(name, defaultValue, description, visibility);
        this.modes = List.copyOf(modes);
        if (!this.modes.contains(defaultValue) && !this.modes.isEmpty()) {
            set(this.modes.get(0));
        }
    }

    public ModeSetting(String name, String defaultValue, List<String> modes, String description) {
        this(name, defaultValue, modes, description, () -> true);
    }

    public ModeSetting(String name, String defaultValue, List<String> modes) {
        this(name, defaultValue, modes, "", () -> true);
    }

    public ModeSetting(String name, String defaultValue, String... modes) {
        this(name, defaultValue, Arrays.asList(modes));
    }

    public List<String> getModes() {
        return modes;
    }

    public boolean is(String mode) {
        return get() != null && get().equalsIgnoreCase(mode);
    }

    public void next() {
        if (modes.isEmpty()) return;
        int idx = modes.indexOf(get());
        set(modes.get((idx + 1) % modes.size()));
    }

    public void prev() {
        if (modes.isEmpty()) return;
        int idx = modes.indexOf(get());
        set(modes.get((idx - 1 + modes.size()) % modes.size()));
    }

    public int getIndex() {
        return modes.indexOf(get());
    }

    public void setIndex(int index) {
        if (index >= 0 && index < modes.size()) {
            set(modes.get(index));
        }
    }
}
