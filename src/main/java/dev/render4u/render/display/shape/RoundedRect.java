package dev.render4u.render.display.shape;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import dev.render4u.render.display.Pipelines;
import dev.render4u.render.display.batch.DisplayBatcher;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.jspecify.annotations.Nullable;
import org.joml.Matrix3x2f;

public final class RoundedRect extends RectBuilder<RoundedRect> {
    private RoundedRect() {
    }

    public static RoundedRect create() {
        return new RoundedRect();
    }

    public static RoundedRect of(float x, float y, float width, float height) {
        return new RoundedRect().position(x, y).size(width, height);
    }

    public static RoundedRect of(float x, float y, float width, float height, float radius) {
        return of(x, y, width, height).radius(radius);
    }

    public RenderState build(GuiGraphicsExtractor graphics) {
        float maxRadius = Math.min(width, height) * 0.5F;
        int topLeft = packRadius(radiusTopLeft, maxRadius, 2.0F, 8);
        int topRight = packRadius(radiusTopRight, maxRadius, 2.0F, 8);
        int bottomRight = packRadius(radiusBottomRight, maxRadius, 2.0F, 8);
        int bottomLeft = packRadius(radiusBottomLeft, maxRadius, 2.0F, 8);
        return new RenderState(
                DisplayBatcher.pose(graphics), x, y, width, height,
                topLeft | topRight << 8, bottomLeft | bottomRight << 8,
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
        RenderState(Matrix3x2f pose, float x, float y, float width, float height, int paramsTop, int paramsBottom,
                    int colorTopLeft, int colorTopRight, int colorBottomRight, int colorBottomLeft,
                    @Nullable ScreenRectangle scissor) {
            super(pose, x, y, width, height, RectBuilder.PADDING, paramsTop, paramsBottom,
                    colorTopLeft, colorTopRight, colorBottomRight, colorBottomLeft, scissor);
        }

        @Override
        public RenderPipeline pipeline() {
            return Pipelines.ROUNDED_RECT;
        }
    }
}
