package dev.render4u.render.display.shape;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import dev.render4u.render.display.Pipelines;
import dev.render4u.render.display.batch.DisplayBatcher;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.joml.Matrix3x2f;

public final class Image extends RectBuilder<Image> {
    private static final Map<String, DynamicTexture> CUSTOM = new ConcurrentHashMap<>();

    private final Identifier texture;
    private float u0;
    private float v0;
    private float u1 = 1.0F;
    private float v1 = 1.0F;

    private Image(Identifier texture) {
        this.texture = texture;
    }

    public static Image of(Identifier texture) {
        return new Image(texture);
    }

    public static Image of(String texturePath) {
        return of(Identifier.withDefaultNamespace(texturePath));
    }

    public static Image of(BufferedImage image, String name) {
        if (CUSTOM.containsKey(key(name))) {
            return new Image(customId(key(name)));
        }
        return of(toNativeImage(image), name);
    }

    public static Image of(Path file) {
        String name = file.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return of(file, dot > 0 ? name.substring(0, dot) : name);
    }

    public static Image of(Path file, String name) {
        try (InputStream stream = Files.newInputStream(file)) {
            BufferedImage buffered = ImageIO.read(stream);
            if (buffered == null) {
                throw new IOException("Unsupported image format: " + file);
            }
            return of(toNativeImage(buffered), name);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load image: " + file, e);
        }
    }

    public static Image of(NativeImage image, String name) {
        String key = key(name);
        if (CUSTOM.containsKey(key)) {
            image.close();
            return new Image(customId(key));
        }
        DynamicTexture texture = new DynamicTexture(() -> "Image_" + key, image);
        if (CUSTOM.putIfAbsent(key, texture) != null) {
            texture.close();
        } else {
            textures().register(customId(key), texture);
        }
        return new Image(customId(key));
    }

    public static void update(String name, BufferedImage image) {
        update(name, toNativeImage(image));
    }

    public static void update(String name, NativeImage image) {
        DynamicTexture texture = CUSTOM.get(key(name));
        if (texture == null) {
            throw new IllegalStateException("Custom image not registered: " + name);
        }
        texture.setPixels(image);
        texture.upload();
    }

    private static Identifier customId(String key) {
        return Identifier.fromNamespaceAndPath("render4u", "custom/" + key);
    }

    private static String key(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9_]", "_");
    }

    private static TextureManager textures() {
        return Minecraft.getInstance().getTextureManager();
    }

    private static NativeImage toNativeImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);
        NativeImage nativeImage = new NativeImage(NativeImage.Format.RGBA, width, height, false);
        for (int i = 0; i < pixels.length; i++) {
            nativeImage.setPixel(i % width, i / width, pixels[i]);
        }
        return nativeImage;
    }

    public Image uv(float u0, float v0, float u1, float v1) {
        this.u0 = u0;
        this.v0 = v0;
        this.u1 = u1;
        this.v1 = v1;
        return self();
    }

    public RenderState build(GuiGraphicsExtractor graphics) {
        float radius = Math.max(Math.max(radiusTopLeft, radiusTopRight), Math.max(radiusBottomRight, radiusBottomLeft));
        return new RenderState(
                DisplayBatcher.pose(graphics), texture, x, y, width, height,
                u0, v0, u1, v1, radius,
                applyAlpha(colorTopLeft), applyAlpha(colorTopRight), applyAlpha(colorBottomRight), applyAlpha(colorBottomLeft),
                graphics.scissorStack.peek()
        );
    }

    public void render(GuiGraphicsExtractor graphics) {
        if (width <= 0.0F || height <= 0.0F || alpha <= 0.0F) {
            return;
        }
        DisplayBatcher.submit(graphics, build(graphics));
    }

    public static final class RenderState implements GuiElementRenderState {
        private final Matrix3x2f pose;
        private final Identifier texture;
        private final float x;
        private final float y;
        private final float width;
        private final float height;
        private final float u0;
        private final float v0;
        private final float u1;
        private final float v1;
        private final int colorTopLeft;
        private final int colorTopRight;
        private final int colorBottomRight;
        private final int colorBottomLeft;
        private final int paramsX;
        private final int paramsY;
        private final @Nullable ScreenRectangle scissor;
        private final ScreenRectangle bounds;
        private @Nullable TextureSetup textureSetup;

        RenderState(Matrix3x2f pose, Identifier texture, float x, float y, float width, float height,
                    float u0, float v0, float u1, float v1, float radius,
                    int colorTopLeft, int colorTopRight, int colorBottomRight, int colorBottomLeft,
                    @Nullable ScreenRectangle scissor) {
            this.pose = pose;
            this.texture = texture;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.u0 = u0;
            this.v0 = v0;
            this.u1 = u1;
            this.v1 = v1;
            this.colorTopLeft = colorTopLeft;
            this.colorTopRight = colorTopRight;
            this.colorBottomRight = colorBottomRight;
            this.colorBottomLeft = colorBottomLeft;
            this.paramsX = packParamsX(radius, width, height);
            this.paramsY = this.paramsX != 0 ? Math.min(Math.round(height), 65535) : 0;
            this.scissor = scissor;
            ScreenRectangle raw = new ScreenRectangle((int) Math.floor(x), (int) Math.floor(y),
                    (int) Math.ceil(width) + 1, (int) Math.ceil(height) + 1).transformAxisAligned(pose);
            ScreenRectangle clipped = scissor != null ? raw.intersection(scissor) : raw;
            this.bounds = clipped != null ? clipped : ScreenRectangle.empty();
        }

        private static int packParamsX(float radius, float width, float height) {
            int radiusBits = Math.min(Math.round(radius), 63);
            if (radiusBits <= 0 || Math.round(width) > 1023 || Math.round(height) > 65535) {
                return 0;
            }
            return radiusBits | Math.min(Math.round(width), 1023) << 6;
        }

        @Override
        public void buildVertices(VertexConsumer consumer) {
            vertex(consumer, x, y, u0, v0, colorTopLeft);
            vertex(consumer, x, y + height, u0, v1, colorBottomLeft);
            vertex(consumer, x + width, y + height, u1, v1, colorBottomRight);
            vertex(consumer, x + width, y, u1, v0, colorTopRight);
        }

        private void vertex(VertexConsumer consumer, float px, float py, float u, float v, int color) {
            consumer.addVertexWith2DPose(pose, px, py).setUv(u, v).setUv2(paramsX, paramsY).setColor(color);
        }

        @Override
        public RenderPipeline pipeline() {
            return Pipelines.IMAGE;
        }

        @Override
        public TextureSetup textureSetup() {
            if (textureSetup == null) {
                GpuTextureView view = Minecraft.getInstance().getTextureManager().getTexture(texture).getTextureView();
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
}
