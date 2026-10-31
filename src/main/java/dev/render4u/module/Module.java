package dev.render4u.module;

import com.mojang.blaze3d.platform.InputConstants;
import dev.render4u.Render4U;
import dev.render4u.module.setting.Setting;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Module {
    protected final Minecraft mc = Minecraft.getInstance();

    private final String name;
    private final String description;
    private final Category category;
    private int key;
    private boolean enabled;
    private final List<Setting<?>> settings = new ArrayList<>();

    public Module(String name, String description, Category category) {
        this(name, description, category, InputConstants.UNKNOWN.getValue());
    }

    public Module(String name, String description, Category category, int key) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.key = key;
        this.enabled = false;
    }

    public void addSettings(Setting<?>... newSettings) {
        Collections.addAll(this.settings, newSettings);
    }

    public List<Setting<?>> getSettings() {
        return Collections.unmodifiableList(settings);
    }

    public boolean hasSettings() {
        return !settings.isEmpty();
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (enabled) {
                if (Render4U.getEventBus() != null) {
                    Render4U.getEventBus().register(this);
                }
                onEnable();
            } else {
                if (Render4U.getEventBus() != null) {
                    Render4U.getEventBus().unregister(this);
                }
                onDisable();
            }
        }
    }

    public void onEnable() {
    }

    public void onDisable() {
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
