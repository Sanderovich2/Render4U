package dev.render4u.render.display.shadow;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.jspecify.annotations.Nullable;
import org.joml.Matrix3x2f;

public final class ShadowRenderState implements GuiElementRenderState {
    private final Matrix3x2f pose;
    private final float x;
    private final float y;
    private final float width;
    private final float height;
    private final float blur;
    private final int paramsTop;
    private final int paramsBottom;
    private final int topLeft;
    private final int topRight;
    private final int bottomRight;
    private final int bottomLeft;
    private final @Nullable ScreenRectangle scissor;
    private final ScreenRectangle bounds;

    ShadowRenderState(Matrix3x2f pose, float x, float y, float width, float height, float blur, int paramsTop, int paramsBottom,
                      int topLeft, int topRight, int bottomRight, int bottomLeft, @Nullable ScreenRectangle scissor) {
        this.pose = pose;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.blur = blur;
        this.paramsTop = paramsTop;
        this.paramsBottom = paramsBottom;
        this.topLeft = topLeft;
        this.topRight = topRight;
        this.bottomRight = bottomRight;
        this.bottomLeft = bottomLeft;
        this.scissor = scissor;
        float pad = blur + ShadowPipeline.PADDING;
        ScreenRectangle raw = new ScreenRectangle((int) Math.floor(x - pad), (int) Math.floor(y - pad),
                (int) Math.ceil(width + pad * 2.0F) + 1, (int) Math.ceil(height + pad * 2.0F) + 1).transformAxisAligned(pose);
        ScreenRectangle clipped = scissor != null ? raw.intersection(scissor) : raw;
        this.bounds = clipped != null ? clipped : ScreenRectangle.empty();
    }

    @Override
    public void buildVertices(VertexConsumer consumer) {
        float pad = blur + ShadowPipeline.PADDING;
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
        consumer.addVertexWith2DPose(pose, px, py).setUv(u, v).setUv2(paramsTop, paramsBottom).setColor(color);
    }

    @Override
    public RenderPipeline pipeline() {
        return ShadowPipeline.INSTANCE;
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

