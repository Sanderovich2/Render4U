package dev.render4u.render.font.msdf;

import dev.render4u.render.display.batch.DisplayBatcher;
import dev.render4u.util.color.Color;
import dev.render4u.util.color.ColorMath;
import dev.render4u.util.color.Gradient;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jspecify.annotations.Nullable;

public final class Text {
    public static final float DEFAULT_SIZE = 9.0F;

    private MsdfFont font = MsdfFonts.defaultFont();
    private String text = "";
    private float x;
    private float y;
    private float size = DEFAULT_SIZE;
    private float letterSpacing;
    private TextAlign align = TextAlign.LEFT;
    private VerticalAlign verticalAlign = VerticalAlign.TOP;
    private int solid = -1;
    private @Nullable ColorFunction function;
    private @Nullable TextEffect effect;
    private float alpha = 1.0F;

    private Text() {
    }

    public static Text create() {
        return new Text();
    }

    public static Text of(String text) {
        return new Text().text(text);
    }

    public static Text of(MsdfFont font, String text) {
        return new Text().font(font).text(text);
    }

    public static Text of(MsdfFont font, String text, float x, float y, float size) {
        return of(font, text).position(x, y).size(size);
    }

    public Text font(MsdfFont font) {
        this.font = font;
        return this;
    }

    public Text font(String name) {
        return font(MsdfFonts.get(name));
    }

    public Text text(String text) {
        this.text = text == null ? "" : text;
        return this;
    }

    public Text position(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Text size(float size) {
        this.size = size;
        return this;
    }

    public Text letterSpacing(float letterSpacing) {
        this.letterSpacing = letterSpacing;
        return this;
    }

    public Text align(TextAlign align) {
        this.align = align;
        return this;
    }

    public Text verticalAlign(VerticalAlign verticalAlign) {
        this.verticalAlign = verticalAlign;
        return this;
    }

    public Text centered() {
        return align(TextAlign.CENTER).verticalAlign(VerticalAlign.MIDDLE);
    }

    public Text centered(float x, float y) {
        return position(x, y).centered();
    }

    public Text color(int argb) {
        this.solid = argb;
        this.function = null;
        return this;
    }

    public Text color(Color color) {
        return color(color.argb());
    }

    public Text verticalGradient(int top, int bottom) {
        this.function = (glyph, px, py) -> ColorMath.lerp(top, bottom, py);
        return this;
    }

    public Text verticalGradient(Color top, Color bottom) {
        return verticalGradient(top.argb(), bottom.argb());
    }

    public Text horizontalGradient(int left, int right) {
        this.function = (glyph, px, py) -> ColorMath.lerp(left, right, px);
        return this;
    }

    public Text horizontalGradient(Color left, Color right) {
        return horizontalGradient(left.argb(), right.argb());
    }

    public Text corners(int topLeft, int topRight, int bottomRight, int bottomLeft) {
        this.function = (glyph, px, py) -> ColorMath.bilerp(topLeft, topRight, bottomRight, bottomLeft, px, py);
        return this;
    }

    public Text corners(Color topLeft, Color topRight, Color bottomRight, Color bottomLeft) {
        return corners(topLeft.argb(), topRight.argb(), bottomRight.argb(), bottomLeft.argb());
    }

    public Text gradient(Gradient gradient) {
        this.function = (glyph, px, py) -> gradient.sample(px, py);
        return this;
    }

    public Text colors(ColorFunction function) {
        this.function = function;
        return this;
    }

    public Text effect(TextEffect effect) {
        this.effect = effect;
        return this;
    }

    public Text outline(int color, float width) {
        return effect(TextEffect.outline(color, width));
    }

    public Text outline(Color color, float width) {
        return effect(TextEffect.outline(color, width));
    }

    public Text shadow(int color, float offsetX, float offsetY) {
        return effect(TextEffect.shadow(color, offsetX, offsetY));
    }

    public Text shadow(int color, float offsetX, float offsetY, float blur) {
        return effect(TextEffect.shadow(color, offsetX, offsetY, blur));
    }

    public Text shadow(Color color, float offsetX, float offsetY) {
        return effect(TextEffect.shadow(color, offsetX, offsetY));
    }

    public Text shadow(Color color, float offsetX, float offsetY, float blur) {
        return effect(TextEffect.shadow(color, offsetX, offsetY, blur));
    }

    public Text glow(int color, float blur) {
        return effect(TextEffect.glow(color, blur));
    }

    public Text glow(Color color, float blur) {
        return effect(TextEffect.glow(color, blur));
    }

    public Text alpha(float alpha) {
        this.alpha = Math.clamp(alpha, 0.0F, 1.0F);
        return this;
    }

    public MsdfFont font() {
        return font;
    }

    public String text() {
        return text;
    }

    public TextLayout layout() {
        return TextLayout.of(font, text, letterSpacing / Math.max(size, 1.0E-3F));
    }

    public float width() {
        return layout().width() * size;
    }

    public float height() {
        return layout().height() * size;
    }

    public TextBounds bounds() {
        TextLayout layout = layout();
        return new TextBounds(originX(layout), originY(layout), layout.width() * size, layout.height() * size);
    }

    public TextRenderState build(GuiGraphicsExtractor graphics) {
        return build(graphics, layout());
    }

    public void render(GuiGraphicsExtractor graphics) {
        if (text.isEmpty() || size <= 0.0F || alpha <= 0.0F) {
            return;
        }
        TextLayout layout = layout();
        if (layout.glyphCount() == 0) {
            return;
        }

        if (effect != null && (effect.color() >>> 24) > 0) {
            int outlineByte = Math.clamp(Math.round(effect.spread() * 40.0F), 0, 255);
            int blurByte = Math.clamp(Math.round(effect.blur() * 20.0F), 0, 255);
            int packedEffect = outlineByte | (blurByte << 8);

            float effX = originX(layout) + effect.offsetX();
            float effY = originY(layout) + effect.offsetY();
            TextRenderState effectState = new TextRenderState(
                    layout, DisplayBatcher.pose(graphics), effX, effY, size,
                    effect.color(), null, alpha, packedEffect, graphics.scissorStack.peek()
            );
            DisplayBatcher.submit(graphics, effectState);
        }

        DisplayBatcher.submit(graphics, build(graphics, layout));
    }

    private TextRenderState build(GuiGraphicsExtractor graphics, TextLayout layout) {
        return new TextRenderState(layout, DisplayBatcher.pose(graphics), originX(layout), originY(layout), size, solid, function, alpha, 0, graphics.scissorStack.peek());
    }

    private float originX(TextLayout layout) {
        return x - layout.width() * size * align.factor();
    }

    private float originY(TextLayout layout) {
        return switch (verticalAlign) {
            case TOP -> y;
            case MIDDLE -> y - layout.height() * size * 0.5F;
            case BOTTOM -> y - layout.height() * size;
            case BASELINE -> y - layout.ascender() * size;
        };
    }
}
