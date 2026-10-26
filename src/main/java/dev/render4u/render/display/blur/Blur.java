package dev.render4u.render.display.blur;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import dev.render4u.render.display.DisplayUtil;
import dev.render4u.render.display.Pipelines;
import dev.render4u.render.display.batch.DisplayBatcher;
import dev.render4u.render.display.shape.RectBuilder;
import dev.render4u.render.display.shape.RectRenderState;
import dev.render4u.util.color.Color;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import org.jspecify.annotations.Nullable;
import org.joml.Matrix3x2f;

public final class Blur extends RectBuilder<Blur> {

    public enum Type {
        GAUSSIAN,
        KAWASE,
        BOX;

        public static final int MAX_STRENGTH = 6;

        public int outputLevel(int strength) {
            return this == KAWASE ? 1 : Math.clamp((strength + 1) / 2, 1, 3);
        }

        public int downDepth(int strength) {
            return this == KAWASE ? strength : outputLevel(strength);
        }
    }

    public record Key(Type type, int strength) {
        public Key {
            strength = Math.clamp(strength, 1, Type.MAX_STRENGTH);
        }
    }

    static final class Targets implements AutoCloseable {
        private static final int LEVELS = Type.MAX_STRENGTH + 1;

        private final TextureTarget[] down = new TextureTarget[LEVELS];
        private final TextureTarget[] temp = new TextureTarget[LEVELS];
        private final Map<Key, TextureTarget> outputs = new HashMap<>();
        private int baseWidth;
        private int baseHeight;

        void ensure(RenderTarget main) {
            if (main.width == baseWidth && main.height == baseHeight) {
                return;
            }
            close();
            baseWidth = main.width;
            baseHeight = main.height;
        }

        TextureTarget down(int level) {
            return obtain(down, level, "down");
        }

        TextureTarget temp(int level) {
            return obtain(temp, level, "temp");
        }

        TextureTarget output(Key key) {
            if (key.type() == Type.KAWASE && key.strength() == 1) {
                return down(1);
            }
            return outputs.computeIfAbsent(key, k -> create("out " + k.type() + " " + k.strength(), k.type().outputLevel(k.strength())));
        }

        @Override
        public void close() {
            destroy(down);
            destroy(temp);
            for (TextureTarget target : outputs.values()) {
                target.destroyBuffers();
            }
            outputs.clear();
        }

        private TextureTarget obtain(TextureTarget[] pool, int level, String label) {
            if (pool[level] == null) {
                pool[level] = create(label, level);
            }
            return pool[level];
        }

        private TextureTarget create(String label, int level) {
            return new TextureTarget("blur " + label + " L" + level, levelSize(baseWidth, level), levelSize(baseHeight, level), GpuFormat.RGBA8_UNORM, null);
        }

        private static int levelSize(int base, int level) {
            int size = base;
            for (int i = 0; i < level; i++) {
                size = Math.max(1, (size + 1) >> 1);
            }
            return size;
        }

        private static void destroy(TextureTarget[] pool) {
            for (int i = 0; i < pool.length; i++) {
                if (pool[i] != null) {
                    pool[i].destroyBuffers();
                    pool[i] = null;
                }
            }
        }
    }

    public static final class RenderState extends RectRenderState {
        private final Key key;
        private @Nullable TextureSetup textureSetup;

        RenderState(Key key, Matrix3x2f pose, float x, float y, float width, float height, int paramsTop, int paramsBottom,
                    int tintTopLeft, int tintTopRight, int tintBottomRight, int tintBottomLeft, @Nullable ScreenRectangle scissor) {
            super(pose, x, y, width, height, RectBuilder.PADDING, paramsTop, paramsBottom,
                    tintTopLeft, tintTopRight, tintBottomRight, tintBottomLeft, scissor);
            this.key = key;
            ScreenRectangle region = new ScreenRectangle(
                    (int) Math.floor(x - RectBuilder.PADDING), (int) Math.floor(y - RectBuilder.PADDING),
                    (int) Math.ceil(width + RectBuilder.PADDING * 2.0F) + 1, (int) Math.ceil(height + RectBuilder.PADDING * 2.0F) + 1
            ).transformAxisAligned(pose);
            Blur.request(key, region);
        }

        @Override
        public RenderPipeline pipeline() {
            return Pipelines.BLUR_PANEL;
        }

        @Override
        public TextureSetup textureSetup() {
            if (textureSetup == null) {
                textureSetup = TextureSetup.singleTexture(Blur.outputView(key), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
            }
            return textureSetup;
        }
    }

