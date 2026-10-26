package dev.render4u.render.display.batch;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.jspecify.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;

public final class DisplayBatcher {
    private static final List<BatchRenderState> POOL = new ArrayList<>();
    private static final List<BatchRenderState> LEASED = new ArrayList<>();
    private static final List<BatchRenderState> PENDING = new ArrayList<>();

    private static Matrix3x2f snapshot = new Matrix3x2f();
    private static @Nullable GuiGraphicsExtractor session;
    private static int viewportWidth;
    private static int viewportHeight;
    private static int frameBatches;
    private static int frameElements;
    private static int lastFrameBatches;
    private static int lastFrameElements;

    private DisplayBatcher() {
    }

    public static void begin(GuiGraphicsExtractor graphics) {
        flush();
        recycle();
        session = graphics;
        Minecraft minecraft = Minecraft.getInstance();
        viewportWidth = minecraft.getWindow().getWidth();
        viewportHeight = minecraft.getWindow().getHeight();
    }

    public static void end() {
        flush();
        lastFrameBatches = frameBatches;
        lastFrameElements = frameElements;
        frameBatches = 0;
        frameElements = 0;
        session = null;
    }

    public static boolean active() {
        return session != null;
    }

    public static int lastFrameBatches() {
        return lastFrameBatches;
    }

    public static int lastFrameElements() {
        return lastFrameElements;
    }

    public static void flush() {
        GuiGraphicsExtractor active = session;
        if (active == null || PENDING.isEmpty()) {
            return;
        }
        frameBatches += PENDING.size();
        for (int i = 0, size = PENDING.size(); i < size; i++) {
            BatchRenderState batch = PENDING.get(i);
            batch.seal();
            frameElements += batch.memberCount();
            active.guiRenderState.addGuiElement(batch);
        }
        PENDING.clear();
    }

    public static void submit(GuiGraphicsExtractor graphics, GuiElementRenderState element) {
        GuiGraphicsExtractor active = session;
        if (active == null || active.guiRenderState != graphics.guiRenderState) {
            graphics.guiRenderState.addGuiElement(element);
            return;
        }
        ScreenRectangle bounds = element.bounds();
        if (bounds == null) {
            flush();
            graphics.guiRenderState.addGuiElement(element);
            return;
        }
        int x0 = bounds.left();
        int y0 = bounds.top();
        int x1 = x0 + bounds.width();
        int y1 = y0 + bounds.height();
        if (x1 <= x0 || y1 <= y0) {
            return;
        }
        if (x1 <= 0 || y1 <= 0 || x0 >= viewportWidth || y0 >= viewportHeight) {
            return;
        }
        RenderPipeline pipeline = element.pipeline();
        TextureSetup textureSetup = element.textureSetup();
        ScreenRectangle scissor = element.scissorArea();
        BatchRenderState target = null;
        for (int i = PENDING.size() - 1; i >= 0; i--) {
            BatchRenderState batch = PENDING.get(i);
            if (target == null && batch.accepts(pipeline, textureSetup, scissor)) {
                target = batch;
            }
            if (batch.overlaps(x0, y0, x1, y1)) {
                break;
            }
        }
        if (target == null) {
            target = acquire(pipeline, textureSetup, scissor);
            PENDING.add(target);
        }
        target.add(element, x0, y0, x1, y1);
    }

    public static Matrix3x2f pose(GuiGraphicsExtractor graphics) {
        Matrix3x2fc current = graphics.pose();
        if (!snapshot.equals(current, 0.0F)) {
            snapshot = new Matrix3x2f(current);
        }
        return snapshot;
    }

    private static BatchRenderState acquire(RenderPipeline pipeline, TextureSetup textureSetup, @Nullable ScreenRectangle scissor) {
        BatchRenderState batch = POOL.isEmpty() ? new BatchRenderState() : POOL.removeLast();
        batch.open(pipeline, textureSetup, scissor);
        LEASED.add(batch);
        return batch;
    }

    private static void recycle() {
        for (int i = 0, size = LEASED.size(); i < size; i++) {
            BatchRenderState batch = LEASED.get(i);
            batch.reset();
            POOL.add(batch);
        }
        LEASED.clear();
    }
}
