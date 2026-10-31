package dev.render4u.module.setting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class Setting<T> {
    private final String name;
    private final String description;
    private T value;
    private final T defaultValue;
    private final Supplier<Boolean> visibility;
    private final List<Consumer<T>> listeners = new ArrayList<>();

    public Setting(String name, T defaultValue, String description, Supplier<Boolean> visibility) {
        this.name = name;
        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.description = description != null ? description : "";
        this.visibility = visibility != null ? visibility : () -> true;
    }

    public Setting(String name, T defaultValue, String description) {
        this(name, defaultValue, description, () -> true);
    }

    public Setting(String name, T defaultValue) {
        this(name, defaultValue, "", () -> true);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public T get() {
        return value;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public boolean isVisible() {
        return visibility == null || visibility.get();
    }

    public void set(T newValue) {
        if (newValue != null && !newValue.equals(this.value)) {
            this.value = newValue;
            for (Consumer<T> listener : listeners) {
                listener.accept(newValue);
            }
        }
    }

    public void reset() {
        set(defaultValue);
    }

    public Setting<T> addListener(Consumer<T> listener) {
        if (listener != null) {
            listeners.add(listener);
        }
        return this;
    }
}
