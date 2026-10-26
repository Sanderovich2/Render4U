package dev.render4u.render.font;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class TTFTextPipeline {
    public static final RenderPipeline INSTANCE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath("render4u", "pipeline/ttf_font"))
                    .withVertexShader(Identifier.fromNamespaceAndPath("render4u", "core/ttf_font"))
                    .withFragmentShader(Identifier.fromNamespaceAndPath("render4u", "core/ttf_font"))
                    .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR)
                    .build()
    );

    private TTFTextPipeline() {
    }

    public static void init() {
    }
}

