package dev.render4u.render.font.msdf;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2FloatMap;
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

final class FontLoader {
    private FontLoader() {
    }

    static FontData load(Identifier id) {
        try (Reader reader = Minecraft.getInstance().getResourceManager().getResourceOrThrow(id).openAsReader()) {
            return parse(JsonParser.parseReader(reader).getAsJsonObject());
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to load font " + id, exception);
        }
    }

    static long pair(int left, int right) {
        return (long) left << 32 | right & 0xFFFFFFFFL;
    }

    private static FontData parse(JsonObject root) {
        JsonObject atlasJson = root.getAsJsonObject("atlas");
        String type = atlasJson.has("type") ? atlasJson.get("type").getAsString() : "msdf";
        boolean topOrigin = atlasJson.has("yOrigin") && atlasJson.get("yOrigin").getAsString().equalsIgnoreCase("top");
        FontAtlas atlas = new FontAtlas(type, number(atlasJson, "distanceRange"), number(atlasJson, "size"),
                atlasJson.get("width").getAsInt(), atlasJson.get("height").getAsInt());

        JsonObject metricsJson = root.getAsJsonObject("metrics");
        float em = metricsJson.has("emSize") ? number(metricsJson, "emSize") : 1.0F;
        FontMetrics metrics = new FontMetrics(
                number(metricsJson, "lineHeight") / em,
                number(metricsJson, "ascender") / em,
                number(metricsJson, "descender") / em,
                metricsJson.has("underlineY") ? number(metricsJson, "underlineY") / em : 0.0F,
                metricsJson.has("underlineThickness") ? number(metricsJson, "underlineThickness") / em : 0.0F
        );

        Int2ObjectMap<Glyph> glyphs = new Int2ObjectOpenHashMap<>();
        for (JsonElement element : root.getAsJsonArray("glyphs")) {
            JsonObject json = element.getAsJsonObject();
            if (!json.has("unicode")) {
                continue;
            }
            int unicode = json.get("unicode").getAsInt();
            float advance = number(json, "advance") / em;
            glyphs.put(unicode, json.has("planeBounds") && json.has("atlasBounds")
                    ? visible(unicode, advance, em, atlas, topOrigin, json.getAsJsonObject("planeBounds"), json.getAsJsonObject("atlasBounds"))
                    : new Glyph(unicode, advance, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        }

        Long2FloatMap kerning = new Long2FloatOpenHashMap();
        if (root.has("kerning") && root.get("kerning").isJsonArray()) {
            for (JsonElement element : root.getAsJsonArray("kerning")) {
                JsonObject json = element.getAsJsonObject();
                kerning.put(pair(json.get("unicode1").getAsInt(), json.get("unicode2").getAsInt()), number(json, "advance") / em);
            }
        }

        return new FontData(atlas, metrics, glyphs, kerning, glyphs.get('?'));
    }

    private static Glyph visible(int unicode, float advance, float em, FontAtlas atlas, boolean topOrigin, JsonObject plane, JsonObject bounds) {
        float al = number(bounds, "left");
        float ar = number(bounds, "right");
        float low = Math.min(number(bounds, "bottom"), number(bounds, "top"));
        float high = Math.max(number(bounds, "bottom"), number(bounds, "top"));
        float v0 = topOrigin ? low / atlas.height() : 1.0F - high / atlas.height();
        float v1 = topOrigin ? high / atlas.height() : 1.0F - low / atlas.height();
        return new Glyph(unicode, advance,
                number(plane, "left") / em, number(plane, "bottom") / em, number(plane, "right") / em, number(plane, "top") / em,
                al / atlas.width(), v0, ar / atlas.width(), v1);
    }

    private static float number(JsonObject json, String key) {
        return json.get(key).getAsFloat();
    }
}
