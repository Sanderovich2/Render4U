package dev.render4u.render.display;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import dev.render4u.render.display.image.GifTexture;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public final class Textures {
    private static final Map<Identifier, GifTexture> GIFS = new Object2ObjectOpenHashMap<>();

    private Textures() {
    }

    public static GifTexture gif(Identifier id) {
        GifTexture gif = GIFS.get(id);
        if (gif == null) {
            Identifier resource = withExtension(id, ".gif");
            try (InputStream stream = Minecraft.getInstance().getResourceManager().getResourceOrThrow(resource).open()) {
                gif = GifTexture.load(resource, stream);
            } catch (IOException e) {
                throw new RuntimeException("Cannot load gif: " + resource, e);
            }
            GIFS.put(id, gif);
        }
        return gif;
    }

    private static Identifier withExtension(Identifier id, String extension) {
        return id.getPath().endsWith(extension)
                ? id
                : Identifier.fromNamespaceAndPath(id.getNamespace(), id.getPath() + extension);
    }

    public static GpuTextureView uploadTexture(Identifier id, NativeImage level0) {
        int width = level0.getWidth();
        int height = level0.getHeight();
        GpuDevice device = RenderSystem.getDevice();
        GpuTexture texture = device.createTexture(
                () -> "render4u " + id,
                GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_COPY_DST,
                GpuFormat.RGBA8_UNORM,
                width, height, 1, 1
        );
        CommandEncoder encoder = device.createCommandEncoder();
        encoder.writeToTexture(texture, level0);
        level0.close();
        return device.createTextureView(texture);
    }

    public static NativeImage downscale(NativeImage image, int maxSide) {
        int width = image.getWidth();
        int height = image.getHeight();
        if (Math.max(width, height) <= maxSide) {
            return image;
        }
        float scale = (float) maxSide / Math.max(width, height);
        int newWidth = Math.max(1, Math.round(width * scale));
        int newHeight = Math.max(1, Math.round(height * scale));
        NativeImage result = new NativeImage(newWidth, newHeight, true);
        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < newWidth; x++) {
                int x0 = (int) ((float) x / scale);
                int x1 = Math.min(width, Math.max(x0 + 1, (int) ((float) (x + 1) / scale)));
                int y0 = (int) ((float) y / scale);
                int y1 = Math.min(height, Math.max(y0 + 1, (int) ((float) (y + 1) / scale)));
                long a = 0, b = 0, c = 0, d = 0;
                int count = 0;
                for (int sy = y0; sy < y1; sy++) {
                    for (int sx = x0; sx < x1; sx++) {
                        int pixel = image.getPixel(sx, sy);
                        a += (pixel >> 24) & 0xFF;
                        b += (pixel >> 16) & 0xFF;
                        c += (pixel >> 8) & 0xFF;
                        d += pixel & 0xFF;
                        count++;
                    }
                }
                int avg = (int) (a / count) << 24 | (int) (b / count) << 16 | (int) (c / count) << 8 | (int) (d / count);
                result.setPixel(x, y, avg);
            }
        }
        image.close();
        return result;
    }
}
