package dev.render4u.render.display.outline;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.jspecify.annotations.Nullable;
import org.joml.Matrix3x2f;

public final class OutlineRenderState implements GuiElementRenderState {
    private final Matrix3x2f pose;
    private final float x;
    private final float y;
    private final float width;
    private final float height;
    private final int packedTop;
    private final int packedBottom;
    private final int topLeft;
    private final int topRight;
    private final int bottomRight;
    private final int bottomLeft;
    private final @Nullable ScreenRectangle scissor;
    private final ScreenRectangle bounds;

    OutlineRenderState(Matrix3x2f pose, float x, float y, float width, float height, int packedTop, int packedBottom,
                       int topLeft, int topRight, int bottomRight, int bottomLeft, @Nullable ScreenRectangle scissor) {
        this.pose = pose;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.packedTop = packedTop;
        this.packedBottom = packedBottom;
        this.topLeft = topLeft;
        this.topRight = topRight;
        this.bottomRight = bottomRight;
        this.bottomLeft = bottomLeft;
        this.scissor = scissor;
        float pad = OutlinePipeline.PADDING;
        ScreenRectangle raw = new ScreenRectangle((int) Math.floor(x - pad), (int) Math.floor(y - pad),
                (int) Math.ceil(width + pad * 2.0F) + 1, (int) Math.ceil(height + pad * 2.0F) + 1).transformAxisAligned(pose);
        ScreenRectangle clipped = scissor != null ? raw.intersection(scissor) : raw;
        this.bounds = clipped != null ? clipped : ScreenRectangle.empty();
    }

    @Override
    public void buildVertices(VertexConsumer consumer) {
        float pad = OutlinePipeline.PADDING;
        float hw = width * 0.5F + pad;
        float hh = height * 0.5F + pad;
        float x0 = x - pad;
        float y0 = y - pad;
        float x1 = x + width + pad;
        float y1 = y + height + pad;
        vertex(consumer, x0, y0, -hw, -hh, topLeft);
        vertex(consumer, x0, y1, -hw, hh, bottomLeft);
        vertex(consumer, x1, y1, hw, hh, bottomRight);
        vertex(consumer, x1, y0, hw, -hh, topRight);
    }

    private void vertex(VertexConsumer consumer, float px, float py, float u, float v, int color) {
        consumer.addVertexWith2DPose(pose, px, py).setUv(u, v).setUv2(packedTop, packedBottom).setColor(color);
    }

    @Override
    public RenderPipeline pipeline() {
        return OutlinePipeline.INSTANCE;
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

