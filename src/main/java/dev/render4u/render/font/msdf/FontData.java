package dev.render4u.render.font.msdf;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2FloatMap;
import org.jspecify.annotations.Nullable;

record FontData(FontAtlas atlas, FontMetrics metrics, Int2ObjectMap<Glyph> glyphs, Long2FloatMap kerning, @Nullable Glyph fallback) {
}
