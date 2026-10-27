package dev.render4u.render.display.image;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import dev.render4u.render.display.Pipelines;

public final class ImagePipeline {
    public static final float PADDING = 1.0F;
    public static final RenderPipeline INSTANCE = Pipelines.IMAGE;

    private ImagePipeline() {
    }

    public static void init() {
    }
}
