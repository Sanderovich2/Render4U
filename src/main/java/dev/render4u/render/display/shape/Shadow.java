package dev.render4u.render.display.shape;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import dev.render4u.render.display.Pipelines;
import dev.render4u.render.display.batch.DisplayBatcher;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.jspecify.annotations.Nullable;
import org.joml.Matrix3x2f;

public final class Shadow extends RectBuilder<Shadow> {
    public static final float DEFAULT_BLUR = 8.0F;

    private float offsetX;
    private float offsetY;
    private float spread;
    private float blur = DEFAULT_BLUR;
    private float strength = 1.0F;

    private Shadow() {
        color(0x80000000);
    }

    public static Shadow create() {
        return new Shadow();
    }

    public static Shadow of(float x, float y, float width, float height) {
        return new Shadow().position(x, y).size(width, height);
    }

    public Shadow offset(float offsetX, float offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        return self();
    }

    public Shadow spread(float spread) {
        this.spread = spread;
        return self();
    }

    public Shadow blur(float blur) {
        this.blur = Math.max(blur, 0.0F);
        return self();
    }

    public Shadow strength(float strength) {
        this.strength = Math.clamp(strength, 1.0F, 4.0F);
        return self();
    }

    public RenderState build(GuiGraphicsExtractor graphics) {
        float drawX = x + offsetX - spread;
        float drawY = y + offsetY - spread;
        float drawWidth = width + spread * 2.0F;
        float drawHeight = height + spread * 2.0F;
        float maxRadius = Math.min(drawWidth, drawHeight) * 0.5F;
        int topLeft = packRadius(radiusTopLeft + spread, maxRadius, 1.0F, 5);
        int topRight = packRadius(radiusTopRight + spread, maxRadius, 1.0F, 5);
        int bottomRight = packRadius(radiusBottomRight + spread, maxRadius, 1.0F, 5);
        int bottomLeft = packRadius(radiusBottomLeft + spread, maxRadius, 1.0F, 5);
        int packedBlur = Math.clamp(Math.round(blur * 2.0F), 0, 127);
        int packedStrength = Math.clamp(Math.round((strength - 1.0F) * 5.0F), 0, 15);
        int paramsTop = topLeft | topRight << 5 | bottomRight << 10;
        int paramsBottom = bottomLeft | packedBlur << 5 | packedStrength << 12;
        return new RenderState(
                DisplayBatcher.pose(graphics), drawX, drawY, drawWidth, drawHeight, blur,
                paramsTop, paramsBottom,
                applyAlpha(colorTopLeft), applyAlpha(colorTopRight), applyAlpha(colorBottomRight), applyAlpha(colorBottomLeft),
                graphics.scissorStack.peek()
        );
    }

    public void render(GuiGraphicsExtractor graphics) {
        if (width <= 0.0F || height <= 0.0F || alpha <= 0.0F) {
            return;
        }
        DisplayBatcher.submit(graphics, build(graphics));
    }

    public static final class RenderState extends RectRenderState {
        RenderState(Matrix3x2f pose, float x, float y, float width, float height, float blur, int paramsTop, int paramsBottom,
                    int colorTopLeft, int colorTopRight, int colorBottomRight, int colorBottomLeft,
                    @Nullable ScreenRectangle scissor) {
            super(pose, x, y, width, height, blur + RectBuilder.PADDING, paramsTop, paramsBottom,
                    colorTopLeft, colorTopRight, colorBottomRight, colorBottomLeft, scissor);
        }

        @Override
        public RenderPipeline pipeline() {
            return Pipelines.SHADOW;
        }
    }
}
