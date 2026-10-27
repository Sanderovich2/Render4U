package dev.render4u.render.display.head;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import dev.render4u.render.display.Pipelines;

public final class PlayerHeadPipeline {
    public static final float PADDING = 1.0F;
    public static final RenderPipeline INSTANCE = Pipelines.HEAD;

    private PlayerHeadPipeline() {
    }

    public static void init() {
    }
}
