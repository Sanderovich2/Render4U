package dev.render4u.render.font.msdf;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import dev.render4u.render.display.Pipelines;

public final class TextPipeline {
    public static final RenderPipeline INSTANCE = Pipelines.TEXT;

    private TextPipeline() {
    }

    public static void init() {
    }
}
