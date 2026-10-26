package dev.render4u.render.display.shape;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import dev.render4u.render.display.Pipelines;
import dev.render4u.render.display.batch.DisplayBatcher;
import dev.render4u.util.color.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.jspecify.annotations.Nullable;
import org.joml.Matrix3x2f;

public final class Line {
    public static final float DEFAULT_THICKNESS = 1.0F;

    private float x1;
    private float y1;
    private float x2;
    private float y2;
    private float thickness = DEFAULT_THICKNESS;
    private int colorStart = -1;
    private int colorEnd = -1;
    private float alpha = 1.0F;

    private Line() {
    }

    public static Line of(float x1, float y1, float x2, float y2) {
        Line line = new Line();
        line.x1 = x1;
        line.y1 = y1;
        line.x2 = x2;
        line.y2 = y2;
        return line;
    }

    public Line points(float x1, float y1, float x2, float y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        return this;
    }

    public Line thickness(float thickness) {
        this.thickness = Math.clamp(thickness, 0.0625F, 8192.0F);
        return this;
    }

    public Line color(int argb) {
        this.colorStart = argb;
        this.colorEnd = argb;
        return this;
    }

    public Line color(Color color) {
        return color(color.argb());
    }

    public Line gradient(int start, int end) {
        this.colorStart = start;
        this.colorEnd = end;
        return this;
    }

    public Line gradient(Color start, Color end) {
        return gradient(start.argb(), end.argb());
    }

    public Line alpha(float alpha) {
        this.alpha = Math.clamp(alpha, 0.0F, 1.0F);
        return this;
    }

    public RenderState build(GuiGraphicsExtractor graphics) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.hypot(dx, dy);
        float radius = thickness * 0.5F;
        float cos = len < 1.0E-4F ? 1.0F : dx / len;
        float sin = len < 1.0E-4F ? 0.0F : dy / len;
        float midX = (x1 + x2) * 0.5F;
        float midY = (y1 + y2) * 0.5F;
        float ht = len * 0.5F + radius + RectBuilder.PADDING;
        float hs = radius + RectBuilder.PADDING;
        int packed = Math.clamp(Math.round(thickness * 2.0F), 0, 65535);
        int start = applyAlpha(colorStart);
        int end = applyAlpha(colorEnd);
        return new RenderState(
                DisplayBatcher.pose(graphics), midX, midY, cos, sin, ht, hs, packed,
                start, end,
                graphics.scissorStack.peek()
        );
    }

    public void render(GuiGraphicsExtractor graphics) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        if ((float) Math.hypot(dx, dy) < 0.001F || thickness <= 0.0F || alpha <= 0.0F) {
            return;
        }
        DisplayBatcher.submit(graphics, build(graphics));
    }

    private int applyAlpha(int color) {
        int a = Math.round((color >>> 24) * alpha);
        return a << 24 | color & 0xFFFFFF;
    }

    public static final class RenderState implements GuiElementRenderState {
        private final Matrix3x2f pose;
        private final float midX;
        private final float midY;
        private final float cos;
        private final float sin;
        private final float ht;
        private final float hs;
        private final int packedThickness;
        private final int colorStart;
        private final int colorEnd;
        private final @Nullable ScreenRectangle scissor;
        private final ScreenRectangle bounds;

        RenderState(Matrix3x2f pose, float midX, float midY, float cos, float sin, float ht, float hs, int packedThickness,
                    int colorStart, int colorEnd, @Nullable ScreenRectangle scissor) {
            this.pose = pose;
            this.midX = midX;
            this.midY = midY;
            this.cos = cos;
            this.sin = sin;
            this.ht = ht;
            this.hs = hs;
            this.packedThickness = packedThickness;
            this.colorStart = colorStart;
            this.colorEnd = colorEnd;
            this.scissor = scissor;
            float minX = Float.MAX_VALUE;
            float minY = Float.MAX_VALUE;
            float maxX = -Float.MAX_VALUE;
            float maxY = -Float.MAX_VALUE;
            for (int i = 0; i < 4; i++) {
                float lx = (i & 1) == 0 ? -ht : ht;
                float ly = (i & 2) == 0 ? -hs : hs;
                float wx = midX + lx * cos - ly * sin;
                float wy = midY + lx * sin + ly * cos;
                minX = Math.min(minX, wx);
                minY = Math.min(minY, wy);
                maxX = Math.max(maxX, wx);
                maxY = Math.max(maxY, wy);
            }
            ScreenRectangle raw = new ScreenRectangle((int) Math.floor(minX), (int) Math.floor(minY),
                    (int) Math.ceil(maxX - minX) + 1, (int) Math.ceil(maxY - minY) + 1).transformAxisAligned(pose);
            ScreenRectangle clipped = scissor != null ? raw.intersection(scissor) : raw;
            this.bounds = clipped != null ? clipped : ScreenRectangle.empty();
        }

        private float vertexX(float lx, float ly) {
            return midX + lx * cos - ly * sin;
        }

        private float vertexY(float lx, float ly) {
            return midY + lx * sin + ly * cos;
        }

        @Override
        public void buildVertices(VertexConsumer consumer) {
            float pad = RectBuilder.PADDING;
            float ux = ht + pad;
            float uy = hs + pad;
            vertex(consumer, -ht, -hs, -ux, -uy, colorStart);
            vertex(consumer, -ht, hs, -ux, uy, colorStart);
            vertex(consumer, ht, hs, ux, uy, colorEnd);
            vertex(consumer, ht, -hs, ux, -uy, colorEnd);
        }

        private void vertex(VertexConsumer consumer, float lx, float ly, float u, float v, int color) {
            consumer.addVertexWith2DPose(pose, vertexX(lx, ly), vertexY(lx, ly)).setUv(u, v).setUv2(packedThickness, 0).setColor(color);
        }

        @Override
        public RenderPipeline pipeline() {
            return Pipelines.LINE;
        }

        @Override
        public TextureSetup textureSetup() {
            return TextureSetup.noTexture();
        }

        @Override
        public @Nullable ScreenRectangle scissorArea() {
            return scissor;
        }

        @Override
        public @Nullable ScreenRectangle bounds() {
            return bounds;
        }
    }
}
