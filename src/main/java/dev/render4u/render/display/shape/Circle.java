package dev.render4u.render.display.shape;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import dev.render4u.render.display.Pipelines;
import dev.render4u.render.display.batch.DisplayBatcher;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.jspecify.annotations.Nullable;
import org.joml.Matrix3x2f;

public final class Circle extends RectBuilder<Circle> {
    private static final float MAX_INNER_PACKED = 2047.0F;

    private float innerRadius;
    private float startAngle;
    private float endAngle = 360.0F;

    private Circle() {
    }

    public static Circle of(float centerX, float centerY, float radius) {
        Circle circle = new Circle();
        circle.position(centerX - radius, centerY - radius);
        circle.size(radius * 2.0F, radius * 2.0F);
        return circle;
    }

    public Circle innerRadius(float innerRadius) {
        this.innerRadius = Math.max(innerRadius, 0.0F);
        return self();
    }

    public Circle angle(float startDegrees, float endDegrees) {
        this.startAngle = Math.clamp(startDegrees, 0.0F, 360.0F);
        this.endAngle = Math.clamp(endDegrees, 0.0F, 360.0F);
        return self();
    }

    public RenderState build(GuiGraphicsExtractor graphics) {
        float r = Math.min(width, height) * 0.5F;
        int inner = Math.clamp(Math.round(Math.clamp(innerRadius, 0.0F, r) * 2.0F), 0, (int) MAX_INNER_PACKED);
        int a0 = encodeAngle(startAngle);
        int a1 = encodeAngle(endAngle);
        int paramsTop = inner | a0 << 11;
        int paramsBottom = a1;
        return new RenderState(
                DisplayBatcher.pose(graphics), x, y, width, height,
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

    static int encodeAngle(float degrees) {
        return Math.clamp(Math.round(degrees * (1023.0F / 360.0F)), 0, 1023);
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
            return Pipelines.CIRCLE;
        }
    }
}
