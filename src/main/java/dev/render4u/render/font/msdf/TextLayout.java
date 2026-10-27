package dev.render4u.render.font.msdf;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public final class TextLayout {
    public static final int STRIDE = 8;

    private static final int CACHE_SIZE = 1024;
    private static final Map<Key, TextLayout> CACHE = new LinkedHashMap<>(256, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Key, TextLayout> eldest) {
            return size() > CACHE_SIZE;
        }
    };

    private final MsdfFont font;
    private final float[] quads;
    private final int[] indices;
    private final int count;
    private final float width;
    private final float height;
    private final float ascender;

    private TextLayout(MsdfFont font, float[] quads, int[] indices, int count, float width, float height, float ascender) {
        this.font = font;
        this.quads = quads;
        this.indices = indices;
        this.count = count;
        this.width = width;
        this.height = height;
        this.ascender = ascender;
    }

    public static TextLayout of(MsdfFont font, String text, float letterSpacing) {
        return CACHE.computeIfAbsent(new Key(font, text, letterSpacing), TextLayout::compute);
    }

    public static void clear() {
        CACHE.clear();
    }

    public MsdfFont font() {
        return font;
    }

    public float[] quads() {
        return quads;
    }

    public int[] indices() {
        return indices;
    }

    public int glyphCount() {
        return count;
    }

    public float width() {
        return width;
    }

    public float height() {
        return height;
    }

    public float ascender() {
        return ascender;
    }

    private static TextLayout compute(Key key) {
        MsdfFont font = key.font();
        int[] codePoints = key.text().codePoints().toArray();
        float spacing = key.letterSpacing();
        float ascender = font.metrics().ascender();
        float[] data = new float[codePoints.length * STRIDE];
        int[] indices = new int[codePoints.length];
        int count = 0;
        int visible = 0;
        float pen = 0.0F;
        int prev = -1;
        for (int i = 0; i < codePoints.length; i++) {
            int codePoint = codePoints[i];
            if (codePoint < 32 && codePoint != '\t') {
                continue;
            }
            if (prev >= 0) {
                pen += font.kerning(prev, codePoint);
            }
            Glyph glyph = font.glyph(codePoint);
            if (glyph != null && glyph.visible()) {
                int o = count * STRIDE;
                data[o] = pen + glyph.left();
                data[o + 1] = ascender - glyph.top();
                data[o + 2] = pen + glyph.right();
                data[o + 3] = ascender - glyph.bottom();
                data[o + 4] = glyph.u0();
                data[o + 5] = glyph.v0();
                data[o + 6] = glyph.u1();
                data[o + 7] = glyph.v1();
                indices[count++] = i;
            }
            pen += font.advance(codePoint) + spacing;
            prev = codePoint;
            visible++;
        }
        float width = visible > 0 ? pen - spacing : 0.0F;
        return new TextLayout(font, Arrays.copyOf(data, count * STRIDE), Arrays.copyOf(indices, count), count, width, font.metrics().lineHeight(), ascender);
    }

    private record Key(MsdfFont font, String text, float letterSpacing) {
    }
}
