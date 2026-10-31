package dev.render4u.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.render4u.Constants;
import dev.render4u.Render4U;
import dev.render4u.gui.ClickGuiScreen;
import dev.render4u.module.Module;
import dev.render4u.module.ModuleManager;
import dev.render4u.module.setting.*;
import dev.render4u.theme.Theme;
import dev.render4u.util.Lang;
import net.minecraft.client.Minecraft;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashSet;
import java.util.Set;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String FOLDER_NAME = Constants.getModId();
    private static final String CONFIG_FILE = "config.json";

    private ConfigManager() {
    }

    public static Path getDir() {
        try {
            if (Minecraft.getInstance() != null && Minecraft.getInstance().gameDirectory != null) {
                return Minecraft.getInstance().gameDirectory.toPath().resolve(FOLDER_NAME);
            }
        } catch (Exception ignored) {
        }
        return Path.of(FOLDER_NAME);
    }

    public static Path getConfigFile() {
        return getDir().resolve(CONFIG_FILE);
    }

    public static synchronized void load() {
        Path path = getConfigFile();
        if (!Files.exists(path)) {
            return;
        }

        try {
            String json = Files.readString(path);
            JsonObject root = GSON.fromJson(json, JsonObject.class);
            if (root == null) return;
            if (root.has("uiScale") && root.get("uiScale").isJsonPrimitive()) {
                ClickGuiScreen.uiScale = Math.max(0.50f, Math.min(1.50f, root.get("uiScale").getAsFloat()));
            }
            if (root.has("compact") && root.get("compact").isJsonPrimitive()) {
                ClickGuiScreen.compact = root.get("compact").getAsBoolean();
            }
            if (root.has("theme") && root.get("theme").isJsonPrimitive()) {
                String themeName = root.get("theme").getAsString();
                Theme.current = Theme.find(themeName);
            }
            if (root.has("language") && root.get("language").isJsonPrimitive()) {
                String langName = root.get("language").getAsString();
                for (Lang l : Lang.values()) {
                    if (l.name().equalsIgnoreCase(langName)) {
                        Lang.current = l;
                        break;
                    }
                }
            }
            if (root.has("modules") && root.get("modules").isJsonObject()) {
                JsonObject modsObj = root.getAsJsonObject("modules");
                for (Module module : ModuleManager.getModules()) {
                    if (!modsObj.has(module.getName()) || !modsObj.get(module.getName()).isJsonObject()) {
                        continue;
                    }
                    JsonObject modObj = modsObj.getAsJsonObject(module.getName());

                    if (modObj.has("enabled") && modObj.get("enabled").isJsonPrimitive()) {
                        boolean enabled = modObj.get("enabled").getAsBoolean();
                        if (module.isEnabled() != enabled) {
                            module.setEnabled(enabled);
                        }
                    }

                    if (modObj.has("key") && modObj.get("key").isJsonPrimitive()) {
                        int savedKey = modObj.get("key").getAsInt();
                        if (savedKey != 0) {
                            module.setKey(savedKey);
                        }
                    }

                    if (modObj.has("settings") && modObj.get("settings").isJsonObject()) {
                        JsonObject setObj = modObj.getAsJsonObject("settings");
                        for (Setting<?> setting : module.getSettings()) {
                            if (!setObj.has(setting.getName())) continue;
                            JsonElement el = setObj.get(setting.getName());
                            try {
                                if (setting instanceof BooleanSetting bs && el.isJsonPrimitive()) {
                                    bs.set(el.getAsBoolean());
                                } else if (setting instanceof NumberSetting ns && el.isJsonPrimitive()) {
                                    ns.set(el.getAsDouble());
                                } else if (setting instanceof ModeSetting ms && el.isJsonPrimitive()) {
                                    ms.set(el.getAsString());
                                } else if (setting instanceof ColorSetting cs && el.isJsonPrimitive()) {
                                    cs.set(el.getAsInt());
                                } else if (setting instanceof KeybindSetting ks && el.isJsonPrimitive()) {
                                    ks.set(el.getAsInt());
                                } else if (setting instanceof StringSetting ss && el.isJsonPrimitive()) {
                                    ss.set(el.getAsString());
                                } else if (setting instanceof MultiModeSetting mms && el.isJsonArray()) {
                                    Set<String> selected = new LinkedHashSet<>();
                                    for (JsonElement item : el.getAsJsonArray()) {
                                        if (item.isJsonPrimitive()) {
                                            selected.add(item.getAsString());
                                        }
                                    }
                                    mms.set(selected);
                                }
                            } catch (Exception e) {
                                Render4U.LOGGER.warn("Failed to load setting {} for module {}", setting.getName(), module.getName(), e);
                            }
                        }
                    }
                }
            }
            Render4U.LOGGER.info("Config loaded successfully from {}", path);
        } catch (Exception e) {
            Render4U.LOGGER.error("Failed to load config from {}", path, e);
        }
    }

    public static synchronized void save() {
        try {
            JsonObject root = new JsonObject();
            root.addProperty("uiScale", ClickGuiScreen.uiScale);
            root.addProperty("compact", ClickGuiScreen.compact);
            if (Theme.current != null) {
                root.addProperty("theme", Theme.current.getId() != null ? Theme.current.getId() : Theme.current.getRawName());
            }
            if (Lang.current != null) {
                root.addProperty("language", Lang.current.name());
            }
            JsonObject modsObj = new JsonObject();
            for (Module module : ModuleManager.getModules()) {
                JsonObject modObj = new JsonObject();
                modObj.addProperty("enabled", module.isEnabled());
                modObj.addProperty("key", module.getKey());

                JsonObject setObj = new JsonObject();
                for (Setting<?> setting : module.getSettings()) {
                    if (setting instanceof BooleanSetting bs) {
                        setObj.addProperty(bs.getName(), bs.get());
                    } else if (setting instanceof NumberSetting ns) {
                        setObj.addProperty(ns.getName(), ns.get());
                    } else if (setting instanceof ModeSetting ms) {
                        setObj.addProperty(ms.getName(), ms.get());
                    } else if (setting instanceof ColorSetting cs) {
                        setObj.addProperty(cs.getName(), cs.get());
                    } else if (setting instanceof KeybindSetting ks) {
                        setObj.addProperty(ks.getName(), ks.get());
                    } else if (setting instanceof StringSetting ss) {
                        setObj.addProperty(ss.getName(), ss.get());
                    } else if (setting instanceof MultiModeSetting mms) {
                        JsonArray arr = new JsonArray();
                        for (String val : mms.get()) {
                            arr.add(val);
                        }
                        setObj.add(mms.getName(), arr);
                    }
                }
                modObj.add("settings", setObj);
                modsObj.add(module.getName(), modObj);
            }
            root.add("modules", modsObj);

            Path dir = getDir();
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            Path path = getConfigFile();
            Path tempPath = dir.resolve(CONFIG_FILE + ".tmp");
            Files.writeString(tempPath, GSON.toJson(root));
            try {
                Files.move(tempPath, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (Exception atomicNotSupported) {
                Files.move(tempPath, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            Render4U.LOGGER.error("Failed to save config", e);
        }
    }
}