    static final class Region {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        void merge(ScreenRectangle rect) {
            merge(rect.left(), rect.top(), rect.right(), rect.bottom());
        }

        void merge(int left, int top, int right, int bottom) {
            minX = Math.min(minX, left);
            minY = Math.min(minY, top);
            maxX = Math.max(maxX, right);
            maxY = Math.max(maxY, bottom);
        }

        void merge(Region other) {
            minX = Math.min(minX, other.minX);
            minY = Math.min(minY, other.minY);
            maxX = Math.max(maxX, other.maxX);
            maxY = Math.max(maxY, other.maxY);
        }

        boolean isEmpty() {
            return minX > maxX || minY > maxY;
        }
    }

    private static final Targets TARGETS = new Targets();
    private static final Map<Key, Region> REQUESTED = new LinkedHashMap<>();

    private static final int KERNEL_MARGIN_TEXELS = 12;
    static final int[] SCISSOR = new int[4];

    private static final float MAX_RADIUS = 31.5F;
    private static final float OPACITY_STEPS = 31.0F;

    private Type type = Type.GAUSSIAN;
    private int strength = 3;

    private Blur() {
        corners(0, 0, 0, 0);
    }

    public static void request(Key key, ScreenRectangle region) {
        REQUESTED.computeIfAbsent(key, k -> new Region()).merge(region);
    }

    public static GpuTextureView outputView(Key key) {
        TARGETS.ensure(mainTarget());
        return TARGETS.output(key).getColorTextureView();
    }

    public static void process() {
        if (REQUESTED.isEmpty()) {
            return;
        }
        RenderTarget main = mainTarget();
        TARGETS.ensure(main);

        int depth = 0;
        for (Key key : REQUESTED.keySet()) {
            int d = key.type().downDepth(key.strength());
            if (d > depth) {
                depth = d;
            }
        }

        Region chain = new Region();
        for (Region region : REQUESTED.values()) {
            chain.merge(region);
        }

        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();

        GpuTextureView source = main.getColorTextureView();
        for (int level = 1; level <= depth; level++) {
            TextureTarget target = TARGETS.down(level);
            pass(encoder, Pipelines.BLUR_DOWNSAMPLE, source, target, level, chain);
            source = target.getColorTextureView();
        }

        for (Map.Entry<Key, Region> entry : REQUESTED.entrySet()) {
            processKey(encoder, entry.getKey(), entry.getValue());
        }
        REQUESTED.clear();
    }

    public static void endFrame() {
        REQUESTED.clear();
    }

    public static void close() {
        REQUESTED.clear();
        TARGETS.close();
    }

    private static void processKey(CommandEncoder encoder, Key key, Region region) {
        switch (key.type()) {
            case GAUSSIAN -> separable(encoder, key, region, Pipelines.BLUR_GAUSSIAN_HORIZONTAL, Pipelines.BLUR_GAUSSIAN_VERTICAL);
            case KAWASE -> kawase(encoder, key, region);
            case BOX -> separable(encoder, key, region, Pipelines.BLUR_BOX_HORIZONTAL, Pipelines.BLUR_BOX_VERTICAL);
        }
    }

    private static void kawase(CommandEncoder encoder, Key key, Region region) {
        GpuTextureView source = TARGETS.down(key.strength()).getColorTextureView();
        for (int level = key.strength() - 1; level >= 1; level--) {
            TextureTarget target = level == 1 ? TARGETS.output(key) : TARGETS.temp(level);
            pass(encoder, Pipelines.BLUR_UPSAMPLE, source, target, level, region);
            source = target.getColorTextureView();
        }
    }

    private static void separable(CommandEncoder encoder, Key key, Region region, RenderPipeline horizontal, RenderPipeline vertical) {
        int level = key.type().outputLevel(key.strength());
        int repeats = key.strength() > 3 ? 2 : 1;
        TextureTarget temp = TARGETS.temp(level);
        TextureTarget output = TARGETS.output(key);
        GpuTextureView source = TARGETS.down(level).getColorTextureView();
        for (int i = 0; i < repeats; i++) {
            pass(encoder, horizontal, source, temp, level, region);
            pass(encoder, vertical, temp.getColorTextureView(), output, level, region);
            source = output.getColorTextureView();
        }
    }

