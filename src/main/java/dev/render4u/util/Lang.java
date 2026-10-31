package dev.render4u.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.render4u.Render4U;
import dev.render4u.config.ConfigManager;
import net.minecraft.client.Minecraft;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public enum Lang {
    RU("Русский", "ru_ru"),
    EN("English", "en_us");

    private static final Gson GSON = new Gson();
    private static final Type MAP_TYPE = new TypeToken<Map<String, String>>() {}.getType();
    private static final Map<Lang, Map<String, String>> TRANSLATIONS = new EnumMap<>(Lang.class);

    public static Lang current = RU;

    static {
        loadTranslations();
    }

    private final String displayName;
    private final String mcCode;

    Lang(String displayName, String mcCode) {
        this.displayName = displayName;
        this.mcCode = mcCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getMcCode() {
        return mcCode;
    }

    public static synchronized void loadTranslations() {
        TRANSLATIONS.clear();
        for (Lang lang : values()) {
            Map<String, String> map = new HashMap<>();

            String resourcePath = "/assets/render4u/lang/" + lang.getMcCode() + ".json";
            try (InputStream in = Lang.class.getResourceAsStream(resourcePath)) {
                if (in != null) {
                    Map<String, String> res = GSON.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), MAP_TYPE);
                    if (res != null) {
                        map.putAll(res);
                    }
                }
            } catch (Exception e) {
                Render4U.LOGGER.warn("Failed to load bundled lang resource {}", resourcePath, e);
            }

            try {
                Path customFile = ConfigManager.getDir().resolve("lang").resolve(lang.getMcCode() + ".json");
                if (Files.exists(customFile)) {
                    String content = Files.readString(customFile, StandardCharsets.UTF_8);
                    Map<String, String> customMap = GSON.fromJson(content, MAP_TYPE);
                    if (customMap != null) {
                        map.putAll(customMap);
                    }
                }
            } catch (Exception e) {
                Render4U.LOGGER.warn("Failed to load custom lang file for {}", lang.getMcCode(), e);
            }

            TRANSLATIONS.put(lang, map);
        }
    }

    public static void setLanguage(Lang lang) {
        current = lang;
        applyToMinecraft(lang);
    }

    public static void applyToMinecraft(Lang lang) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.getLanguageManager() == null) return;
            String code = lang.getMcCode();
            if (!code.equals(mc.getLanguageManager().getSelected())) {
                mc.getLanguageManager().setSelected(code);
                mc.options.languageCode = code;
                mc.options.save();
                mc.reloadResourcePacks();
            }
        } catch (Throwable ignored) {
        }
    }

    public static boolean has(String key) {
        if (key == null) return false;
        Map<String, String> currentMap = TRANSLATIONS.get(current);
        if (currentMap != null && currentMap.containsKey(key)) {
            return true;
        }
        Map<String, String> fallbackMap = TRANSLATIONS.get(EN);
        return fallbackMap != null && fallbackMap.containsKey(key);
    }

    public static String tr(String key, Object... args) {
        if (key == null) {
            return "";
        }

        String template = null;
        Map<String, String> currentMap = TRANSLATIONS.get(current);
        if (currentMap != null) {
            template = currentMap.get(key);
        }

        if (template == null) {
            Map<String, String> fallbackMap = TRANSLATIONS.get(EN);
            if (fallbackMap != null) {
                template = fallbackMap.get(key);
            }
        }

        if (template == null) {
            template = key;
        }

        if (args != null && args.length > 0) {
            try {
                return String.format(template, args);
            } catch (Exception e) {
                return template;
            }
        }
        return template;
    }
}
