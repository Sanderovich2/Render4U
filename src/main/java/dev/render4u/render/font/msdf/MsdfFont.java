package dev.render4u.render.font.msdf;

import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public final class MsdfFont {
    private static final int TAB_SPACES = 4;

    private final String name;
    private final Identifier descriptor;
    private final Identifier texture;
    private @Nullable FontData data;

    MsdfFont(String name, Identifier descriptor, Identifier texture) {
        this.name = name;
        this.descriptor = descriptor;
        this.texture = texture;
    }

    public String name() {
        return name;
    }

    public Identifier texture() {
        return texture;
    }

    public FontAtlas atlas() {
        return data().atlas();
    }

    public FontMetrics metrics() {
        return data().metrics();
    }

    public GpuTextureView textureView() {
        return Minecraft.getInstance().getTextureManager().getTexture(texture).getTextureView();
    }

    public @Nullable Glyph glyph(int codePoint) {
        if (codePoint < 32) {
            return null;
        }
        FontData font = data();
        Glyph glyph = font.glyphs().get(codePoint);
        return glyph != null ? glyph : font.fallback();
    }

    public boolean has(int codePoint) {
        return data().glyphs().containsKey(codePoint);
    }

    public float advance(int codePoint) {
        if (codePoint == '\t') {
            return advance(' ') * TAB_SPACES;
        }
        Glyph glyph = glyph(codePoint);
        return glyph != null ? glyph.advance() : 0.0F;
    }

    public float kerning(int left, int right) {
        return data().kerning().get(FontLoader.pair(left, right));
    }

    public float width(CharSequence text, float size) {
        return width(text, size, 0.0F);
    }

    public float width(CharSequence text, float size, float letterSpacing) {
        float spacing = size > 0.0F ? letterSpacing / size : 0.0F;
        float pen = 0.0F;
        int count = 0;
        int prev = -1;
        for (int i = 0; i < text.length(); ) {
            int codePoint = Character.codePointAt(text, i);
            i += Character.charCount(codePoint);
            if (codePoint < 32 && codePoint != '\t') {
                continue;
            }
            if (prev >= 0) {
                pen += kerning(prev, codePoint);
            }
            pen += advance(codePoint) + spacing;
            prev = codePoint;
            count++;
        }
        return count > 0 ? (pen - spacing) * size : 0.0F;
    }

    public float lineHeight(float size) {
        return metrics().lineHeight() * size;
    }

    public float ascent(float size) {
        return metrics().ascender() * size;
    }

    public float descent(float size) {
        return -metrics().descender() * size;
    }

    public String trim(String text, float size, float maxWidth) {
        if (width(text, size) <= maxWidth) {
            return text;
        }
        int end = 0;
        for (int i = 0; i < text.length(); ) {
            int next = i + Character.charCount(text.codePointAt(i));
            if (width(text.substring(0, next), size) > maxWidth) {
                break;
            }
            end = next;
            i = next;
        }
        return text.substring(0, end);
    }

    public Text text(String text) {
        return Text.of(this, text);
    }

    void reload() {
        data = null;
    }

    private FontData data() {
        FontData loaded = data;
        if (loaded == null) {
            loaded = FontLoader.load(descriptor);
            data = loaded;
        }
        return loaded;
    }
}
