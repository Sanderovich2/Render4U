package dev.render4u.render.display.shape;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import dev.render4u.render.display.Pipelines;
import dev.render4u.render.display.batch.DisplayBatcher;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.jspecify.annotations.Nullable;
import org.joml.Matrix3x2f;

public final class Outline extends RectBuilder<Outline> {
    public static final float DEFAULT_THICKNESS = 1.0F;

    private float thickness = DEFAULT_THICKNESS;

    private Outline() {
    }

    public static Outline create() {
        return new Outline();
    }

    public static Outline of(float x, float y, float width, float height) {
        return new Outline().position(x, y).size(width, height);
    }

    public Outline thickness(float thickness) {
        this.thickness = Math.clamp(thickness, 0.0625F, 16.0F);
        return self();
    }

    public RenderState build(GuiGraphicsExtractor graphics) {
        float maxRadius = Math.min(width, height) * 0.5F;
        int topLeft = packRadius(radiusTopLeft, maxRadius, 2.0F, 6);
        int topRight = packRadius(radiusTopRight, maxRadius, 2.0F, 6);
        int bottomRight = packRadius(radiusBottomRight, maxRadius, 2.0F, 6);
        int bottomLeft = packRadius(radiusBottomLeft, maxRadius, 2.0F, 6);
        int packedThickness = Math.clamp(Math.round(thickness * 16.0F), 1, 255);
        int paramsTop = topLeft | topRight << 6 | (packedThickness & 15) << 12;
        int paramsBottom = bottomLeft | bottomRight << 6 | (packedThickness >> 4) << 12;
        return new RenderState(
                DisplayBatcher.pose(graphics), x, y, width, height,
                paramsTop, paramsBottom,
                applyAlpha(colorTopLeft), applyAlpha(colorTopRight), applyAlpha(colorBottomRight), applyAlpha(colorBottomLeft),
                graphics.scissorStack.peek()
        );
    }

    public void render(GuiGraphicsExtractor graphics) {
        if (width <= 0.0F || height <= 0.0F || thickness <= 0.0F || alpha <= 0.0F) {
            return;
        }
        DisplayBatcher.submit(graphics, build(graphics));
    }

    public static final class RenderState extends RectRenderState {
        RenderState(Matrix3x2f pose, float x, float y, float width, float height, int paramsTop, int paramsBottom,
                    int colorTopLeft, int colorTopRight, int colorBottomRight, int colorBottomLeft,
                    @Nullable ScreenRectangle scissor) {
            super(pose, x, y, width, height, RectBuilder.PADDING, paramsTop, paramsBottom,
                    colorTopLeft, colorTopRight, colorBottomRight, colorBottomLeft, scissor);
        }

        @Override
        public RenderPipeline pipeline() {
            return Pipelines.OUTLINE;
        }
    }
}
