package dev.render4u.render.display.shadow;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class ShadowPipeline {
    public static final float PADDING = 1.0F;

    public static final RenderPipeline INSTANCE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath("render4u", "pipeline/shadow"))
                    .withVertexShader(Identifier.fromNamespaceAndPath("render4u", "core/shadow"))
                    .withFragmentShader(Identifier.fromNamespaceAndPath("render4u", "core/shadow"))
                    .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR)
                    .build()
    );

    private ShadowPipeline() {
    }

    public static void init() {
    }
}

