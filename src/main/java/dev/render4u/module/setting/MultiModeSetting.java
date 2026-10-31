package dev.render4u.module.setting;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class MultiModeSetting extends Setting<Set<String>> {
    private final List<String> modes;

    public MultiModeSetting(String name, Set<String> defaultValue, List<String> modes, String description, Supplier<Boolean> visibility) {
        super(name, new LinkedHashSet<>(defaultValue), description, visibility);
        this.modes = List.copyOf(modes);
    }

    public MultiModeSetting(String name, Set<String> defaultValue, List<String> modes, String description) {
        this(name, defaultValue, modes, description, () -> true);
    }

    public MultiModeSetting(String name, Set<String> defaultValue, List<String> modes) {
        this(name, defaultValue, modes, "", () -> true);
    }

    public MultiModeSetting(String name, Set<String> defaultValue, String... modes) {
        this(name, defaultValue, Arrays.asList(modes));
    }

    public List<String> getModes() {
        return modes;
    }

    public boolean isSelected(String mode) {
        return get() != null && get().contains(mode);
    }

    public void toggle(String mode) {
        Set<String> newSet = new LinkedHashSet<>(get());
        if (newSet.contains(mode)) {
            newSet.remove(mode);
        } else {
            newSet.add(mode);
        }
        set(newSet);
    }

    public void selectAll() {
        set(new LinkedHashSet<>(modes));
    }

    public void deselectAll() {
        set(new LinkedHashSet<>());
    }
}
