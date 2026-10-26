package dev.render4u.render.display.rounded;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class RoundedRectPipeline {
    public static final float PADDING = 1.0F;

    public static final RenderPipeline INSTANCE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath("render4u", "pipeline/rounded_rect"))
                    .withVertexShader(Identifier.fromNamespaceAndPath("render4u", "core/rounded_rect"))
                    .withFragmentShader(Identifier.fromNamespaceAndPath("render4u", "core/rounded_rect"))
                    .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR)
                    .build()
    );

    private RoundedRectPipeline() {
    }

    public static void init() {
    }
}

