package dev.render4u.render.display.image;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public final class Images {
    private static final String NAMESPACE = "render4u";
    private static final String DIRECTORY = "images/";
    private static final Map<String, ImageAsset> REGISTRY = new LinkedHashMap<>();
    private static final Map<Identifier, ImageSize> SIZES = new HashMap<>();

    private Images() {
    }

    public static void init() {
    }

    public static ImageAsset register(String name) {
        return register(name, DIRECTORY + name + ".png");
    }

    public static ImageAsset register(String name, String path) {
        return REGISTRY.computeIfAbsent(name, key -> new ImageAsset(key, Identifier.fromNamespaceAndPath(NAMESPACE, path)));
    }

    public static ImageAsset get(String name) {
        ImageAsset asset = REGISTRY.get(name);
        if (asset == null) {
            throw new IllegalArgumentException("Unknown image: " + name);
        }
        return asset;
    }

    public static boolean has(String name) {
        return REGISTRY.containsKey(name);
    }

    public static Collection<ImageAsset> all() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }

    public static ImageSize size(Identifier texture) {
        return SIZES.computeIfAbsent(texture, Images::load);
    }

    public static void reload() {
        SIZES.clear();
    }

    private static ImageSize load(Identifier texture) {
        try (InputStream stream = Minecraft.getInstance().getResourceManager().getResourceOrThrow(texture).open();
             NativeImage image = NativeImage.read(stream)) {
            return new ImageSize(image.getWidth(), image.getHeight());
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to read image " + texture, exception);
        }
    }
}
