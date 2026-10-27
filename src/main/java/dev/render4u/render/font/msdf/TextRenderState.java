package dev.render4u.render.font.msdf;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.FilterMode;
import dev.render4u.util.color.ColorMath;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.jspecify.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;

public final class TextRenderState implements GuiElementRenderState {
    private static final float PADDING = 2.0F;

    private final TextLayout layout;
    private final MsdfFont font;
    private final Matrix3x2f pose;
    private final float x;
    private final float y;
    private final float size;
    private final int solid;
    private final @Nullable ColorFunction function;
    private final float alpha;
    private final int packedRange;
    private final int packedEffect;
    private final @Nullable ScreenRectangle scissor;
    private final ScreenRectangle bounds;
    private @Nullable TextureSetup textureSetup;

    TextRenderState(TextLayout layout, Matrix3x2f pose, float x, float y, float size, int solid, @Nullable ColorFunction function,
                    float alpha, int packedEffect, @Nullable ScreenRectangle scissor) {
        this.layout = layout;
        this.font = layout.font();
        this.pose = pose;
        this.x = x;
        this.y = y;
        this.size = size;
        this.solid = solid;
        this.function = function;
        this.alpha = alpha;
        this.packedRange = Math.clamp(Math.round(font.atlas().distanceRange() * 2.0F), 1, 255);
        this.packedEffect = packedEffect;
        this.scissor = scissor;
        this.bounds = bounds(pose, x - PADDING, y - PADDING, layout.width() * size + PADDING * 2.0F, layout.height() * size + PADDING * 2.0F, scissor);
    }

    TextRenderState(TextLayout layout, Matrix3x2f pose, float x, float y, float size, int solid, @Nullable ColorFunction function,
                    float alpha, @Nullable ScreenRectangle scissor) {
        this(layout, pose, x, y, size, solid, function, alpha, 0, scissor);
    }

    @Override
    public void buildVertices(VertexConsumer consumer) {
        float[] quads = layout.quads();
        int[] indices = layout.indices();
        float invWidth = layout.width() > 0.0F ? 1.0F / layout.width() : 0.0F;
        float invHeight = layout.height() > 0.0F ? 1.0F / layout.height() : 0.0F;
        for (int g = 0; g < layout.glyphCount(); g++) {
            int o = g * TextLayout.STRIDE;
            float x0 = quads[o];
            float y0 = quads[o + 1];
            float x1 = quads[o + 2];
            float y1 = quads[o + 3];
            float u0 = quads[o + 4];
            float v0 = quads[o + 5];
            float u1 = quads[o + 6];
            float v1 = quads[o + 7];
            int index = indices[g];
            vertex(consumer, x0, y0, u0, v0, color(index, x0 * invWidth, y0 * invHeight));
            vertex(consumer, x0, y1, u0, v1, color(index, x0 * invWidth, y1 * invHeight));
            vertex(consumer, x1, y1, u1, v1, color(index, x1 * invWidth, y1 * invHeight));
            vertex(consumer, x1, y0, u1, v0, color(index, x1 * invWidth, y0 * invHeight));
        }
    }

    private int color(int index, float nx, float ny) {
        if (function == null) {
            return ColorMath.multiplyAlpha(solid, alpha);
        }
        return ColorMath.multiplyAlpha(function.color(index, Math.clamp(nx, 0.0F, 1.0F), Math.clamp(ny, 0.0F, 1.0F)), alpha);
    }

    private void vertex(VertexConsumer consumer, float px, float py, float u, float v, int color) {
        consumer.addVertexWith2DPose(pose, x + px * size, y + py * size).setUv(u, v).setUv2(packedRange, packedEffect).setColor(color);
    }

    private static ScreenRectangle bounds(Matrix3x2f pose, float x, float y, float width, float height, @Nullable ScreenRectangle scissor) {
        Vector2f point = new Vector2f();
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        for (int i = 0; i < 4; i++) {
            pose.transformPosition(x + (i & 1) * width, y + (i >> 1) * height, point);
            minX = Math.min(minX, point.x);
            minY = Math.min(minY, point.y);
            maxX = Math.max(maxX, point.x);
            maxY = Math.max(maxY, point.y);
        }
        ScreenRectangle raw = new ScreenRectangle((int) Math.floor(minX), (int) Math.floor(minY), (int) Math.ceil(maxX - minX) + 1, (int) Math.ceil(maxY - minY) + 1);
        ScreenRectangle clipped = scissor != null ? raw.intersection(scissor) : raw;
        return clipped != null ? clipped : ScreenRectangle.empty();
    }

    @Override
    public RenderPipeline pipeline() {
        return TextPipeline.INSTANCE;
    }

    @Override
    public TextureSetup textureSetup() {
        if (textureSetup == null) {
            textureSetup = TextureSetup.singleTexture(font.textureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
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
