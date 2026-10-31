package dev.render4u.theme;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.render4u.Constants;
import dev.render4u.Render4U;
import dev.render4u.config.ConfigManager;
import dev.render4u.util.Lang;
import dev.render4u.util.color.ColorUtil;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Theme {
    private static final Gson GSON = new Gson();
    private static final String THEMES_RESOURCE = "/assets/" + Constants.getModId() + "/themes.json";
    private static final String THEMES_FILE = "themes.json";

    public static final Theme PURPLE = new Theme("purple", "Фиолетовый", "theme.purple", 0xFFC9B8FB, 0xFF8B5CF6, "#C9B8FB - #8B5CF6");
    public static final Theme BLUE = new Theme("blue", "Сапфир", "theme.blue", 0xFF60A5FA, 0xFF2563EB, "#60A5FA - #2563EB");
    public static final Theme EMERALD = new Theme("emerald", "Изумруд", "theme.emerald", 0xFF34D399, 0xFF059669, "#34D399 - #059669");
    public static final Theme SUNSET = new Theme("sunset", "Закат", "theme.sunset", 0xFFFB923C, 0xFFEA580C, "#FB923C - #EA580C");
    public static final Theme CRIMSON = new Theme("crimson", "Рубин", "theme.crimson", 0xFFF87171, 0xFFDC2626, "#F87171 - #DC2626");
    public static final Theme CYAN = new Theme("cyan", "Неон", "theme.cyan", 0xFF22D3EE, 0xFF0891B2, "#22D3EE - #0891B2");

    private static final List<Theme> DEFAULT_THEMES = List.of(PURPLE, BLUE, EMERALD, SUNSET, CRIMSON, CYAN);
    public static List<Theme> ALL = loadThemes();
    public static Theme current = ALL.isEmpty() ? PURPLE : ALL.get(0);

    private final String id;
    private final String name;
    private final String translationKey;
    private final int firstColor;
    private final int secondColor;
    private final String hexDescription;

    public Theme(String id, String name, String translationKey, int firstColor, int secondColor, String hexDescription) {
        this.id = id;
        this.name = name;
        this.translationKey = translationKey;
        this.firstColor = firstColor;
        this.secondColor = secondColor;
        this.hexDescription = hexDescription;
    }

    public static List<Theme> loadThemes() {
        List<Theme> list = new ArrayList<>();

        try {
            Path customPath = ConfigManager.getDir().resolve(THEMES_FILE);
            if (Files.exists(customPath)) {
                String json = Files.readString(customPath, StandardCharsets.UTF_8);
                parseThemesJson(json, list);
            }
        } catch (Exception e) {
            Render4U.LOGGER.warn("Failed to load custom themes.json", e);
        }

        if (list.isEmpty()) {
            try (InputStream in = Theme.class.getResourceAsStream(THEMES_RESOURCE)) {
                if (in != null) {
                    String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                    parseThemesJson(json, list);
                }
            } catch (Exception e) {
                Render4U.LOGGER.warn("Failed to load bundled themes.json resource", e);
            }
        }

        if (list.isEmpty()) {
            list.addAll(DEFAULT_THEMES);
        }

        return Collections.unmodifiableList(list);
    }

    private static void parseThemesJson(String json, List<Theme> out) {
        JsonArray array = GSON.fromJson(json, JsonArray.class);
        if (array == null) return;

        for (JsonElement el : array) {
            if (!el.isJsonObject()) continue;
            JsonObject obj = el.getAsJsonObject();

            String id = obj.has("id") ? obj.get("id").getAsString() : "";
            String name = obj.has("name") ? obj.get("name").getAsString() : id;
            String translationKey = obj.has("translationKey") ? obj.get("translationKey").getAsString() : null;

            int firstColor = 0xFFFFFFFF;
            if (obj.has("firstColor")) {
                firstColor = ColorUtil.parseHex(obj.get("firstColor").getAsString());
            }

            int secondColor = firstColor;
            if (obj.has("secondColor")) {
                secondColor = ColorUtil.parseHex(obj.get("secondColor").getAsString());
            }

            String desc = obj.has("hexDescription")
                    ? obj.get("hexDescription").getAsString()
                    : ColorUtil.toRgbHex(firstColor) + " - " + ColorUtil.toRgbHex(secondColor);

            out.add(new Theme(id, name, translationKey, firstColor, secondColor, desc));
        }
    }

    public static Theme find(String nameOrId) {
        if (nameOrId == null) return current;
        for (Theme t : ALL) {
            if (t.name.equalsIgnoreCase(nameOrId)
                    || (t.id != null && t.id.equalsIgnoreCase(nameOrId))
                    || (t.translationKey != null && t.translationKey.equalsIgnoreCase(nameOrId))) {
                return t;
            }
        }
        return current;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        if (translationKey != null && Lang.has(translationKey)) {
            return Lang.tr(translationKey);
        }
        return name;
    }

    public String getRawName() {
        return name;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public int getFirstColor() {
        return firstColor;
    }

    public int getSecondColor() {
        return secondColor;
    }

    public String getHexDescription() {
        return hexDescription;
    }
}
