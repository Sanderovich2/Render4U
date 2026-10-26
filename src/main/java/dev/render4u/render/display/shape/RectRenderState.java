package dev.render4u.render.display.shape;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.jspecify.annotations.Nullable;
import org.joml.Matrix3x2f;

public abstract class RectRenderState implements GuiElementRenderState {
    protected final Matrix3x2f pose;
    protected final float x;
    protected final float y;
    protected final float width;
    protected final float height;
    protected final float pad;
    protected final int paramsTop;
    protected final int paramsBottom;
    protected final int colorTopLeft;
    protected final int colorTopRight;
    protected final int colorBottomRight;
    protected final int colorBottomLeft;
    private final @Nullable ScreenRectangle scissor;
    private final ScreenRectangle bounds;

    protected RectRenderState(Matrix3x2f pose, float x, float y, float width, float height, float pad,
                              int paramsTop, int paramsBottom,
                              int colorTopLeft, int colorTopRight, int colorBottomRight, int colorBottomLeft,
                              @Nullable ScreenRectangle scissor) {
        this.pose = pose;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.pad = pad;
        this.paramsTop = paramsTop;
        this.paramsBottom = paramsBottom;
        this.colorTopLeft = colorTopLeft;
        this.colorTopRight = colorTopRight;
        this.colorBottomRight = colorBottomRight;
        this.colorBottomLeft = colorBottomLeft;
        this.scissor = scissor;
        ScreenRectangle raw = new ScreenRectangle((int) Math.floor(x - pad), (int) Math.floor(y - pad),
                (int) Math.ceil(width + pad * 2.0F) + 1, (int) Math.ceil(height + pad * 2.0F) + 1).transformAxisAligned(pose);
        ScreenRectangle clipped = scissor != null ? raw.intersection(scissor) : raw;
        this.bounds = clipped != null ? clipped : ScreenRectangle.empty();
    }

    @Override
    public final void buildVertices(VertexConsumer consumer) {
        float hw = width * 0.5F + pad;
        float hh = height * 0.5F + pad;
        float x0 = x - pad;
        float y0 = y - pad;
        float x1 = x + width + pad;
        float y1 = y + height + pad;
        vertex(consumer, x0, y0, -hw, -hh, colorTopLeft);
        vertex(consumer, x0, y1, -hw, hh, colorBottomLeft);
        vertex(consumer, x1, y1, hw, hh, colorBottomRight);
        vertex(consumer, x1, y0, hw, -hh, colorTopRight);
    }

    protected final void vertex(VertexConsumer consumer, float px, float py, float u, float v, int color) {
        consumer.addVertexWith2DPose(pose, px, py).setUv(u, v).setUv2(paramsTop, paramsBottom).setColor(color);
    }

    @Override
    public TextureSetup textureSetup() {
        return TextureSetup.noTexture();
    }

    @Override
    public final @Nullable ScreenRectangle scissorArea() {
        return scissor;
    }

    @Override
    public final @Nullable ScreenRectangle bounds() {
        return bounds;
    }
}
