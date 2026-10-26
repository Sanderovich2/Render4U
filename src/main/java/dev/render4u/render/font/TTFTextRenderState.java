package dev.render4u.render.font;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import java.util.function.Supplier;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.jspecify.annotations.Nullable;
import org.joml.Matrix3x2f;

public final class TTFTextRenderState implements GuiElementRenderState {
    private final Matrix3x2f pose;
    private final @Nullable GpuTextureView textureView;
    private final @Nullable Supplier<GpuTextureView> textureSupplier;
    private final float[] quads;
    private final int[] colors;
    private final int quadCount;
    private final @Nullable ScreenRectangle scissor;
    private final ScreenRectangle bounds;
    private @Nullable TextureSetup textureSetup;

    public TTFTextRenderState(Matrix3x2f pose, GpuTextureView textureView, float[] quads, int[] colors, int quadCount,
                              float minX, float minY, float maxX, float maxY, @Nullable ScreenRectangle scissor) {
        this.pose = pose;
        this.textureView = textureView;
        this.textureSupplier = null;
        this.quads = quads;
        this.colors = colors;
        this.quadCount = quadCount;
        this.scissor = scissor;
        this.bounds = calculateBounds(pose, minX, minY, maxX - minX, maxY - minY, scissor);
    }

    public TTFTextRenderState(Matrix3x2f pose, Supplier<GpuTextureView> textureSupplier, float[] quads, int[] colors, int quadCount,
                              float minX, float minY, float maxX, float maxY, @Nullable ScreenRectangle scissor) {
        this.pose = pose;
        this.textureView = null;
        this.textureSupplier = textureSupplier;
        this.quads = quads;
        this.colors = colors;
        this.quadCount = quadCount;
        this.scissor = scissor;
        this.bounds = calculateBounds(pose, minX, minY, maxX - minX, maxY - minY, scissor);
    }

    @Override
    public void buildVertices(VertexConsumer consumer) {
        for (int i = 0; i < quadCount; i++) {
            int q = i * 8;
            float x0 = quads[q];
            float y0 = quads[q + 1];
            float x1 = quads[q + 2];
            float y1 = quads[q + 3];
            float u0 = quads[q + 4];
            float v0 = quads[q + 5];
            float u1 = quads[q + 6];
            float v1 = quads[q + 7];
            int color = colors[i];

            consumer.addVertexWith2DPose(pose, x0, y0).setUv(u0, v0).setUv2(0, 0).setColor(color);
            consumer.addVertexWith2DPose(pose, x0, y1).setUv(u0, v1).setUv2(0, 0).setColor(color);
            consumer.addVertexWith2DPose(pose, x1, y1).setUv(u1, v1).setUv2(0, 0).setColor(color);
            consumer.addVertexWith2DPose(pose, x1, y0).setUv(u1, v0).setUv2(0, 0).setColor(color);
        }
    }

    private static ScreenRectangle calculateBounds(Matrix3x2f pose, float x, float y, float width, float height, @Nullable ScreenRectangle scissor) {
        float bMinX = Float.MAX_VALUE;
        float bMinY = Float.MAX_VALUE;
        float bMaxX = -Float.MAX_VALUE;
        float bMaxY = -Float.MAX_VALUE;

        for (int i = 0; i < 4; i++) {
            float px = x + (i & 1) * width;
            float py = y + (i >> 1) * height;
            float tx = pose.m00 * px + pose.m10 * py + pose.m20;
            float ty = pose.m01 * px + pose.m11 * py + pose.m21;
            if (tx < bMinX) bMinX = tx;
            if (ty < bMinY) bMinY = ty;
            if (tx > bMaxX) bMaxX = tx;
            if (ty > bMaxY) bMaxY = ty;
        }

        ScreenRectangle raw = new ScreenRectangle((int) Math.floor(bMinX), (int) Math.floor(bMinY),
                (int) Math.ceil(bMaxX - bMinX) + 1, (int) Math.ceil(bMaxY - bMinY) + 1);
        ScreenRectangle clipped = scissor != null ? raw.intersection(scissor) : raw;
        return clipped != null ? clipped : ScreenRectangle.empty();
    }

    @Override
    public RenderPipeline pipeline() {
        return TTFTextPipeline.INSTANCE;
    }

    @Override
    public TextureSetup textureSetup() {
        if (textureSetup == null) {
            GpuTextureView view = textureView != null ? textureView : (textureSupplier != null ? textureSupplier.get() : null);
            textureSetup = TextureSetup.singleTexture(view, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
        }
        return textureSetup;
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

