package dev.render4u.render.display.blur;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import dev.render4u.render.display.batch.DisplayBatcher;
import dev.render4u.util.color.Color;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.joml.Matrix3x2f;

public final class Blur {

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

    public static final class Pipelines {
        public static final float PADDING = 1.0F;

        public static final RenderPipeline DOWNSAMPLE = fullscreen("downsample", "blur_down", false, false);
        public static final RenderPipeline UPSAMPLE = fullscreen("upsample", "blur_up", false, false);
        public static final RenderPipeline GAUSSIAN_HORIZONTAL = fullscreen("gaussian_h", "blur_separable", false, false);
        public static final RenderPipeline GAUSSIAN_VERTICAL = fullscreen("gaussian_v", "blur_separable", true, false);
        public static final RenderPipeline BOX_HORIZONTAL = fullscreen("box_h", "blur_separable", false, true);
        public static final RenderPipeline BOX_VERTICAL = fullscreen("box_v", "blur_separable", true, true);

        public static final RenderPipeline PANEL = RenderPipelines.register(
                RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
                        .withLocation(id("pipeline/blur_panel"))
                        .withVertexShader(id("core/blur_panel"))
                        .withFragmentShader(id("core/blur_panel"))
                        .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR)
                        .build()
        );

        private Pipelines() {
        }

        private static RenderPipeline fullscreen(String name, String fragment, boolean vertical, boolean box) {
            RenderPipeline.Builder builder = RenderPipeline.builder()
                    .withLocation(id("pipeline/blur_" + name))
                    .withVertexShader(Identifier.withDefaultNamespace("core/screenquad"))
                    .withFragmentShader(id("core/" + fragment))
                    .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
                    .withColorTargetState(ColorTargetState.DEFAULT);
            RenderPipelines.TRACY_BLIT.getBindGroupLayouts().forEach(builder::withBindGroupLayout);
            if (vertical) {
                builder.withShaderDefine("BLUR_VERTICAL");
            }
            if (box) {
                builder.withShaderDefine("KERNEL_BOX");
            }
            return RenderPipelines.register(builder.build());
        }

