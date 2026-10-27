package dev.render4u.render.display.image;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import dev.render4u.render.display.Textures;
import dev.render4u.render.display.batch.DisplayBatcher;
import dev.render4u.render.display.shape.RoundedRect;
import dev.render4u.util.color.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public final class Image {
    private static final float MAX_RADIUS = 63.5F;
    private static final Map<String, DynamicTexture> CUSTOM = new ConcurrentHashMap<>();

    private Identifier texture;
    private @Nullable Supplier<GpuTextureView> source;
    private float x;
    private float y;
    private float width;
    private float height;
    private float radiusTopLeft;
    private float radiusTopRight;
    private float radiusBottomRight;
    private float radiusBottomLeft;
    private boolean circle;
    private int colorTopLeft = -1;
    private int colorTopRight = -1;
    private int colorBottomRight = -1;
    private int colorBottomLeft = -1;
    private float alpha = 1.0F;
    private boolean flipX;
    private boolean flipY;
    private boolean smooth = true;
    private ImageFit fit = ImageFit.STRETCH;
    private int overlay;
    private float overlayAmount;

    private Image(Identifier texture) {
        this.texture = texture;
    }

    public static Image of(Identifier texture) {
        return new Image(texture);
    }

    public static Image of(String path) {
        return of(Identifier.fromNamespaceAndPath("render4u", path));
    }

    public static Image of(ImageAsset asset) {
        return new Image(asset.texture());
    }

    public static Image of(Identifier texture, float x, float y, float width, float height) {
        return new Image(texture).bounds(x, y, width, height);
    }

    public static Image of(ImageAsset asset, float x, float y, float width, float height) {
        return new Image(asset.texture()).bounds(x, y, width, height);
    }

    public static Image ofGif(Identifier texture) {
        return new Image(texture).gif(texture);
    }

    public static Image ofGif(Identifier texture, float x, float y, float width, float height) {
        return new Image(texture).gif(texture).bounds(x, y, width, height);
    }

    public static Image of(BufferedImage image, String name) {
        String key = key(name);
        if (CUSTOM.containsKey(key)) {
            return new Image(customId(key));
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
        DynamicTexture dynamicTexture = new DynamicTexture(() -> "Image_" + key, image);
        if (CUSTOM.putIfAbsent(key, dynamicTexture) != null) {
            dynamicTexture.close();
        } else {
            Minecraft.getInstance().getTextureManager().register(customId(key), dynamicTexture);
        }
        return new Image(customId(key));
    }

    public static void update(String name, BufferedImage image) {
        update(name, toNativeImage(image));
    }

    public static void update(String name, NativeImage image) {
        DynamicTexture dynamicTexture = CUSTOM.get(key(name));
        if (dynamicTexture == null) {
            throw new IllegalStateException("Custom image not registered: " + name);
        }
        dynamicTexture.setPixels(image);
        dynamicTexture.upload();
    }

    private static Identifier customId(String key) {
        return Identifier.fromNamespaceAndPath("render4u", "custom/" + key);
    }

    private static String key(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9_]", "_");
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

    public Image gif(Identifier id) {
        this.source = () -> Textures.gif(id).currentFrame();
        return this;
    }

    public Image gif(GifTexture gif) {
        this.source = gif::currentFrame;
        return this;
    }

    public Image view(Supplier<GpuTextureView> source) {
        this.source = source;
        return this;
    }

    public Image texture(Identifier texture) {
        this.texture = texture;
        return this;
    }

    public Image texture(ImageAsset asset) {
        return texture(asset.texture());
    }

    public Image position(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Image size(float size) {
        return size(size, size);
    }

    public Image size(float width, float height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public Image bounds(float x, float y, float width, float height) {
        return position(x, y).size(width, height);
    }

    public Image fitWidth(float width) {
        return size(width, width / Images.size(texture).aspect());
    }

    public Image fitHeight(float height) {
        return size(height * Images.size(texture).aspect(), height);
    }

    public Image scale(float scale) {
        ImageSize size = Images.size(texture);
        return size(size.width() * scale, size.height() * scale);
    }

    public Image fit(ImageFit fit) {
        this.fit = fit;
        return this;
    }

    public Image contain() {
        return fit(ImageFit.CONTAIN);
    }

    public Image circle() {
        this.circle = true;
        return this;
    }

    public Image radius(float radius) {
        return radius(radius, radius, radius, radius);
    }

    public Image radius(float topLeft, float topRight, float bottomRight, float bottomLeft) {
        this.circle = false;
        this.radiusTopLeft = topLeft;
        this.radiusTopRight = topRight;
        this.radiusBottomRight = bottomRight;
        this.radiusBottomLeft = bottomLeft;
        return this;
    }

    public Image radiusTop(float radius) {
        this.circle = false;
        this.radiusTopLeft = radius;
        this.radiusTopRight = radius;
        return this;
    }

    public Image radiusBottom(float radius) {
        this.circle = false;
        this.radiusBottomLeft = radius;
        this.radiusBottomRight = radius;
        return this;
    }

    public Image radiusLeft(float radius) {
        this.circle = false;
        this.radiusTopLeft = radius;
        this.radiusBottomLeft = radius;
        return this;
    }

    public Image radiusRight(float radius) {
        this.circle = false;
        this.radiusTopRight = radius;
        this.radiusBottomRight = radius;
        return this;
    }

    public Image color(int argb) {
        return corners(argb, argb, argb, argb);
    }

    public Image verticalGradient(int top, int bottom) {
        return corners(top, top, bottom, bottom);
    }

    public Image horizontalGradient(int left, int right) {
        return corners(left, right, right, left);
    }

    public Image corners(int topLeft, int topRight, int bottomRight, int bottomLeft) {
        this.colorTopLeft = topLeft;
        this.colorTopRight = topRight;
        this.colorBottomRight = bottomRight;
        this.colorBottomLeft = bottomLeft;
        return this;
    }

    public Image color(Color color) {
        return color(color.argb());
    }

    public Image verticalGradient(Color top, Color bottom) {
        return verticalGradient(top.argb(), bottom.argb());
    }

    public Image horizontalGradient(Color left, Color right) {
        return horizontalGradient(left.argb(), right.argb());
    }

    public Image corners(Color topLeft, Color topRight, Color bottomRight, Color bottomLeft) {
        return corners(topLeft.argb(), topRight.argb(), bottomRight.argb(), bottomLeft.argb());
    }

    public Image mix(int argb, float amount) {
        this.overlay = argb;
        this.overlayAmount = Math.clamp(amount, 0.0F, 1.0F);
        return this;
    }

    public Image mix(Color color, float amount) {
        return mix(color.argb(), amount);
    }

    public Image alpha(float alpha) {
        this.alpha = Math.clamp(alpha, 0.0F, 1.0F);
        return this;
    }

    public Image flipX(boolean flipX) {
        this.flipX = flipX;
        return this;
    }

    public Image flipY(boolean flipY) {
        this.flipY = flipY;
        return this;
    }

    public Image smooth(boolean smooth) {
        this.smooth = smooth;
        return this;
    }

    public ImageSize size() {
        return Images.size(texture);
    }

    public float width() {
        return width;
    }

    public float height() {
        return height;
    }

    public ImageRenderState build(GuiGraphicsExtractor graphics) {
        float[] rect = rect();
        float maxRadius = Math.min(Math.min(rect[2], rect[3]) * 0.5F, MAX_RADIUS);
        int topLeft = packRadius(circle ? maxRadius : radiusTopLeft, maxRadius);
        int topRight = packRadius(circle ? maxRadius : radiusTopRight, maxRadius);
        int bottomRight = packRadius(circle ? maxRadius : radiusBottomRight, maxRadius);
        int bottomLeft = packRadius(circle ? maxRadius : radiusBottomLeft, maxRadius);
        return new ImageRenderState(
                DisplayBatcher.pose(graphics), rect[0], rect[1], rect[2], rect[3],
                topLeft | topRight << 7 | (flipX ? 1 : 0) << 14 | (flipY ? 1 : 0) << 15, bottomLeft | bottomRight << 7,
                applyAlpha(colorTopLeft), applyAlpha(colorTopRight), applyAlpha(colorBottomRight), applyAlpha(colorBottomLeft),
                this::resolveTexture, smooth, graphics.scissorStack.peek()
        );
    }

    private GpuTextureView resolveTexture() {
        return source != null ? source.get() : staticTexture(texture);
    }

    private static GpuTextureView staticTexture(Identifier id) {
        Identifier resource = id.getPath().endsWith(".png")
                ? id
                : Identifier.fromNamespaceAndPath(id.getNamespace(), id.getPath() + ".png");
        return Minecraft.getInstance().getTextureManager().getTexture(resource).getTextureView();
    }

    public void render(GuiGraphicsExtractor graphics) {
        if (width <= 0.0F || height <= 0.0F || alpha <= 0.0F) {
            return;
        }
        DisplayBatcher.submit(graphics, build(graphics));
        if (overlayAmount > 0.0F && (overlay >>> 24) > 0) {
            float[] rect = rect();
            float maxRadius = Math.min(Math.min(rect[2], rect[3]) * 0.5F, MAX_RADIUS);
            RoundedRect.of(rect[0], rect[1], rect[2], rect[3])
                    .radius(circle ? maxRadius : radiusTopLeft, circle ? maxRadius : radiusTopRight, circle ? maxRadius : radiusBottomRight, circle ? maxRadius : radiusBottomLeft)
                    .color(overlay)
                    .alpha(alpha * overlayAmount)
                    .render(graphics);
        }
    }

    private float[] rect() {
        if (fit == ImageFit.STRETCH) {
            return new float[]{x, y, width, height};
        }
        ImageSize size = Images.size(texture);
        float scale = Math.min(width / size.width(), height / size.height());
        float w = size.width() * scale;
        float h = size.height() * scale;
        return new float[]{x + (width - w) * 0.5F, y + (height - h) * 0.5F, w, h};
    }

    private int applyAlpha(int color) {
        int a = Math.round((color >>> 24) * alpha);
        return a << 24 | color & 0xFFFFFF;
    }

    private static int packRadius(float radius, float max) {
        return Math.clamp(Math.round(Math.clamp(radius, 0.0F, max) * 2.0F), 0, 127);
    }
}
