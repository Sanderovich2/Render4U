package dev.render4u.render.display.batch;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.jspecify.annotations.Nullable;

public final class BatchRenderState implements GuiElementRenderState {
    private final List<GuiElementRenderState> members = new ArrayList<>(32);
    private int[] rects = new int[128];
    private RenderPipeline pipeline;
    private TextureSetup textureSetup;
    private @Nullable ScreenRectangle scissor;
    private int count;
    private int minX = Integer.MAX_VALUE;
    private int minY = Integer.MAX_VALUE;
    private int maxX = Integer.MIN_VALUE;
    private int maxY = Integer.MIN_VALUE;
    private ScreenRectangle bounds = ScreenRectangle.empty();

    void open(RenderPipeline pipeline, TextureSetup textureSetup, @Nullable ScreenRectangle scissor) {
        this.pipeline = pipeline;
        this.textureSetup = textureSetup;
        this.scissor = scissor;
        this.minX = Integer.MAX_VALUE;
        this.minY = Integer.MAX_VALUE;
        this.maxX = Integer.MIN_VALUE;
        this.maxY = Integer.MIN_VALUE;
        this.bounds = ScreenRectangle.empty();
    }

    private static final int MAX_MEMBERS = 4096;

    boolean accepts(RenderPipeline pipeline, TextureSetup textureSetup, @Nullable ScreenRectangle scissor) {
        return members.size() < MAX_MEMBERS && this.pipeline == pipeline && (this.textureSetup == textureSetup || (this.textureSetup != null && this.textureSetup.equals(textureSetup))) && Objects.equals(this.scissor, scissor);
    }

    void add(GuiElementRenderState element, int x0, int y0, int x1, int y1) {
        members.add(element);
        int offset = count * 4;
        if (offset + 4 > rects.length) {
            rects = Arrays.copyOf(rects, Math.max(rects.length * 2, offset + 4));
        }
        rects[offset] = x0;
        rects[offset + 1] = y0;
        rects[offset + 2] = x1;
        rects[offset + 3] = y1;
        count++;
        minX = Math.min(minX, x0);
        minY = Math.min(minY, y0);
        maxX = Math.max(maxX, x1);
        maxY = Math.max(maxY, y1);
    }

    boolean overlaps(int x0, int y0, int x1, int y1) {
        if (count == 0 || x0 >= maxX || x1 <= minX || y0 >= maxY || y1 <= minY) {
            return false;
        }
        for (int i = 0; i < count; i++) {
            int offset = i * 4;
            if (x0 < rects[offset + 2] && x1 > rects[offset] && y0 < rects[offset + 3] && y1 > rects[offset + 1]) {
                return true;
            }
        }
        return false;
    }

    int memberCount() {
        return count;
    }

    void seal() {
        if (count == 0) {
            bounds = ScreenRectangle.empty();
            return;
        }
        ScreenRectangle raw = new ScreenRectangle(minX, minY, maxX - minX, maxY - minY);
        bounds = scissor != null ? raw.intersection(scissor) : raw;
        if (bounds == null) {
            bounds = ScreenRectangle.empty();
        }
    }

    void reset() {
        members.clear();
        pipeline = null;
        textureSetup = null;
        scissor = null;
        count = 0;
        bounds = ScreenRectangle.empty();
    }

    @Override
    public void buildVertices(VertexConsumer consumer) {
        for (int i = 0, size = members.size(); i < size; i++) {
            members.get(i).buildVertices(consumer);
        }
    }

    @Override
    public RenderPipeline pipeline() {
        return pipeline;
    }

    @Override
    public TextureSetup textureSetup() {
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

