package dev.render4u.render.font.msdf;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;

public final class MsdfFonts {
    private static final String NAMESPACE = "render4u";
    private static final String DIRECTORY = "fonts/msdf/";
    private static final Map<String, MsdfFont> REGISTRY = new LinkedHashMap<>();

    public static final MsdfFont SF_MEDIUM = register("sfmedium");
    public static final MsdfFont SF_SEMIBOLD = register("sfsemibold");
    public static final MsdfFont SF_BOLD = register("sfbold");

    private static MsdfFont defaultFont = SF_MEDIUM;

    private MsdfFonts() {
    }

    public static void init() {
    }

    public static MsdfFont register(String name) {
        return register(name, DIRECTORY + name);
    }

    public static MsdfFont register(String name, String path) {
        return REGISTRY.computeIfAbsent(name, key -> new MsdfFont(key, id(path + ".json"), id(path + ".png")));
    }

    public static MsdfFont get(String name) {
        MsdfFont font = REGISTRY.get(name);
        if (font == null) {
            throw new IllegalArgumentException("Unknown font: " + name);
        }
        return font;
    }

    public static boolean has(String name) {
        return REGISTRY.containsKey(name);
    }

    public static Collection<MsdfFont> all() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }

    public static MsdfFont defaultFont() {
        return defaultFont;
    }

    public static void defaultFont(MsdfFont font) {
        defaultFont = font;
    }

    public static void reload() {
        REGISTRY.values().forEach(MsdfFont::reload);
        TextLayout.clear();
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(NAMESPACE, path);
    }
}