        private static Identifier id(String path) {
            return Identifier.fromNamespaceAndPath("render4u", path);
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

    public static final class RenderState implements GuiElementRenderState {
        private final Key key;
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
        private @Nullable TextureSetup textureSetup;

        RenderState(Key key, Matrix3x2f pose, float x, float y, float width, float height, int packedTop, int packedBottom,
                    int topLeft, int topRight, int bottomRight, int bottomLeft, @Nullable ScreenRectangle scissor) {
            this.key = key;
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
            float pad = Pipelines.PADDING;
            ScreenRectangle raw = new ScreenRectangle((int) Math.floor(x - pad), (int) Math.floor(y - pad),
                    (int) Math.ceil(width + pad * 2.0F) + 1, (int) Math.ceil(height + pad * 2.0F) + 1).transformAxisAligned(pose);
            ScreenRectangle clipped = scissor != null ? raw.intersection(scissor) : raw;
            this.bounds = clipped != null ? clipped : ScreenRectangle.empty();
        }

        @Override
        public void buildVertices(VertexConsumer consumer) {
            float pad = Pipelines.PADDING;
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
            return Pipelines.PANEL;
        }

        @Override
        public TextureSetup textureSetup() {
            if (textureSetup == null) {
                Blur.request(key);
                textureSetup = TextureSetup.singleTexture(Blur.outputView(key), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
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

    private static final Targets TARGETS = new Targets();
    private static final Set<Key> REQUESTED = new LinkedHashSet<>();

    public static void init() {
        var _ = Pipelines.PANEL;
    }

    public static void request(Key key) {
        REQUESTED.add(key);
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
        for (Key key : REQUESTED) {
            int d = key.type().downDepth(key.strength());
            if (d > depth) {
                depth = d;
            }
        }

        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();

        GpuTextureView source = main.getColorTextureView();
        for (int level = 1; level <= depth; level++) {
            TextureTarget target = TARGETS.down(level);
            pass(encoder, Pipelines.DOWNSAMPLE, source, target);
            source = target.getColorTextureView();
        }

        for (Key key : REQUESTED) {
            processKey(encoder, key);
        }
        REQUESTED.clear();
    }

    public static void close() {
        REQUESTED.clear();
        TARGETS.close();
    }

    private static void processKey(CommandEncoder encoder, Key key) {
        switch (key.type()) {
            case GAUSSIAN -> separable(encoder, key, Pipelines.GAUSSIAN_HORIZONTAL, Pipelines.GAUSSIAN_VERTICAL);
            case KAWASE -> kawase(encoder, key);
            case BOX -> separable(encoder, key, Pipelines.BOX_HORIZONTAL, Pipelines.BOX_VERTICAL);
        }
    }

    private static void kawase(CommandEncoder encoder, Key key) {
        GpuTextureView source = TARGETS.down(key.strength()).getColorTextureView();
        for (int level = key.strength() - 1; level >= 1; level--) {
            TextureTarget target = level == 1 ? TARGETS.output(key) : TARGETS.temp(level);
            pass(encoder, Pipelines.UPSAMPLE, source, target);
            source = target.getColorTextureView();
        }
    }

    private static void separable(CommandEncoder encoder, Key key, RenderPipeline horizontal, RenderPipeline vertical) {
        int level = key.type().outputLevel(key.strength());
        int repeats = key.strength() > 3 ? 2 : 1;
        TextureTarget temp = TARGETS.temp(level);
        TextureTarget output = TARGETS.output(key);
        GpuTextureView source = TARGETS.down(level).getColorTextureView();
        for (int i = 0; i < repeats; i++) {
            pass(encoder, horizontal, source, temp);
            pass(encoder, vertical, temp.getColorTextureView(), output);
            source = output.getColorTextureView();
        }
    }

    private static void pass(CommandEncoder encoder, RenderPipeline pipeline, GpuTextureView source, TextureTarget target) {
        GpuSampler sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
        try (RenderPass renderPass = encoder.createRenderPass(() -> "Blur " + pipeline.getLocation().getPath(), target.getColorTextureView(), Optional.empty())) {
            renderPass.setPipeline(RenderSystem.getCompiledPipeline(pipeline));
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("InSampler", source, sampler);
            renderPass.draw(3, 1, 0, 0);
        }
    }

    private static RenderTarget mainTarget() {
        return Minecraft.getInstance().gameRenderer.mainRenderTarget();
    }

    private static final float MAX_RADIUS = 31.5F;
    private static final float OPACITY_STEPS = 31.0F;

    private float x;
    private float y;
    private float width;
    private float height;
    private float radiusTopLeft;
    private float radiusTopRight;
    private float radiusBottomRight;
    private float radiusBottomLeft;
    private Type type = Type.GAUSSIAN;
    private int strength = 3;
    private int tintTopLeft;
    private int tintTopRight;
    private int tintBottomRight;
    private int tintBottomLeft;
    private float alpha = 1.0F;

    private Blur() {
    }

    public static Blur create() {
        return new Blur();
    }

    public static Blur of(float x, float y, float width, float height) {
        return new Blur().bounds(x, y, width, height);
    }

    public Blur position(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Blur size(float width, float height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public Blur bounds(float x, float y, float width, float height) {
        return position(x, y).size(width, height);
    }

    public Blur radius(float radius) {
        return radius(radius, radius, radius, radius);
    }

    public Blur radius(float topLeft, float topRight, float bottomRight, float bottomLeft) {
        this.radiusTopLeft = topLeft;
        this.radiusTopRight = topRight;
        this.radiusBottomRight = bottomRight;
        this.radiusBottomLeft = bottomLeft;
        return this;
    }

    public Blur radiusTop(float radius) {
        this.radiusTopLeft = radius;
        this.radiusTopRight = radius;
        return this;
    }

    public Blur radiusBottom(float radius) {
        this.radiusBottomLeft = radius;
        this.radiusBottomRight = radius;
        return this;
    }

    public Blur radiusLeft(float radius) {
        this.radiusTopLeft = radius;
        this.radiusBottomLeft = radius;
        return this;
    }

    public Blur radiusRight(float radius) {
        this.radiusTopRight = radius;
        this.radiusBottomRight = radius;
        return this;
    }

    public Blur type(Type type) {
        this.type = type;
        return this;
    }

    public Blur strength(int strength) {
        this.strength = Math.clamp(strength, 1, Type.MAX_STRENGTH);
        return this;
    }

    public Blur tint(int argb) {
        return corners(argb, argb, argb, argb);
    }

    public Blur tint(Color color) {
        return tint(color.argb());
    }

    public Blur verticalGradient(Color top, Color bottom) {
        return corners(top.argb(), top.argb(), bottom.argb(), bottom.argb());
    }

    public Blur horizontalGradient(Color left, Color right) {
        return corners(left.argb(), right.argb(), right.argb(), left.argb());
    }

    public Blur corners(int topLeft, int topRight, int bottomRight, int bottomLeft) {
        this.tintTopLeft = topLeft;
        this.tintTopRight = topRight;
        this.tintBottomRight = bottomRight;
        this.tintBottomLeft = bottomLeft;
        return this;
    }

    public Blur alpha(float alpha) {
        this.alpha = Math.clamp(alpha, 0.0F, 1.0F);
        return this;
    }

    public RenderState build(GuiGraphicsExtractor graphics) {
        float maxRadius = Math.min(Math.min(width, height) * 0.5F, MAX_RADIUS);
        int topLeft = packRadius(radiusTopLeft, maxRadius);
        int topRight = packRadius(radiusTopRight, maxRadius);
        int bottomRight = packRadius(radiusBottomRight, maxRadius);
        int bottomLeft = packRadius(radiusBottomLeft, maxRadius);
        int opacity = Math.round(alpha * OPACITY_STEPS);
        Key key = new Key(type, strength);
        return new RenderState(
                key, DisplayBatcher.pose(graphics), x, y, width, height,
                topLeft | topRight << 6 | type.outputLevel(strength) << 12 | (opacity & 1) << 15,
                bottomLeft | bottomRight << 6 | (opacity >> 1) << 12,
                tintTopLeft, tintTopRight, tintBottomRight, tintBottomLeft,
                graphics.scissorStack.peek()
        );
    }

    public void render(GuiGraphicsExtractor graphics) {
        if (width <= 0.0F || height <= 0.0F || alpha <= 0.0F) {
            return;
        }
        DisplayBatcher.submit(graphics, build(graphics));
    }

    private static int packRadius(float radius, float max) {
        return Math.clamp(Math.round(Math.clamp(radius, 0.0F, max) * 2.0F), 0, 63);
    }
}