    private static void pass(CommandEncoder encoder, RenderPipeline pipeline, GpuTextureView source, TextureTarget target, int level, Region region) {
        if (!crop(region, level, target.width, target.height)) {
            return;
        }
        GpuSampler sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
        try (RenderPass renderPass = encoder.createRenderPass(() -> "Blur " + pipeline.getLocation().getPath(), target.getColorTextureView(), Optional.empty())) {
            renderPass.setPipeline(RenderSystem.getCompiledPipeline(pipeline));
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.enableScissor(SCISSOR[0], SCISSOR[1], SCISSOR[2], SCISSOR[3]);
            renderPass.setUniform("InSampler", source, sampler);
            renderPass.draw(3, 1, 0, 0);
            renderPass.disableScissor();
        }
    }

    static boolean crop(Region region, int level, int targetWidth, int targetHeight) {
        if (region.isEmpty()) {
            return false;
        }
        int margin = KERNEL_MARGIN_TEXELS << level;
        int x0 = Math.max(0, toScreen(region.minX) - margin);
        int y0 = Math.max(0, toScreen(region.minY) - margin);
        int x1 = toScreen(region.maxX) + margin;
        int y1 = toScreen(region.maxY) + margin;
        int tx0 = x0 >> level;
        int ty0 = y0 >> level;
        int tx1 = (x1 + (1 << level) - 1) >> level;
        int ty1 = (y1 + (1 << level) - 1) >> level;
        int scX = Math.max(0, tx0);
        int scY = Math.max(0, targetHeight - ty1);
        int scW = Math.max(0, Math.min(targetWidth, tx1) - scX);
        int scH = Math.max(0, (targetHeight - Math.max(0, ty0)) - scY);
        if (scW == 0 || scH == 0) {
            return false;
        }
        SCISSOR[0] = scX;
        SCISSOR[1] = scY;
        SCISSOR[2] = scW;
        SCISSOR[3] = scH;
        return true;
    }

    private static int toScreen(int gui) {
        return Math.round(gui * DisplayUtil.SCALE);
    }

    private static RenderTarget mainTarget() {
        return Minecraft.getInstance().gameRenderer.mainRenderTarget();
    }

    public static Blur create() {
        return new Blur();
    }

    public static Blur of(float x, float y, float width, float height) {
        return new Blur().position(x, y).size(width, height);
    }

    public Blur radiusTop(float radius) {
        this.radiusTopLeft = radius;
        this.radiusTopRight = radius;
        return self();
    }

    public Blur radiusBottom(float radius) {
        this.radiusBottomLeft = radius;
        this.radiusBottomRight = radius;
        return self();
    }

    public Blur radiusLeft(float radius) {
        this.radiusTopLeft = radius;
        this.radiusBottomLeft = radius;
        return self();
    }

    public Blur radiusRight(float radius) {
        this.radiusTopRight = radius;
        this.radiusBottomRight = radius;
        return self();
    }

    public Blur type(Type type) {
        this.type = type;
        return self();
    }

    public Blur strength(int strength) {
        this.strength = Math.clamp(strength, 1, Type.MAX_STRENGTH);
        return self();
    }

    public Blur tint(int argb) {
        return color(argb);
    }

    public Blur tint(Color color) {
        return tint(color.argb());
    }

    public RenderState build(GuiGraphicsExtractor graphics) {
        float maxRadius = Math.min(Math.min(width, height) * 0.5F, MAX_RADIUS);
        int topLeft = packRadius(radiusTopLeft, maxRadius, 2.0F, 6);
        int topRight = packRadius(radiusTopRight, maxRadius, 2.0F, 6);
        int bottomRight = packRadius(radiusBottomRight, maxRadius, 2.0F, 6);
        int bottomLeft = packRadius(radiusBottomLeft, maxRadius, 2.0F, 6);
        int opacity = Math.round(alpha * OPACITY_STEPS);
        Key key = new Key(type, strength);
        return new RenderState(
                key, DisplayBatcher.pose(graphics), x, y, width, height,
                topLeft | topRight << 6 | type.outputLevel(strength) << 12 | (opacity & 1) << 15,
                bottomLeft | bottomRight << 6 | (opacity >> 1) << 12,
                colorTopLeft, colorTopRight, colorBottomRight, colorBottomLeft,
                graphics.scissorStack.peek()
        );
    }

    public void render(GuiGraphicsExtractor graphics) {
        if (width <= 0.0F || height <= 0.0F || alpha <= 0.0F) {
            return;
        }
        DisplayBatcher.submit(graphics, build(graphics));
    }
}
