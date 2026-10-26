package dev.render4u.feature.module;

import dev.render4u.Render4U;

public abstract class Module {
    private final String name;
    private final String description;
    private final Category category;
    private int key;
    private boolean enabled;

    public Module(String name, String description, Category category) {
        this(name, description, category, 0);
    }

    public Module(String name, String description, Category category, int key) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.key = key;
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (enabled) {
                Render4U.getEventBus().register(this);
                onEnable();
            } else {
                Render4U.getEventBus().unregister(this);
                onDisable();
            }
        }
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public boolean isEnabled() {
        return enabled;
    }
}

